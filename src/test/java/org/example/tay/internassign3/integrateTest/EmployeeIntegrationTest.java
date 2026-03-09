package org.example.tay.internassign3.integrateTest;

import org.example.tay.internassign3.controller.EmployeeController;
import org.example.tay.internassign3.dto.request.EmployeeRequestDTO;
import org.example.tay.internassign3.dto.response.EmployeeResponseDTO;
import org.example.tay.internassign3.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmployeeController.class)
class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void createEmployee_shouldReturnEmployee() throws Exception {

        EmployeeRequestDTO request = new EmployeeRequestDTO();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("alice@test.com");

        EmployeeResponseDTO response = new EmployeeResponseDTO();
        response.setId("507f1f77bcf86cd799439011");
        request.setFirstName("Alice");
        request.setLastName("Smith");
        response.setEmail("alice@test.com");

        Mockito.when(employeeService.createEmployee(Mockito.any())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("507f1f77bcf86cd799439011"))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@test.com"));
    }

}
