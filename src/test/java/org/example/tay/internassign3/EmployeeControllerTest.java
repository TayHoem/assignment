package org.example.tay.internassign3;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tay.internassign3.controller.EmployeeController;
import org.example.tay.internassign3.dto.request.EmployeeRequestDTO;
import org.example.tay.internassign3.dto.response.EmployeeResponseDTO;
import org.example.tay.internassign3.exception.ConflictException;
import org.example.tay.internassign3.exception.GlobalExceptionHandler;
import org.example.tay.internassign3.exception.ResourceNotFoundException;
import org.example.tay.internassign3.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration," +
                "org.springframework.boot.mongodb.autoconfigure.MongoDataAutoConfiguration," +
                "org.springframework.boot.mongodb.autoconfigure.MongoRepositoriesAutoConfiguration"
})
@WebMvcTest(controllers = {EmployeeController.class, GlobalExceptionHandler.class})

@DisplayName("EmployeeController Integration Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    private static final String BASE_URL = "/api/employees";
    private static final String EMPLOYEE_ID = "507f1f77bcf86cd799439011";

    private EmployeeRequestDTO validRequest;
    private EmployeeResponseDTO employeeResponse;

    @BeforeEach
    void setUp() {
        validRequest = EmployeeRequestDTO.builder()
                .employeeNumber("EMP001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        employeeResponse = EmployeeResponseDTO.builder()
                .id(EMPLOYEE_ID)
                .employeeNumber("EMP001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
    }

    // ─────────────────────────────────────────────
    // GET /api/employees
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/employees")
    class GetAllEmployees {

        @Test
        @DisplayName("should return 200 with list of employees")
        void getAllEmployees_success() throws Exception {
            // Given
            given(employeeService.findAllEmployees()).willReturn(List.of(employeeResponse));

            // When / Then
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].employeeNumber").value("EMP001"))
                    .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/employees/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/employees/{id}")
    class GetEmployeeById {

        @Test
        @DisplayName("should return 200 with employee when found")
        void getById_success() throws Exception {
            // Given
            given(employeeService.findById(EMPLOYEE_ID)).willReturn(employeeResponse);

            // When / Then
            mockMvc.perform(get(BASE_URL + "/" + EMPLOYEE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(EMPLOYEE_ID))
                    .andExpect(jsonPath("$.employeeNumber").value("EMP001"));
        }

        @Test
        @DisplayName("should return 404 when employee not found")
        void getById_notFound_returns404() throws Exception {
            // Given
            given(employeeService.findById(EMPLOYEE_ID))
                    .willThrow(new ResourceNotFoundException("Employee not found with id: " + EMPLOYEE_ID));

            // When / Then
            mockMvc.perform(get(BASE_URL + "/" + EMPLOYEE_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Employee not found with id: " + EMPLOYEE_ID));
        }
    }

    // ─────────────────────────────────────────────
    // POST /api/employees
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/employees")
    class CreateEmployee {

        @Test
        @DisplayName("should return 201 when employee created successfully")
        void createEmployee_success() throws Exception {
            // Given
            given(employeeService.createEmployee(any(EmployeeRequestDTO.class))).willReturn(employeeResponse);

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.employeeNumber").value("EMP001"));
        }

        @Test
        @DisplayName("should return 400 when required fields are missing")
        void createEmployee_missingFields_returns400() throws Exception {
            // Given – empty request body, all @NotBlank fields missing
            EmployeeRequestDTO invalidRequest = EmployeeRequestDTO.builder().build();

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 400 when email format is invalid")
        void createEmployee_invalidEmail_returns400() throws Exception {
            // Given
            EmployeeRequestDTO badEmail = EmployeeRequestDTO.builder()
                    .employeeNumber("EMP002")
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("not-an-email")
                    .build();

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badEmail)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 409 when employee number already exists")
        void createEmployee_duplicateEmployeeNumber_returns409() throws Exception {
            // Given
            given(employeeService.createEmployee(any(EmployeeRequestDTO.class)))
                    .willThrow(new ConflictException("Employee number already exists: EMP001"));

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Employee number already exists: EMP001"));
        }
    }

    // ─────────────────────────────────────────────
    // PUT /api/employees/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/employees/{id}")
    class UpdateEmployee {

        @Test
        @DisplayName("should return 200 when employee updated successfully")
        void updateEmployee_success() throws Exception {
            // Given
            given(employeeService.updateEmployee(eq(EMPLOYEE_ID), any(EmployeeRequestDTO.class)))
                    .willReturn(employeeResponse);

            // When / Then
            mockMvc.perform(put(BASE_URL + "/" + EMPLOYEE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.employeeNumber").value("EMP001"));
        }

        @Test
        @DisplayName("should return 400 when update body is invalid")
        void updateEmployee_invalidBody_returns400() throws Exception {
            // Given – missing required fields
            EmployeeRequestDTO invalid = EmployeeRequestDTO.builder().build();

            // When / Then
            mockMvc.perform(put(BASE_URL + "/" + EMPLOYEE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE /api/employees/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/employees/{id}")
    class DeleteEmployee {

        @Test
        @DisplayName("should return 204 when employee deleted successfully")
        void deleteEmployee_success() throws Exception {
            // Given
            willDoNothing().given(employeeService).deleteEmployee(EMPLOYEE_ID);

            // When / Then
            mockMvc.perform(delete(BASE_URL + "/" + EMPLOYEE_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when employee to delete not found")
        void deleteEmployee_notFound_returns404() throws Exception {
            // Given
            given(employeeService.findById(EMPLOYEE_ID))
                    .willThrow(new ResourceNotFoundException("Employee not found with id: " + EMPLOYEE_ID));

            // When / Then
            mockMvc.perform(get(BASE_URL + "/" + EMPLOYEE_ID))
                    .andExpect(status().isNotFound());
        }
    }
}