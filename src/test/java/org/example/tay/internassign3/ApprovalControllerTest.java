package org.example.tay.internassign3;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tay.internassign3.controller.ApprovalController;
import org.example.tay.internassign3.dto.request.ApprovalRequestDTO;
import org.example.tay.internassign3.dto.response.ApprovalResponseDTO;
import org.example.tay.internassign3.exception.ConflictException;
import org.example.tay.internassign3.exception.GlobalExceptionHandler;
import org.example.tay.internassign3.exception.ResourceNotFoundException;
import org.example.tay.internassign3.service.ApprovalService;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration," +
                "org.springframework.boot.mongodb.autoconfigure.MongoDataAutoConfiguration," +
                "org.springframework.boot.mongodb.autoconfigure.MongoRepositoriesAutoConfiguration"
})
@WebMvcTest(controllers = {ApprovalController.class, GlobalExceptionHandler.class})

@DisplayName("ApprovalController Integration Tests")
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApprovalService approvalService;

    private static final String EMPLOYEE_ID = "507f1f77bcf86cd799439011";
    private static final String CLAIM_ID    = "507f1f77bcf86cd799439022";
    private static final String APPROVER_ID = "507f1f77bcf86cd799439099";
    private static final String APPROVAL_ID = "507f1f77bcf86cd799439088";
    private static final String BASE_URL =
            "/api/employees/" + EMPLOYEE_ID + "/claims/" + CLAIM_ID + "/approvals";

    private ApprovalResponseDTO approvalResponse;

    @BeforeEach
    void setUp() {
        approvalResponse = ApprovalResponseDTO.builder()
                .id(APPROVAL_ID)
                .claimId(CLAIM_ID)
                .approverId(APPROVER_ID)
                .status("APPROVED")
                .comments("Looks good")
                .actionDate(LocalDateTime.now())
                .build();
    }

    // ─────────────────────────────────────────────
    // POST /api/employees/{employeeId}/claims/{claimId}/approvals
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/employees/{employeeId}/claims/{claimId}/approvals")
    class CreateApproval {

        @Test
        @DisplayName("should return 201 when approval is created successfully")
        void createApproval_success() throws Exception {
            // Given
            ApprovalRequestDTO request = ApprovalRequestDTO.builder()
                    .approverId(APPROVER_ID)
                    .status("APPROVED")
                    .comments("Looks good")
                    .build();
            given(approvalService.createApproval(eq(EMPLOYEE_ID), eq(CLAIM_ID), any(ApprovalRequestDTO.class)))
                    .willReturn(approvalResponse);

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(APPROVAL_ID))
                    .andExpect(jsonPath("$.status").value("APPROVED"))
                    .andExpect(jsonPath("$.claimId").value(CLAIM_ID));
        }

        @Test
        @DisplayName("should return 409 when claim is already processed")
        void createApproval_alreadyProcessed_returns409() throws Exception {
            // Given
            ApprovalRequestDTO request = ApprovalRequestDTO.builder()
                    .approverId(APPROVER_ID)
                    .status("APPROVED")
                    .build();
            given(approvalService.createApproval(eq(EMPLOYEE_ID), eq(CLAIM_ID), any(ApprovalRequestDTO.class)))
                    .willThrow(new ConflictException("Claim already processed with status: APPROVED"));

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Claim already processed with status: APPROVED"));
        }

        @Test
        @DisplayName("should return 400 when approverId is blank")
        void createApproval_missingApproverId_returns400() throws Exception {
            // Given – approverId is @NotBlank
            ApprovalRequestDTO invalid = ApprovalRequestDTO.builder()
                    .status("APPROVED")
                    .build();

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when status value is not APPROVED or REJECTED")
        void createApproval_invalidStatus_returns400() throws Exception {
            // Given – @Pattern constraint on status
            ApprovalRequestDTO invalid = ApprovalRequestDTO.builder()
                    .approverId(APPROVER_ID)
                    .status("PENDING")
                    .build();

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 404 when claim not found")
        void createApproval_claimNotFound_returns404() throws Exception {
            // Given
            ApprovalRequestDTO request = ApprovalRequestDTO.builder()
                    .approverId(APPROVER_ID)
                    .status("APPROVED")
                    .build();
            given(approvalService.createApproval(eq(EMPLOYEE_ID), eq(CLAIM_ID), any(ApprovalRequestDTO.class)))
                    .willThrow(new ResourceNotFoundException("Claim not found"));

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}