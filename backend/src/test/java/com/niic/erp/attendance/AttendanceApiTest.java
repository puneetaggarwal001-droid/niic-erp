package com.niic.erp.attendance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

// In-memory DB instead of the dev profile's file DB — each test run needs a
// clean schema, not whatever the last `mvn spring-boot:run` left on disk.
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:erp-test;DB_CLOSE_DELAY=-1",
        // Keep the admin-bootstrap password identical to AuthApiTest so the shared
        // erp-test context stays consistent.
        "erp.admin.password=test-admin-pw"})
class AttendanceApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDesignationThenListsIt() throws Exception {
        mockMvc.perform(post("/api/designations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Stitching Operator"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/designations")).andExpect(status().isOk());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
    }
}
