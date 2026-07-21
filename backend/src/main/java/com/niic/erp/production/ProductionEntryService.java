package com.niic.erp.production;

import com.niic.erp.attendance.AttendanceRecordRepository;
import com.niic.erp.attendance.Employee;
import com.niic.erp.attendance.EmployeeRepository;
import com.niic.erp.common.BadRequestException;
import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.dto.ProductionEntryDto;
import com.niic.erp.production.dto.ProductionEntryOpRequest;
import com.niic.erp.production.dto.ProductionEntryRequest;
import com.niic.erp.user.User;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductionEntryService {

    private static final String QUALITY_WORKSTATION_MARKER = "quality";

    private final ProductionEntryRepository productionEntryRepository;
    private final EmployeeRepository employeeRepository;
    private final JobRepository jobRepository;
    private final JobColourRepository jobColourRepository;
    private final JobSizeRepository jobSizeRepository;
    private final WorkstationRepository workstationRepository;
    private final OperationRepository operationRepository;
    private final RoutingRepository routingRepository;
    private final OperationClosureRepository operationClosureRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final QcEntryRepository qcEntryRepository;

    public ProductionEntryService(ProductionEntryRepository productionEntryRepository,
                                   EmployeeRepository employeeRepository, JobRepository jobRepository,
                                   JobColourRepository jobColourRepository, JobSizeRepository jobSizeRepository,
                                   WorkstationRepository workstationRepository, OperationRepository operationRepository,
                                   RoutingRepository routingRepository,
                                   OperationClosureRepository operationClosureRepository,
                                   AttendanceRecordRepository attendanceRecordRepository,
                                   QcEntryRepository qcEntryRepository) {
        this.productionEntryRepository = productionEntryRepository;
        this.employeeRepository = employeeRepository;
        this.jobRepository = jobRepository;
        this.jobColourRepository = jobColourRepository;
        this.jobSizeRepository = jobSizeRepository;
        this.workstationRepository = workstationRepository;
        this.operationRepository = operationRepository;
        this.routingRepository = routingRepository;
        this.operationClosureRepository = operationClosureRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.qcEntryRepository = qcEntryRepository;
    }

    @Transactional
    public ProductionEntryDto save(ProductionEntryRequest request, User actor) {
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new NotFoundException("Employee not found: " + request.employeeId()));
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new NotFoundException("Job not found: " + request.jobId()));
        JobColour colour = request.colourId() != null ? jobColourRepository.findById(request.colourId())
                .orElseThrow(() -> new NotFoundException("Job colour not found: " + request.colourId())) : null;
        JobSize size = request.sizeId() != null ? jobSizeRepository.findById(request.sizeId())
                .orElseThrow(() -> new NotFoundException("Job size not found: " + request.sizeId())) : null;

        if (!job.getColours().isEmpty() && (colour == null || size == null)) {
            throw new BadRequestException("Colour and size are required for this job.");
        }
        if (job.getColours().isEmpty() && job.getUnit() == Unit.PAIR && request.side() == null) {
            throw new BadRequestException("Side (left/right/pair) is required for this job.");
        }
        if (attendanceRecordRepository.findByDateAndEmployee_Id(request.date(), employee.getId()).isEmpty()) {
            throw new BadRequestException(employee.getName() + " has no attendance recorded for " + request.date() + ".");
        }

        Routing routing = routingRepository.findByJobId(job.getId())
                .orElseThrow(() -> new BadRequestException("No routing has been assigned to this job yet."));
        Map<Long, RoutingOperation> routingByOperationId = flattenRoutingOperations(routing);

        ProductionEntry entry = productionEntryRepository
                .findMergeTarget(request.date(), employee.getId(), job.getId(), request.colourId(), request.sizeId(), request.side())
                .orElse(null);
        boolean isNewEntry = entry == null;
        if (isNewEntry) {
            entry = new ProductionEntry(request.date(), employee, job, colour, size, request.side(), job.getUnit(), actor);
        } else {
            entry.setLastEditedBy(actor);
            entry.setLastEditedAt(Instant.now());
        }

        for (ProductionEntryOpRequest opReq : request.operations()) {
            Workstation workstation = workstationRepository.findById(opReq.workstationId())
                    .orElseThrow(() -> new NotFoundException("Workstation not found: " + opReq.workstationId()));
            Operation operation = operationRepository.findById(opReq.operationId())
                    .orElseThrow(() -> new NotFoundException("Operation not found: " + opReq.operationId()));
            if (!operation.getWorkstation().getId().equals(workstation.getId())) {
                throw new BadRequestException(
                        "Operation \"" + operation.getName() + "\" does not belong to workstation \"" + workstation.getName() + "\".");
            }
            if (!employee.getAuthorizedWorkstations().isEmpty()
                    && !employee.getAuthorizedWorkstations().contains(workstation.getCode())) {
                throw new BadRequestException(employee.getName() + " is not authorized for workstation \"" + workstation.getName() + "\".");
            }

            RoutingOperation routingOp = routingByOperationId.get(operation.getId());
            if (routingOp == null) {
                throw new BadRequestException(
                        "Operation \"" + operation.getName() + "\" is not part of this job's routing.");
            }
            if (operationClosureRepository.findMatch(job.getId(), request.colourId(), request.sizeId(),
                    workstation.getId(), operation.getId()).isPresent()) {
                throw new BadRequestException("Operation \"" + operation.getName() + "\" has been closed for this job/colour/size.");
            }

            int available = computeAvailable(job, colour, size, request.side(), routingOp);
            if (opReq.quantity() > available) {
                throw new BadRequestException(
                        "Only " + available + " pcs available to enter for \"" + operation.getName() + "\".");
            }

            ProductionEntryOp existingLine = findLine(entry, workstation.getId(), operation.getId());
            if (existingLine != null) {
                existingLine.setQuantity(existingLine.getQuantity() + opReq.quantity());
            } else {
                entry.getOperations().add(new ProductionEntryOp(entry, workstation, operation, opReq.quantity(), job.getUnit()));
            }

            // A production entry logged at a "Quality"-named workstation drops straight
            // into the QC queue as a pending record — see production package-info.
            if (workstation.getName().toLowerCase().contains(QUALITY_WORKSTATION_MARKER)) {
                qcEntryRepository.save(new QcEntry(request.date(), job, colour, size, request.side(), job.getUnit(),
                        operation.getName(), workstation, opReq.quantity(), QcStatus.PENDING_DETAILS, true, actor));
            }
        }

        return ProductionEntryDto.from(productionEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public List<ProductionEntryDto> listForDate(java.time.LocalDate date) {
        return productionEntryRepository.findByDate(date).stream().map(ProductionEntryDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductionEntryDto> listForJob(Long jobId) {
        return productionEntryRepository.findByJobId(jobId).stream().map(ProductionEntryDto::from).toList();
    }

    // Applied once an edit request is approved: wipes every prior entry for this
    // (job, colour, size) — across all dates/employees/sides — and replaces them
    // with a single consolidated entry, bypassing routing/dependency validation
    // since this is a correction, not new floor output. See ProductionEditRequest.
    @Transactional
    public ProductionEntryDto replaceForApprovedEdit(ProductionEditRequest editRequest, ProductionEntryRequest request,
                                                      User actor) {
        Job job = editRequest.getJob();
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new NotFoundException("Employee not found: " + request.employeeId()));
        JobColour colour = request.colourId() != null ? jobColourRepository.findById(request.colourId())
                .orElseThrow(() -> new NotFoundException("Job colour not found: " + request.colourId())) : null;
        JobSize size = request.sizeId() != null ? jobSizeRepository.findById(request.sizeId())
                .orElseThrow(() -> new NotFoundException("Job size not found: " + request.sizeId())) : null;

        List<ProductionEntry> priorEntries =
                productionEntryRepository.findAllForJobColourSize(job.getId(), request.colourId(), request.sizeId());
        productionEntryRepository.deleteAll(priorEntries);
        productionEntryRepository.flush();

        ProductionEntry newEntry = new ProductionEntry(request.date(), employee, job, colour, size, request.side(),
                job.getUnit(), actor);
        newEntry.setApprovedEdit(true);
        newEntry.setApprovedEditRequest(editRequest);

        for (ProductionEntryOpRequest opReq : request.operations()) {
            Workstation workstation = workstationRepository.findById(opReq.workstationId())
                    .orElseThrow(() -> new NotFoundException("Workstation not found: " + opReq.workstationId()));
            Operation operation = operationRepository.findById(opReq.operationId())
                    .orElseThrow(() -> new NotFoundException("Operation not found: " + opReq.operationId()));
            newEntry.getOperations().add(new ProductionEntryOp(newEntry, workstation, operation, opReq.quantity(), job.getUnit()));
        }

        return ProductionEntryDto.from(productionEntryRepository.save(newEntry));
    }

    // Remaining pieces this routing step can accept for a (job, colour, size, side)
    // combo. With no dependencies it's capped by the planned quantity; with
    // dependencies it's capped by the slowest (minimum) upstream step's output —
    // a closed upstream step contributes its frozen doneQtyAtClosure instead of
    // its live entry total. See production package-info for the source algorithm.
    int computeAvailable(Job job, JobColour colour, JobSize size, Side side, RoutingOperation targetOp) {
        Long colourId = colour != null ? colour.getId() : null;
        Long sizeId = size != null ? size.getId() : null;
        int doneAtTarget = doneAt(job.getId(), colourId, sizeId, side, targetOp.getOperation().getId());

        if (targetOp.getDependsOn().isEmpty()) {
            int planned = size != null ? size.getPlannedQty() : job.getTotalPlannedQty();
            return Math.max(planned - doneAtTarget, 0);
        }

        int availableFromDeps = targetOp.getDependsOn().stream()
                .mapToInt(dep -> effectiveDone(job.getId(), colourId, sizeId, side, dep))
                .min().orElse(0);
        return Math.max(availableFromDeps - doneAtTarget, 0);
    }

    private int effectiveDone(Long jobId, Long colourId, Long sizeId, Side side, RoutingOperation dependency) {
        Long wstId = dependency.getRoutingWorkstation().getWorkstation().getId();
        Long opId = dependency.getOperation().getId();
        return operationClosureRepository.findMatch(jobId, colourId, sizeId, wstId, opId)
                .map(OperationClosure::getDoneQtyAtClosure)
                .orElseGet(() -> doneAt(jobId, colourId, sizeId, side, opId));
    }

    private int doneAt(Long jobId, Long colourId, Long sizeId, Side side, Long operationId) {
        return productionEntryRepository.sumQuantityForOperation(jobId, colourId, sizeId, side, operationId);
    }

    private ProductionEntryOp findLine(ProductionEntry entry, Long workstationId, Long operationId) {
        return entry.getOperations().stream()
                .filter(op -> op.getWorkstation().getId().equals(workstationId) && op.getOperation().getId().equals(operationId))
                .findFirst().orElse(null);
    }

    private Map<Long, RoutingOperation> flattenRoutingOperations(Routing routing) {
        Map<Long, RoutingOperation> map = new HashMap<>();
        for (RoutingWorkstation rw : routing.getWorkstations()) {
            for (RoutingOperation ro : rw.getOperations()) {
                map.put(ro.getOperation().getId(), ro);
            }
        }
        return map;
    }
}
