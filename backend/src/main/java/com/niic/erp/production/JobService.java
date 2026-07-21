package com.niic.erp.production;

import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.dto.JobColourRequest;
import com.niic.erp.production.dto.JobCreateRequest;
import com.niic.erp.production.dto.JobDto;
import com.niic.erp.production.dto.JobSizeRequest;
import com.niic.erp.user.User;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final StyleRepository styleRepository;
    private final JobNumberAllocator jobNumberAllocator;

    public JobService(JobRepository jobRepository, StyleRepository styleRepository,
                       JobNumberAllocator jobNumberAllocator) {
        this.jobRepository = jobRepository;
        this.styleRepository = styleRepository;
        this.jobNumberAllocator = jobNumberAllocator;
    }

    // Shared by both creation paths: an admin creating a job directly, and an
    // admin approving a pending JobRequest (see JobRequestService). The job
    // number is only allocated here — i.e. at the point the job actually goes
    // live — unlike the legacy app, which burned a number even for requests
    // that later got rejected.
    @Transactional
    public JobDto createJob(JobCreateRequest request, JobSource source, User createdBy) {
        Style style = styleRepository.findByCode(request.styleCode())
                .orElseThrow(() -> new NotFoundException("Style not found: " + request.styleCode()));
        String jobDisplayId = jobNumberAllocator.nextJobDisplayId(style.getCode());
        Job job = new Job(jobDisplayId, style, request.modelNo(), request.fgItemId(), request.unit(), source, createdBy);

        int total = 0;
        int colourSeq = 0;
        for (JobColourRequest colourReq : request.colours()) {
            JobColour colour = new JobColour(job, colourReq.name(), colourSeq++);
            int sizeSeq = 0;
            for (JobSizeRequest sizeReq : colourReq.sizes()) {
                colour.getSizes().add(new JobSize(colour, sizeReq.size(), sizeReq.plannedQty(), sizeReq.variantId(), sizeSeq++));
                total += sizeReq.plannedQty();
            }
            job.getColours().add(colour);
        }
        job.setTotalPlannedQty(total);
        return JobDto.from(jobRepository.save(job));
    }

    @Transactional(readOnly = true)
    public List<JobDto> listActive() {
        return jobRepository.findByActiveTrue().stream().map(JobDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<JobDto> listAll() {
        return jobRepository.findAll().stream().map(JobDto::from).toList();
    }

    @Transactional(readOnly = true)
    public JobDto get(Long id) {
        return JobDto.from(findJob(id));
    }

    Job findJob(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> new NotFoundException("Job not found: " + id));
    }

    @Transactional
    public JobDto setActive(Long id, boolean active) {
        Job job = findJob(id);
        job.setActive(active);
        return JobDto.from(job);
    }
}
