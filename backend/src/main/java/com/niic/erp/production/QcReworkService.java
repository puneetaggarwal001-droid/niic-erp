package com.niic.erp.production;

import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.dto.QcReworkDto;
import com.niic.erp.user.User;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QcReworkService {

    private final QcReworkRepository qcReworkRepository;
    private final QcEntryRepository qcEntryRepository;

    public QcReworkService(QcReworkRepository qcReworkRepository, QcEntryRepository qcEntryRepository) {
        this.qcReworkRepository = qcReworkRepository;
        this.qcEntryRepository = qcEntryRepository;
    }

    // Records a partial (or final) rework result. Every piece marked "done" loops
    // back into QC as a brand-new pending entry (rework -> QC -> maybe rework again).
    @Transactional
    public QcReworkDto recordResult(Long id, int done, int reject, User actor) {
        QcRework rework = qcReworkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rework record not found: " + id));
        if (rework.getStatus() == ReworkStatus.COMPLETED) {
            throw new BadRequestException("This rework has already been completed.");
        }
        if (done + reject <= 0) {
            throw new BadRequestException("Enter a done or reject quantity.");
        }
        int remaining = rework.getAlterQty() - rework.getReworkDone() - rework.getReworkReject();
        if (done + reject > remaining) {
            throw new BadRequestException("Only " + remaining + " pcs remaining for this rework.");
        }

        rework.setReworkDone(rework.getReworkDone() + done);
        rework.setReworkReject(rework.getReworkReject() + reject);
        rework.setUpdatedBy(actor);
        rework.setUpdatedAtBusiness(Instant.now());
        if (rework.getReworkDone() + rework.getReworkReject() >= rework.getAlterQty()) {
            rework.setStatus(ReworkStatus.COMPLETED);
        }

        if (done > 0) {
            QcEntry loopEntry = new QcEntry(LocalDate.now(), rework.getJob(), rework.getColour(), rework.getSize(),
                    rework.getSide(), rework.getJob().getUnit(), "Rework → QC", null, done,
                    QcStatus.PENDING_DETAILS, false, actor);
            qcEntryRepository.save(loopEntry);
        }

        return QcReworkDto.from(rework);
    }

    public List<QcReworkDto> listForJob(Long jobId) {
        return qcReworkRepository.findByJobId(jobId).stream().map(QcReworkDto::from).toList();
    }

    public List<QcReworkDto> listPending() {
        return qcReworkRepository.findByStatus(ReworkStatus.PENDING).stream().map(QcReworkDto::from).toList();
    }
}
