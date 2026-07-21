package com.niic.erp.production;

import com.niic.erp.attendance.Employee;
import com.niic.erp.attendance.EmployeeRepository;
import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.dto.OperationClosureDto;
import com.niic.erp.production.dto.OperationClosureRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationClosureService {

    private final OperationClosureRepository operationClosureRepository;
    private final JobRepository jobRepository;
    private final JobColourRepository jobColourRepository;
    private final JobSizeRepository jobSizeRepository;
    private final WorkstationRepository workstationRepository;
    private final OperationRepository operationRepository;
    private final EmployeeRepository employeeRepository;

    public OperationClosureService(OperationClosureRepository operationClosureRepository, JobRepository jobRepository,
                                    JobColourRepository jobColourRepository, JobSizeRepository jobSizeRepository,
                                    WorkstationRepository workstationRepository, OperationRepository operationRepository,
                                    EmployeeRepository employeeRepository) {
        this.operationClosureRepository = operationClosureRepository;
        this.jobRepository = jobRepository;
        this.jobColourRepository = jobColourRepository;
        this.jobSizeRepository = jobSizeRepository;
        this.workstationRepository = workstationRepository;
        this.operationRepository = operationRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public OperationClosureDto close(OperationClosureRequest request) {
        if (request.reason() == ClosureReason.REWORK && request.reworkQty() == null) {
            throw new BadRequestException("Rework quantity is required when the reason is REWORK.");
        }
        if (request.reason() == ClosureReason.REJECTION && request.rejectionQty() == null) {
            throw new BadRequestException("Rejection quantity is required when the reason is REJECTION.");
        }

        operationClosureRepository.findMatch(request.jobId(), request.colourId(), request.sizeId(),
                        request.workstationId(), request.operationId())
                .ifPresent(existing -> {
                    throw new BadRequestException("This operation is already closed for this job/colour/size.");
                });

        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new NotFoundException("Job not found: " + request.jobId()));
        JobColour colour = request.colourId() != null ? jobColourRepository.findById(request.colourId())
                .orElseThrow(() -> new NotFoundException("Job colour not found: " + request.colourId())) : null;
        JobSize size = request.sizeId() != null ? jobSizeRepository.findById(request.sizeId())
                .orElseThrow(() -> new NotFoundException("Job size not found: " + request.sizeId())) : null;
        Workstation workstation = workstationRepository.findById(request.workstationId())
                .orElseThrow(() -> new NotFoundException("Workstation not found: " + request.workstationId()));
        Operation operation = operationRepository.findById(request.operationId())
                .orElseThrow(() -> new NotFoundException("Operation not found: " + request.operationId()));
        Employee employee = employeeRepository.findById(request.closedByEmployeeId())
                .orElseThrow(() -> new NotFoundException("Employee not found: " + request.closedByEmployeeId()));

        OperationClosure closure = new OperationClosure(job, colour, size, workstation, operation,
                request.doneQtyAtClosure(), request.plannedQty(), request.reason(), request.reworkQty(),
                request.rejectionQty(), request.notes(), employee, request.date());
        return OperationClosureDto.from(operationClosureRepository.save(closure));
    }

    public List<OperationClosureDto> listForJob(Long jobId) {
        return operationClosureRepository.findByJobId(jobId).stream().map(OperationClosureDto::from).toList();
    }

    // Reopening just deletes the closure — legacy keeps no audit trail of reopens either.
    @Transactional
    public void reopen(Long id) {
        if (!operationClosureRepository.existsById(id)) {
            throw new NotFoundException("Operation closure not found: " + id);
        }
        operationClosureRepository.deleteById(id);
    }
}
