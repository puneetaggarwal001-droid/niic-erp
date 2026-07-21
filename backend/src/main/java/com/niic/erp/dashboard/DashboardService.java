package com.niic.erp.dashboard;

import com.niic.erp.attendance.AttendanceRecordRepository;
import com.niic.erp.attendance.EmployeeRepository;
import com.niic.erp.attendance.GatePassRepository;
import com.niic.erp.common.RequestStatus;
import com.niic.erp.payroll.PayrollRun;
import com.niic.erp.payroll.PayrollRunRepository;
import com.niic.erp.production.ChallanStatus;
import com.niic.erp.production.JobRepository;
import com.niic.erp.production.JobRequestRepository;
import com.niic.erp.production.ProductionEditRequestRepository;
import com.niic.erp.production.ProductionEntry;
import com.niic.erp.production.ProductionEntryOp;
import com.niic.erp.production.ProductionEntryRepository;
import com.niic.erp.production.QcEntryRepository;
import com.niic.erp.production.QcStatus;
import com.niic.erp.production.RoutingChangeRequestRepository;
import com.niic.erp.production.TransferChallanRepository;
import com.niic.erp.sampling.Sample;
import com.niic.erp.sampling.SampleRepository;
import com.niic.erp.sampling.SampleRequestRepository;
import com.niic.erp.sampling.SampleStatus;
import com.niic.erp.store.MaterialRequisitionRepository;
import com.niic.erp.store.PoStatus;
import com.niic.erp.store.PurchaseOrderRepository;
import com.niic.erp.store.RequisitionStatus;
import com.niic.erp.store.StoreService;
import com.niic.erp.store.dto.ItemDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aggregates cross-module KPIs for the management dashboard. */
@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final GatePassRepository gatePassRepository;
    private final JobRepository jobRepository;
    private final ProductionEntryRepository productionEntryRepository;
    private final JobRequestRepository jobRequestRepository;
    private final ProductionEditRequestRepository editRequestRepository;
    private final RoutingChangeRequestRepository routingRequestRepository;
    private final QcEntryRepository qcEntryRepository;
    private final TransferChallanRepository transferRepository;
    private final StoreService storeService;
    private final PurchaseOrderRepository poRepository;
    private final MaterialRequisitionRepository requisitionRepository;
    private final SampleRepository sampleRepository;
    private final SampleRequestRepository sampleRequestRepository;
    private final PayrollRunRepository payrollRunRepository;

    public DashboardService(EmployeeRepository employeeRepository, AttendanceRecordRepository attendanceRepository,
                            GatePassRepository gatePassRepository, JobRepository jobRepository,
                            ProductionEntryRepository productionEntryRepository, JobRequestRepository jobRequestRepository,
                            ProductionEditRequestRepository editRequestRepository,
                            RoutingChangeRequestRepository routingRequestRepository, QcEntryRepository qcEntryRepository,
                            TransferChallanRepository transferRepository, StoreService storeService,
                            PurchaseOrderRepository poRepository, MaterialRequisitionRepository requisitionRepository,
                            SampleRepository sampleRepository, SampleRequestRepository sampleRequestRepository,
                            PayrollRunRepository payrollRunRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.gatePassRepository = gatePassRepository;
        this.jobRepository = jobRepository;
        this.productionEntryRepository = productionEntryRepository;
        this.jobRequestRepository = jobRequestRepository;
        this.editRequestRepository = editRequestRepository;
        this.routingRequestRepository = routingRequestRepository;
        this.qcEntryRepository = qcEntryRepository;
        this.transferRepository = transferRepository;
        this.storeService = storeService;
        this.poRepository = poRepository;
        this.requisitionRepository = requisitionRepository;
        this.sampleRepository = sampleRepository;
        this.sampleRequestRepository = sampleRequestRepository;
        this.payrollRunRepository = payrollRunRepository;
    }

    @Transactional(readOnly = true)
    public DashboardDto summary() {
        LocalDate today = LocalDate.now();
        return new DashboardDto(hr(today), production(today), store(), sampling(), payroll());
    }

    private DashboardDto.Hr hr(LocalDate today) {
        long total = employeeRepository.count();
        long active = employeeRepository.findByActiveTrue().size();
        long present = attendanceRepository.findByDate(today).size();
        long gatePasses = gatePassRepository.findByDateOrderByCreatedAtDesc(today).size();
        return new DashboardDto.Hr(total, active, present, gatePasses);
    }

    private DashboardDto.Production production(LocalDate today) {
        long activeJobs = jobRepository.findByActiveTrue().size();
        long unitsToday = productionEntryRepository.findByDate(today).stream()
                .map(ProductionEntry::getOperations)
                .flatMap(List::stream)
                .mapToLong(ProductionEntryOp::getQuantity)
                .sum();
        long jobReq = jobRequestRepository.findByStatus(RequestStatus.PENDING).size();
        long editReq = editRequestRepository.findByStatus(RequestStatus.PENDING).size();
        long routingReq = routingRequestRepository.findByStatus(RequestStatus.PENDING).size();
        long qc = qcEntryRepository.findByStatus(QcStatus.PENDING_DETAILS).size();
        long transfers = transferRepository.findByStatus(ChallanStatus.PENDING).size();
        return new DashboardDto.Production(activeJobs, unitsToday, jobReq, editReq, routingReq, qc, transfers);
    }

    private DashboardDto.Store store() {
        List<ItemDto> items = storeService.listItems(null);
        long total = items.size();
        long belowReorder = items.stream().filter(ItemDto::belowReorder).count();
        long pendingApprovals = storeService.pendingApprovals().size();
        long openPos = poRepository.findByOrderByCreatedAtDesc().stream()
                .filter(p -> p.getStatus() == PoStatus.PENDING || p.getStatus() == PoStatus.ORDERED)
                .count();
        long pendingReqs = requisitionRepository.findByOrderByCreatedAtDesc().stream()
                .filter(r -> r.getStatus() != RequisitionStatus.FULFILLED)
                .count();
        return new DashboardDto.Store(total, pendingApprovals, belowReorder, openPos, pendingReqs);
    }

    private DashboardDto.Sampling sampling() {
        List<Sample> samples = sampleRepository.findAll();
        Map<String, Long> pipeline = new TreeMap<>();
        for (SampleStatus st : SampleStatus.values()) {
            pipeline.put(st.name(), 0L);
        }
        pipeline.putAll(samples.stream()
                .collect(Collectors.groupingBy(s -> s.getStatus().name(), Collectors.counting())));
        long active = samples.stream()
                .filter(s -> s.getStatus() != SampleStatus.CLOSED && s.getStatus() != SampleStatus.REJECTED)
                .count();
        long pendingReq = sampleRequestRepository.findByOrderByCreatedAtDesc().stream()
                .filter(r -> r.getStatus() == com.niic.erp.sampling.RequestStatus.PENDING
                        || r.getStatus() == com.niic.erp.sampling.RequestStatus.IN_PROGRESS)
                .count();
        return new DashboardDto.Sampling(pipeline, active, pendingReq);
    }

    private DashboardDto.Payroll payroll() {
        List<PayrollRun> runs = payrollRunRepository.findByOrderByPeriodMonthDesc();
        if (runs.isEmpty()) {
            return new DashboardDto.Payroll(null, null);
        }
        PayrollRun latest = runs.get(0);
        return new DashboardDto.Payroll(latest.getPeriodMonth(), latest.getStatus().name());
    }
}
