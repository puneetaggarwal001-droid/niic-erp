package com.niic.erp.production;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.dto.QcEntryCreateRequest;
import com.niic.erp.production.dto.QcEntryDto;
import com.niic.erp.production.dto.QcEntryFillRequest;
import com.niic.erp.user.User;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QcEntryService {

    private final QcEntryRepository qcEntryRepository;
    private final QcReworkRepository qcReworkRepository;
    private final JobRepository jobRepository;
    private final JobColourRepository jobColourRepository;
    private final JobSizeRepository jobSizeRepository;
    private final WorkstationRepository workstationRepository;

    public QcEntryService(QcEntryRepository qcEntryRepository, QcReworkRepository qcReworkRepository,
                           JobRepository jobRepository, JobColourRepository jobColourRepository,
                           JobSizeRepository jobSizeRepository, WorkstationRepository workstationRepository) {
        this.qcEntryRepository = qcEntryRepository;
        this.qcReworkRepository = qcReworkRepository;
        this.jobRepository = jobRepository;
        this.jobColourRepository = jobColourRepository;
        this.jobSizeRepository = jobSizeRepository;
        this.workstationRepository = workstationRepository;
    }

    @Transactional
    public QcEntryDto create(QcEntryCreateRequest request, User actor) {
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new NotFoundException("Job not found: " + request.jobId()));
        JobColour colour = request.colourId() != null ? jobColourRepository.findById(request.colourId())
                .orElseThrow(() -> new NotFoundException("Job colour not found: " + request.colourId())) : null;
        JobSize size = request.sizeId() != null ? jobSizeRepository.findById(request.sizeId())
                .orElseThrow(() -> new NotFoundException("Job size not found: " + request.sizeId())) : null;
        Workstation workstation = request.workstationId() != null ? workstationRepository.findById(request.workstationId())
                .orElseThrow(() -> new NotFoundException("Workstation not found: " + request.workstationId())) : null;

        if (request.skipDetails()) {
            QcEntry entry = new QcEntry(request.date(), job, colour, size, request.side(), job.getUnit(),
                    request.opRef(), workstation, request.totalChecked(), QcStatus.PENDING_DETAILS, false, actor);
            return QcEntryDto.from(qcEntryRepository.save(entry));
        }

        validateTotals(request.totalChecked(), request.passQty(), request.alterQty(), request.rejectQty(), job, size);
        QcEntry entry = new QcEntry(request.date(), job, colour, size, request.side(), job.getUnit(), request.opRef(),
                workstation, request.totalChecked(), QcStatus.COMPLETED, false, actor);
        entry.setPassQty(request.passQty());
        entry.setAlterQty(request.alterQty());
        entry.setRejectQty(request.rejectQty());
        entry = qcEntryRepository.save(entry);
        spawnReworkIfNeeded(entry, actor);
        return QcEntryDto.from(entry);
    }

    @Transactional
    public QcEntryDto fillDetails(Long id, QcEntryFillRequest request, User actor) {
        QcEntry entry = qcEntryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QC entry not found: " + id));
        if (entry.getStatus() == QcStatus.COMPLETED) {
            throw new BadRequestException("This QC entry has already been completed.");
        }
        validateTotals(entry.getTotalChecked(), request.passQty(), request.alterQty(), request.rejectQty(),
                entry.getJob(), entry.getSize());

        entry.setPassQty(request.passQty());
        entry.setAlterQty(request.alterQty());
        entry.setRejectQty(request.rejectQty());
        entry.setStatus(QcStatus.COMPLETED);
        entry.setUpdatedBy(actor);
        entry.setUpdatedAtBusiness(Instant.now());
        spawnReworkIfNeeded(entry, actor);
        return QcEntryDto.from(entry);
    }

    public List<QcEntryDto> listForJob(Long jobId) {
        return qcEntryRepository.findByJobId(jobId).stream().map(QcEntryDto::from).toList();
    }

    public List<QcEntryDto> listPending() {
        return qcEntryRepository.findByStatus(QcStatus.PENDING_DETAILS).stream().map(QcEntryDto::from).toList();
    }

    private void validateTotals(int total, int pass, int alter, int reject, Job job, JobSize size) {
        if (total <= 0) {
            throw new BadRequestException("Total checked must be greater than zero.");
        }
        if (pass + alter + reject != total) {
            throw new BadRequestException("Pass + alter + reject must add up to the total checked.");
        }
        int planned = size != null ? size.getPlannedQty() : job.getTotalPlannedQty();
        if (total > planned) {
            throw new BadRequestException("Total checked cannot exceed the job's planned quantity (" + planned + ").");
        }
    }

    // One rework record per QC entry that has alter > 0 — created the moment the
    // entry is completed (whether immediately or via fillDetails), never re-created
    // on subsequent saves.
    private void spawnReworkIfNeeded(QcEntry entry, User actor) {
        if (entry.getAlterQty() <= 0 || qcReworkRepository.findByQcEntryId(entry.getId()).isPresent()) {
            return;
        }
        QcRework rework = new QcRework(entry, entry.getDate(), entry.getJob(), entry.getColour(), entry.getSize(),
                entry.getSide(), entry.getAlterQty(), actor);
        qcReworkRepository.save(rework);
    }
}
