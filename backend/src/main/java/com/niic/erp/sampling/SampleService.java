package com.niic.erp.sampling;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.sampling.dto.PhotoDto;
import com.niic.erp.sampling.dto.PhotoForm;
import com.niic.erp.sampling.dto.PpmForm;
import com.niic.erp.sampling.dto.PpsForm;
import com.niic.erp.sampling.dto.SampleDto;
import com.niic.erp.sampling.dto.SampleForm;
import com.niic.erp.sampling.dto.SampleSummaryDto;
import com.niic.erp.security.CurrentUserProvider;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sample lifecycle service. Mirrors the legacy sampling module: numbering
 * (SMP-NNN with -Rn revision chains), the DRAFT -> IN_REVIEW -> SELECTED ->
 * PPS_DONE -> PPM_DONE progression, chain-closing on selection, and PPS/PPM
 * capture. DTO mapping runs inside the transaction so lazy collections load.
 */
@Service
public class SampleService {

    private static final Set<SampleStatus> HEADER_EDITABLE = EnumSet.of(SampleStatus.DRAFT, SampleStatus.IN_REVIEW);
    private static final Set<SampleStatus> PPS_EDITABLE = EnumSet.of(SampleStatus.SELECTED, SampleStatus.PPS_DONE);
    private static final Set<SampleStatus> PPM_EDITABLE = EnumSet.of(SampleStatus.PPS_DONE, SampleStatus.PPM_DONE);

    private final SampleRepository sampleRepository;
    private final SampleRequestRepository requestRepository;
    private final SamplePhotoRepository photoRepository;
    private final CurrentUserProvider currentUserProvider;

