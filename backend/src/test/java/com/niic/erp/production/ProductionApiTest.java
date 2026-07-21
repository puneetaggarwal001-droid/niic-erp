package com.niic.erp.production;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niic.erp.attendance.AttendanceRecord;
import com.niic.erp.attendance.AttendanceRecordRepository;
import com.niic.erp.attendance.Designation;
import com.niic.erp.attendance.DesignationRepository;
import com.niic.erp.attendance.Employee;
import com.niic.erp.attendance.EmployeeRepository;
import com.niic.erp.attendance.SalaryType;
import com.niic.erp.user.User;
import com.niic.erp.user.UserRepository;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

// Own in-memory DB, isolated from the attendance module's test DB — same
// reasoning as AttendanceApiTest: a fresh schema per test run, no cross-run
// state pollution.
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:production-test;DB_CLOSE_DELAY=-1",
        // Provision the admin via config so tests don't depend on a shipped default.
        "erp.admin.password=test-admin-pw"})
class ProductionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private UserRepository userRepository;

    // Real JWT via the actual login endpoint rather than @WithMockUser — the
    // controllers pull the ERP User off an AppUserPrincipal (see
    // CurrentUserProvider), which @WithMockUser's generic principal doesn't provide.
    private String jwt;

    @BeforeEach
    void login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "admin", "password", "test-admin-pw"))))
                .andExpect(status().isOk()).andReturn();
        jwt = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void fullProductionCycleEnforcesRoutingAndPlannedQty() throws Exception {
        long stitchingId = createWorkstation("Stitching");
        long sewOpId = createOperation(stitchingId, "Sew");

        String jobBody = """
                {"styleCode":"SLIP-ONS","modelNo":"M100","unit":"PCS",
                 "colours":[{"name":"Black","sizes":[{"size":"S","plannedQty":10}]}]}
                """;
        JsonNode job = postForJson("/api/production/jobs", jobBody);
        assertThat(job.get("jobDisplayId").asText()).matches("NIIC / SLIP-ONS / \\d{3}");
        long jobId = job.get("id").asLong();
        long colourId = job.get("colours").get(0).get("id").asLong();
        long sizeId = job.get("colours").get(0).get("sizes").get(0).get("id").asLong();

        // Regression check: listing jobs back must not blow up on the nested
        // colours/sizes collection (this previously threw a LazyInitializationException
        // because JobService.listAll() wasn't @Transactional).
        mockMvc.perform(get("/api/production/jobs").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());

        String routingBody = String.format(
                "{\"jobId\":%d,\"workstations\":[{\"workstationId\":%d,\"operations\":[{\"operationId\":%d}]}]}",
                jobId, stitchingId, sewOpId);
        postForJson("/api/production/routing", routingBody);

        // Same regression check for the routing/workstations/operations lazy chain.
        mockMvc.perform(get("/api/production/routing/" + jobId).header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());

        Designation designation = designationRepository.save(new Designation("Operator"));
        Employee employee = employeeRepository.save(new Employee("EMP-900", "Test Worker", "111122223333",
                "9999999999", designation, LocalDate.now(), SalaryType.SALARIED));
        User admin = userRepository.findByUsername("admin").orElseThrow();
        attendanceRecordRepository.save(new AttendanceRecord(LocalDate.now(), employee, designation, admin));

        String entryBody = String.format(
                "{\"date\":\"%s\",\"employeeId\":%d,\"jobId\":%d,\"colourId\":%d,\"sizeId\":%d,"
                        + "\"operations\":[{\"workstationId\":%d,\"operationId\":%d,\"quantity\":6}]}",
                LocalDate.now(), employee.getId(), jobId, colourId, sizeId, stitchingId, sewOpId);
        postForJson("/api/production/entries", entryBody);

        // Regression check for the ProductionEntry -> operations lazy chain.
        mockMvc.perform(get("/api/production/entries").param("jobId", String.valueOf(jobId))
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());

        // Only 4 pcs remain (planned 10 - 6 already entered) — asking for 5 must fail.
        String overEntryBody = String.format(
                "{\"date\":\"%s\",\"employeeId\":%d,\"jobId\":%d,\"colourId\":%d,\"sizeId\":%d,"
                        + "\"operations\":[{\"workstationId\":%d,\"operationId\":%d,\"quantity\":5}]}",
                LocalDate.now(), employee.getId(), jobId, colourId, sizeId, stitchingId, sewOpId);
        mockMvc.perform(authorizedPost("/api/production/entries").contentType(MediaType.APPLICATION_JSON).content(overEntryBody))
                .andExpect(status().isBadRequest());

        long packingId = createWorkstation("Packing");
        String challanBody = String.format(
                "{\"jobId\":%d,\"fromWorkstationId\":%d,\"toWorkstationId\":%d,"
                        + "\"items\":[{\"itemName\":\"Box\",\"itemUnit\":\"PCS\",\"qty\":5}]}",
                jobId, stitchingId, packingId);
        JsonNode challan = postForJson("/api/production/transfer-challans", challanBody);
        assertThat(challan.get("challanNo").asText()).matches("TC-\\d{2}-0001");

        // Regression check for the TransferChallan -> items lazy chain.
        mockMvc.perform(get("/api/production/transfer-challans").param("status", "PENDING")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());
    }

    private long createWorkstation(String name) throws Exception {
        return postForJson("/api/production/workstations", objectMapper.writeValueAsString(Map.of("name", name)))
                .get("id").asLong();
    }

    private long createOperation(long workstationId, String name) throws Exception {
        return postForJson("/api/production/operations",
                objectMapper.writeValueAsString(Map.of("workstationId", workstationId, "name", name)))
                .get("id").asLong();
    }

    private MockHttpServletRequestBuilder authorizedPost(String url) {
        return post(url).header("Authorization", "Bearer " + jwt);
    }

    private JsonNode postForJson(String url, String body) throws Exception {
        MvcResult result = mockMvc.perform(authorizedPost(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
