package com.niic.erp.sampling.dto;

import com.niic.erp.sampling.PpmSection;
import com.niic.erp.sampling.PpsOptionType;
import com.niic.erp.sampling.Sample;
import com.niic.erp.sampling.SampleLineType;
import com.niic.erp.sampling.SamplePpmRow;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Full sample detail: header, lines grouped by kind, PPS spec, PPM sections, photos. */
public record SampleDto(
        Long id,
        String sampleNo,
        String revBase,
        int revNum,
        LocalDate date,
        String name,
        String style,
        String category,
        String designer,
        String reference,
        String notes,
        String status,
        String closedRemark,
        Instant closedAt,
        String createdBy,
        Instant createdAt,
        Instant updatedAt,
        List<SampleLineDto> rawMaterials,
        List<SampleLineDto> sfgItems,
        List<SampleLineDto> operations,
        PpsDto pps,
        PpmDto ppm,
        List<PhotoDto> photos) {

    public record PpsDto(
            String fabricDetails,
            Integer designCount,
            String specialInstructions,
            List<String> colours,
            List<String> sizes,
            Instant savedAt,
            Instant approvedAt,
            String approvedBy) {
    }

    public record PpmDto(
            List<PpmRowDto> sop,
            List<PpmRowDto> trim,
            List<PpmRowDto> mark,
            List<PpmRowDto> check,
            List<PpmRowDto> pack) {
    }

    public static SampleDto from(Sample s) {
        List<SampleLineDto> rm = linesOf(s, SampleLineType.RM);
        List<SampleLineDto> sfg = linesOf(s, SampleLineType.SFG);
        List<SampleLineDto> ops = linesOf(s, SampleLineType.OP);

        List<String> colours = s.getPpsOptions().stream()
                .filter(o -> o.getOptType() == PpsOptionType.COLOUR).map(o -> o.getValue()).toList();
        List<String> sizes = s.getPpsOptions().stream()
                .filter(o -> o.getOptType() == PpsOptionType.SIZE).map(o -> o.getValue()).toList();
        PpsDto pps = new PpsDto(s.getPpsFabricDetails(), s.getPpsDesignCount(), s.getPpsSpecialInstructions(),
                colours, sizes, s.getPpsSavedAt(), s.getPpsApprovedAt(), s.getPpsApprovedBy());

        PpmDto ppm = new PpmDto(ppmOf(s, PpmSection.SOP), ppmOf(s, PpmSection.TRIM), ppmOf(s, PpmSection.MARK),
                ppmOf(s, PpmSection.CHECK), ppmOf(s, PpmSection.PACK));

        List<PhotoDto> photos = s.getPhotos().stream().map(PhotoDto::from).toList();

        return new SampleDto(
                s.getId(), s.getSampleNo(), s.getRevBase(), SampleSummaryDto.revNum(s.getSampleNo()),
                s.getSampleDate(), s.getName(), s.getStyle(), s.getCategory(), s.getDesigner(), s.getReference(),
                s.getNotes(), s.getStatus().name(),
                s.getClosedRemark() != null ? s.getClosedRemark().name() : null, s.getClosedAt(),
                s.getCreatedBy(), s.getCreatedAt(), s.getUpdatedAt(),
                rm, sfg, ops, pps, ppm, photos);
    }

    private static List<SampleLineDto> linesOf(Sample s, SampleLineType type) {
        return s.getLines().stream().filter(l -> l.getLineType() == type).map(SampleLineDto::from).toList();
    }

    private static List<PpmRowDto> ppmOf(Sample s, PpmSection section) {
        return s.getPpmRows().stream().filter(r -> r.getSection() == section).map(PpmRowDto::from).toList();
    }
}