    public SampleService(SampleRepository sampleRepository, SampleRequestRepository requestRepository,
                         SamplePhotoRepository photoRepository, CurrentUserProvider currentUserProvider) {
        this.sampleRepository = sampleRepository;
        this.requestRepository = requestRepository;
        this.photoRepository = photoRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // ---- Reads -----------------------------------------------------------

    @Transactional(readOnly = true)
    public List<SampleSummaryDto> list(SampleStatus status, boolean includeClosed) {
        List<Sample> samples = status != null
                ? sampleRepository.findByStatus(status)
                : sampleRepository.findByOrderByCreatedAtDesc();
        return samples.stream()
                .filter(s -> includeClosed || (s.getStatus() != SampleStatus.CLOSED && s.getStatus() != SampleStatus.REJECTED))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(SampleSummaryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SampleDto getDto(Long id) {
        return SampleDto.from(get(id));
    }

    public Sample get(Long id) {
        return sampleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sample " + id + " not found."));
    }

    // ---- Create / update -------------------------------------------------

    @Transactional
    public SampleDto create(SampleForm form, boolean submit) {
        requireName(form);
        Sample sample = new Sample(form.name().trim(),
                form.date() != null ? form.date() : LocalDate.now(),
                currentUserProvider.require().getUsername());
        applyHeader(sample, form);
        String no = nextSampleNo();
        sample.setSampleNo(no);
        sample.setRevBase(no);
        applyLines(sample, form);
        sample.setStatus(submit ? SampleStatus.IN_REVIEW : SampleStatus.DRAFT);
        // Flush so the sample and its child lines receive their identities before mapping.
        return SampleDto.from(sampleRepository.saveAndFlush(sample));
    }

    /** Next SMP-NNN number: the highest existing base sequence plus one (legacy semantics). */
    private String nextSampleNo() {
        int max = sampleRepository.findAll().stream()
                .map(Sample::getRevBase)
                .filter(b -> b != null && b.startsWith("SMP-"))
                .mapToInt(SampleService::baseSeq)
                .max().orElse(0);
        return String.format("SMP-%03d", max + 1);
    }

    private static int baseSeq(String base) {
        try {
            return Integer.parseInt(base.substring(base.lastIndexOf('-') + 1));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return 0;
        }
    }

    @Transactional
    public SampleDto update(Long id, SampleForm form, boolean submit) {
        requireName(form);
        Sample sample = get(id);
        if (!HEADER_EDITABLE.contains(sample.getStatus())) {
            throw new BadRequestException("Only draft or in-review samples can be edited.");
        }
        applyHeader(sample, form);
        sample.getLines().clear();
        applyLines(sample, form);
        if (submit) {
            sample.setStatus(SampleStatus.IN_REVIEW);
        }
        return SampleDto.from(sample);
    }

    @Transactional
    public void delete(Long id) {
        Sample sample = get(id);
        sampleRepository.delete(sample);
    }

    private void requireName(SampleForm form) {
        if (form.name() == null || form.name().isBlank()) {
            throw new BadRequestException("Sample name is required.");
        }
    }

    private void applyHeader(Sample sample, SampleForm form) {
        sample.setName(form.name().trim());
        sample.setSampleDate(form.date() != null ? form.date() : sample.getSampleDate());
        sample.setStyle(trimToNull(form.style()));
        sample.setCategory(trimToNull(form.category()));
        sample.setDesigner(trimToNull(form.designer()));
        sample.setReference(trimToNull(form.reference()));
        sample.setNotes(trimToNull(form.notes()));
    }

    private void applyLines(Sample sample, SampleForm form) {
        int seq = 0;
        seq = addLines(sample, SampleLineType.RM, form.rawMaterials(), seq);
        seq = addLines(sample, SampleLineType.SFG, form.sfgItems(), seq);
        addLines(sample, SampleLineType.OP, form.operations(), seq);
    }

    private int addLines(Sample sample, SampleLineType type, List<SampleForm.LineForm> rows, int seq) {
        if (rows == null) {
            return seq;
        }
        for (SampleForm.LineForm r : rows) {
            boolean empty = (r.name() == null || r.name().isBlank())
                    && (r.description() == null || r.description().isBlank())
                    && r.itemId() == null && r.qty() == null;
            if (empty) {
                continue;
            }
            sample.getLines().add(new SampleLine(sample, type, r.itemId(), trimToNull(r.name()),
                    trimToNull(r.description()), trimToNull(r.colour()), r.qty(),
                    trimToNull(r.unit()), seq++));
        }
        return seq;
    }

    // ---- Lifecycle -------------------------------------------------------

    @Transactional
    public SampleDto submit(Long id) {
        Sample sample = get(id);
        if (sample.getStatus() != SampleStatus.DRAFT) {
            throw new BadRequestException("Only a draft can be submitted for review.");
        }
        sample.setStatus(SampleStatus.IN_REVIEW);
        return SampleDto.from(sample);
    }

    @Transactional
    public SampleDto select(Long id) {
        Sample sample = get(id);
        if (sample.getStatus() != SampleStatus.IN_REVIEW) {
            throw new BadRequestException("Only an in-review sample can be selected.");
        }
        // Block if any change requests for this chain are still open.
        List<Long> chainIds = sampleRepository.findByRevBase(sample.getRevBase()).stream()
                .map(Sample::getId).toList();
        List<SampleRequest> pendingChanges = requestRepository.findByRefSampleIdInAndReqTypeAndStatusIn(
                chainIds, RequestType.CHANGE, EnumSet.of(RequestStatus.PENDING, RequestStatus.IN_PROGRESS));
        if (!pendingChanges.isEmpty()) {
            throw new BadRequestException(pendingChanges.size()
                    + " change request(s) still pending; complete them before selecting.");
        }
        sample.setStatus(SampleStatus.SELECTED);
        closeChain(sample.getRevBase(), id, ClosedRemark.SELECTED);
        resolveRequests(id, ClosedRemark.SELECTED);
        return SampleDto.from(sample);
    }

    @Transactional
    public SampleDto reject(Long id) {
        Sample sample = get(id);
        if (sample.getStatus() == SampleStatus.CLOSED || sample.getStatus() == SampleStatus.REJECTED) {
            throw new BadRequestException("Sample is already closed.");
        }
        sample.setStatus(SampleStatus.REJECTED);
        sample.setClosedAt(Instant.now());
        resolveRequests(id, ClosedRemark.REJECTED);
        return SampleDto.from(sample);
    }

    @Transactional
    public SampleDto close(Long id, String remark) {
        Sample sample = get(id);
        sample.setStatus(SampleStatus.CLOSED);
        sample.setClosedRemark(parseRemark(remark));
        sample.setClosedAt(Instant.now());
        return SampleDto.from(sample);
    }

    /** Create a new revision (-Rn) in the same chain, carrying over the header and lines. */
    @Transactional
    public SampleDto revise(Long id) {
        Sample parent = get(id);
        String base = parent.getRevBase();
        int nextRev = sampleRepository.findByRevBase(base).stream()
                .mapToInt(s -> SampleSummaryDto.revNum(s.getSampleNo()))
                .max().orElse(0) + 1;
        Sample copy = new Sample(parent.getName(), LocalDate.now(), currentUserProvider.require().getUsername());
        copy.setStyle(parent.getStyle());
        copy.setCategory(parent.getCategory());
        copy.setDesigner(parent.getDesigner());
        copy.setReference(parent.getReference());
        copy.setNotes(parent.getNotes());
        copy.setSampleDate(parent.getSampleDate());
        copy.setStatus(SampleStatus.DRAFT);
        copy.setSampleNo(base + "-R" + nextRev);
        copy.setRevBase(base);
        int seq = 0;
        for (SampleLine l : parent.getLines()) {
            copy.getLines().add(new SampleLine(copy, l.getLineType(), l.getItemId(), l.getName(),
                    l.getDescription(), l.getColour(), l.getQty(), l.getUnit(), seq++));
        }
        return SampleDto.from(sampleRepository.saveAndFlush(copy));
    }

    // ---- PPS -------------------------------------------------------------

    @Transactional
    public SampleDto savePps(Long id, PpsForm form, boolean approve) {
        Sample sample = get(id);
        if (!PPS_EDITABLE.contains(sample.getStatus())) {
            throw new BadRequestException("PPS can only be edited once a sample is selected.");
        }
        sample.setPpsFabricDetails(trimToNull(form.fabricDetails()));
        sample.setPpsDesignCount(form.designCount());
        sample.setPpsSpecialInstructions(trimToNull(form.specialInstructions()));
        sample.getPpsOptions().clear();
        int seq = 0;
        for (String c : nonNull(form.colours())) {
            if (c != null && !c.isBlank()) {
                sample.getPpsOptions().add(new SamplePpsOption(sample, PpsOptionType.COLOUR, c.trim(), seq++));
            }
        }
        seq = 0;
        for (String s : nonNull(form.sizes())) {
            if (s != null && !s.isBlank()) {
                sample.getPpsOptions().add(new SamplePpsOption(sample, PpsOptionType.SIZE, s.trim(), seq++));
            }
        }
        sample.setPpsSavedAt(Instant.now());
        if (approve) {
            if (form.fabricDetails() == null || form.fabricDetails().isBlank()) {
                throw new BadRequestException("Fabric details are required to approve the PPS.");
            }
            sample.setPpsApprovedAt(Instant.now());
            sample.setPpsApprovedBy(currentUserProvider.require().getUsername());
            sample.setStatus(SampleStatus.PPS_DONE);
        }
        return SampleDto.from(sample);
    }

    // ---- PPM -------------------------------------------------------------

    @Transactional
    public SampleDto savePpm(Long id, PpmForm form) {
        Sample sample = get(id);
        if (!PPM_EDITABLE.contains(sample.getStatus())) {
            throw new BadRequestException("PPM can only be edited once the PPS is approved.");
        }
        PpmSection section = parseSection(form.section());
        sample.getPpmRows().removeIf(r -> r.getSection() == section);
        int seq = 0;
        for (PpmForm.RowForm row : nonNull(form.rows())) {
            boolean empty = isBlank(row.text1()) && isBlank(row.text2()) && isBlank(row.remark());
            if (empty) {
                continue;
            }
            sample.getPpmRows().add(new SamplePpmRow(sample, section, seq++,
                    trimToNull(row.text1()), trimToNull(row.text2()), trimToNull(row.remark())));
        }
        sample.setPpmSavedAt(Instant.now());
        return SampleDto.from(sample);
    }

    @Transactional
    public SampleDto completePpm(Long id) {
        Sample sample = get(id);
        if (sample.getStatus() != SampleStatus.PPS_DONE && sample.getStatus() != SampleStatus.PPM_DONE) {
            throw new BadRequestException("The PPS must be approved before completing the PPM.");
        }
        sample.setStatus(SampleStatus.PPM_DONE);
        return SampleDto.from(sample);
    }

    // ---- Photos ----------------------------------------------------------

    @Transactional
    public PhotoDto addPhoto(Long id, PhotoForm form) {
        Sample sample = get(id);
        PhotoSection section = parsePhotoSection(form.section());
        int seq = (int) sample.getPhotos().stream().filter(p -> p.getSection() == section).count();
        SamplePhoto photo = new SamplePhoto(sample, section, form.dataUrl(), trimToNull(form.caption()), seq);
        sample.getPhotos().add(photo);
        // Persist the child directly so the returned DTO carries its generated id.
        return PhotoDto.from(photoRepository.saveAndFlush(photo));
    }

    @Transactional
    public void deletePhoto(Long sampleId, Long photoId) {
        Sample sample = get(sampleId);
        boolean removed = sample.getPhotos().removeIf(p -> p.getId().equals(photoId));
        if (!removed) {
            throw new NotFoundException("Photo " + photoId + " not on sample " + sampleId + ".");
        }
    }

    // ---- Helpers ---------------------------------------------------------

    /** Close every other still-open sample in the chain, tagging the reason. */
    private void closeChain(String base, Long keepId, ClosedRemark remark) {
        for (Sample s : sampleRepository.findByRevBase(base)) {
            if (s.getId().equals(keepId)) {
                continue;
            }
            if (s.getStatus() == SampleStatus.CLOSED || s.getStatus() == SampleStatus.REJECTED) {
                continue;
            }
            s.setStatus(SampleStatus.CLOSED);
            s.setClosedRemark(remark);
            s.setClosedAt(Instant.now());
        }
    }

    /** Mark the completed request(s) that produced this sample as admin-resolved. */
    private void resolveRequests(Long sampleId, ClosedRemark remark) {
        String username = currentUserProvider.require().getUsername();
        for (SampleRequest r : requestRepository.findByCompletedSampleId(sampleId)) {
            if (r.getStatus() == RequestStatus.COMPLETED && r.getAdminResolvedAt() == null) {
                r.setAdminResolvedAt(Instant.now());
                r.setAdminResolvedBy(username);
                r.setAdminResolvedRemark(remark);
            }
        }
    }

    private static <T> List<T> nonNull(List<T> list) {
        return list != null ? list : List.of();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static ClosedRemark parseRemark(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return ClosedRemark.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown close remark: " + s);
        }
    }

    private static PpmSection parseSection(String s) {
        try {
            return PpmSection.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Unknown PPM section: " + s);
        }
    }

    private static PhotoSection parsePhotoSection(String s) {
        try {
            return PhotoSection.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Unknown photo section: " + s);
        }
    }
}
