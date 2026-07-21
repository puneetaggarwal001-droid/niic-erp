package com.niic.erp.payroll;

import com.niic.erp.payroll.dto.AdvanceDto;
import com.niic.erp.payroll.dto.AdvanceRequest;
import com.niic.erp.payroll.dto.ContractorBillDto;
import com.niic.erp.payroll.dto.ContractorBillRequest;
import com.niic.erp.payroll.dto.ContractorDto;
import com.niic.erp.payroll.dto.ContractorRequest;
import com.niic.erp.payroll.dto.PayrollRunDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payroll")
@PreAuthorize("hasRole('ADMIN')")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    // ---- Advances --------------------------------------------------------

    @GetMapping("/advances")
    public List<AdvanceDto> listAdvances(@RequestParam String month) {
        return payrollService.listAdvances(month).stream().map(AdvanceDto::from).toList();
    }

    @PostMapping("/advances")
    @ResponseStatus(HttpStatus.CREATED)
    public AdvanceDto addAdvance(@Valid @RequestBody AdvanceRequest request) {
        return AdvanceDto.from(payrollService.addAdvance(
                request.employeeId(), request.periodMonth(), request.amount(), request.reason()));
    }

    // ---- Runs ------------------------------------------------------------

    @GetMapping("/runs")
    public List<PayrollRunDto> listRuns() {
        return payrollService.listRuns().stream().map(PayrollRunDto::from).toList();
    }

    @GetMapping("/runs/{id}")
    public PayrollRunDto getRun(@PathVariable Long id) {
        return PayrollRunDto.from(payrollService.getRun(id));
    }

    @PostMapping("/runs")
    @ResponseStatus(HttpStatus.CREATED)
    public PayrollRunDto generate(@RequestParam String month) {
        return PayrollRunDto.from(payrollService.generateDraft(month));
    }

    @PostMapping("/runs/{id}/finalize")
    public PayrollRunDto finalizeRun(@PathVariable Long id) {
        return PayrollRunDto.from(payrollService.finalizeRun(id));
    }

    // ---- Contractors -----------------------------------------------------

    @GetMapping("/contractors")
    public List<ContractorDto> listContractors() {
        return payrollService.listContractors().stream().map(ContractorDto::from).toList();
    }

    @PostMapping("/contractors")
    @ResponseStatus(HttpStatus.CREATED)
    public ContractorDto addContractor(@Valid @RequestBody ContractorRequest request) {
        return ContractorDto.from(payrollService.addContractor(request.name(), request.phone()));
    }

    @DeleteMapping("/contractors/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateContractor(@PathVariable Long id) {
        payrollService.deactivateContractor(id);
    }

    // ---- Contractor bills ------------------------------------------------

    @GetMapping("/bills")
    public List<ContractorBillDto> listBills(@RequestParam String month) {
        return payrollService.listBills(month).stream().map(ContractorBillDto::from).toList();
    }

    @PostMapping("/bills")
    @ResponseStatus(HttpStatus.CREATED)
    public ContractorBillDto createBill(@Valid @RequestBody ContractorBillRequest request) {
        return ContractorBillDto.from(payrollService.createBill(
                request.contractorId(), request.periodMonth(), request.rateType(),
                request.quantity(), request.rate(), request.advances(), request.notes()));
    }

    @PostMapping("/bills/{id}/finalize")
    public ContractorBillDto finalizeBill(@PathVariable Long id) {
        return ContractorBillDto.from(payrollService.finalizeBill(id));
    }
}
