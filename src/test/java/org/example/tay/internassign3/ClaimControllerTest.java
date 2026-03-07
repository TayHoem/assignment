package org.example.tay.internassign3;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tay.internassign3.controller.ClaimController;
import org.example.tay.internassign3.dto.ClaimItemDto;
import org.example.tay.internassign3.dto.ClaimTypeDto;
import org.example.tay.internassign3.dto.request.ClaimRequestDTO;
import org.example.tay.internassign3.dto.request.UpdateClaimAmountRequest;
import org.example.tay.internassign3.dto.response.ClaimResponseDTO;
import org.example.tay.internassign3.dto.response.EmployeeResponseDTO;
import org.example.tay.internassign3.exception.ConflictException;
import org.example.tay.internassign3.exception.GlobalExceptionHandler;
import org.example.tay.internassign3.exception.ResourceNotFoundException;
import org.example.tay.internassign3.service.ClaimService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration," +
                "org.springframework.boot.mongodb.autoconfigure.MongoDataAutoConfiguration," +
                "org.springframework.boot.mongodb.autoconfigure.MongoRepositoriesAutoConfiguration"
})
@WebMvcTest(controllers = {ClaimController.class, GlobalExceptionHandler.class})

@DisplayName("ClaimController Integration Tests")
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClaimService claimService;

    private static final String EMPLOYEE_ID = "507f1f77bcf86cd799439011";
    private static final String CLAIM_ID    = "507f1f77bcf86cd799439022";
    private static final String ITEM_ID     = "507f1f77bcf86cd799439033";
    private static final String BASE_URL    = "/api/employees/" + EMPLOYEE_ID + "/claims";

    private ClaimResponseDTO claimResponse;

    @BeforeEach
    void setUp() {
        claimResponse = ClaimResponseDTO.builder()
                .id(CLAIM_ID)
                .employee(EmployeeResponseDTO.builder().id(EMPLOYEE_ID).employeeNumber("EMP001").build())
                .claimType(ClaimTypeDto.builder().typeCode("TRAVEL").name("Travel Expense").build())
                .totalAmount(new BigDecimal("100.00"))
                .items(List.of(ClaimItemDto.builder()
                        .expenseDate("2025-01-10")
                        .amount(new BigDecimal("100.00"))
                        .categoryCode("TRAVEL")
                        .build()))
                .status("PENDING")
                .build();
    }

    // ─────────────────────────────────────────────
    // POST /api/employees/{employeeId}/claims
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/employees/{employeeId}/claims")
    class CreateClaim {

        @Test
        @DisplayName("should return 201 when claim is created successfully")
        void createClaim_success() throws Exception {
            // Given
            ClaimRequestDTO request = ClaimRequestDTO.builder()
                    .claimType(ClaimTypeDto.builder().typeCode("TRAVEL").name("Travel Expense").build())
                    .claimItems(List.of(ClaimItemDto.builder()
                            .expenseDate("2025-01-10")
                            .amount(new BigDecimal("100.00"))
                            .categoryCode("TRAVEL")
                            .build()))
                    .build();

            given(claimService.createClaim(eq(EMPLOYEE_ID), any(ClaimRequestDTO.class)))
                    .willReturn(claimResponse);

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(CLAIM_ID))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.totalAmount").value(100.00));
        }

        @Test
        @DisplayName("should return 400 when claim type is missing")
        void createClaim_missingClaimType_returns400() throws Exception {
            // Given – claimType is @NotNull
            ClaimRequestDTO invalid = ClaimRequestDTO.builder().build();

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 404 when employee does not exist")
        void createClaim_employeeNotFound_returns404() throws Exception {
            // Given
            ClaimRequestDTO request = ClaimRequestDTO.builder()
                    .claimType(ClaimTypeDto.builder().typeCode("TRAVEL").name("Travel Expense").build())
                    .build();
            given(claimService.createClaim(eq(EMPLOYEE_ID), any(ClaimRequestDTO.class)))
                    .willThrow(new ResourceNotFoundException("Employee not found"));

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────
    // POST /api/employees/{employeeId}/claims/{claimId}/items
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /{claimId}/items")
    class AddItemToClaim {

        @Test
        @DisplayName("should return 201 when item is added successfully")
        void addItem_success() throws Exception {
            // Given
            ClaimItemDto itemDto = ClaimItemDto.builder()
                    .expenseDate("2025-02-01")
                    .amount(new BigDecimal("50.00"))
                    .categoryCode("MEALS")
                    .build();

            given(claimService.addItemtoClaim(eq(CLAIM_ID), any(ClaimItemDto.class)))
                    .willReturn(claimResponse);

            // When / Then
            mockMvc.perform(post(BASE_URL + "/" + CLAIM_ID + "/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(itemDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(CLAIM_ID));
        }

        @Test
        @DisplayName("should return 400 when item amount is missing")
        void addItem_missingAmount_returns400() throws Exception {
            // Given – amount is @NotNull
            ClaimItemDto invalid = ClaimItemDto.builder()
                    .expenseDate("2025-02-01")
                    .categoryCode("MEALS")
                    .build();

            // When / Then
            mockMvc.perform(post(BASE_URL + "/" + CLAIM_ID + "/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when item amount is zero or negative")
        void addItem_zeroAmount_returns400() throws Exception {
            // Given
            ClaimItemDto invalid = ClaimItemDto.builder()
                    .expenseDate("2025-02-01")
                    .amount(BigDecimal.ZERO)
                    .categoryCode("MEALS")
                    .build();

            // When / Then
            mockMvc.perform(post(BASE_URL + "/" + CLAIM_ID + "/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 404 when claim not found")
        void addItem_claimNotFound_returns404() throws Exception {
            // Given
            ClaimItemDto itemDto = ClaimItemDto.builder()
                    .expenseDate("2025-02-01")
                    .amount(new BigDecimal("50.00"))
                    .categoryCode("MEALS")
                    .build();
            given(claimService.addItemtoClaim(eq(CLAIM_ID), any(ClaimItemDto.class)))
                    .willThrow(new ResourceNotFoundException("Claim not found"));

            // When / Then
            mockMvc.perform(post(BASE_URL + "/" + CLAIM_ID + "/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(itemDto)))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────
    // PATCH /api/employees/{employeeId}/claims/{claimId}/totalAmount
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("PATCH /{claimId}/totalAmount")
    class UpdateClaimAmount {

        @Test
        @DisplayName("should return 200 when claim item amount is updated successfully")
        void updateClaimAmount_success() throws Exception {
            // Given
            UpdateClaimAmountRequest request = UpdateClaimAmountRequest.builder()
                    .itemId(ITEM_ID)
                    .amount(new BigDecimal("200.00"))
                    .build();

            ClaimResponseDTO updatedResponse = ClaimResponseDTO.builder()
                    .id(CLAIM_ID)
                    .totalAmount(new BigDecimal("200.00"))
                    .status("PENDING")
                    .build();

            given(claimService.updateClaimAmount(eq(CLAIM_ID), any(UpdateClaimAmountRequest.class)))
                    .willReturn(updatedResponse);

            // When / Then
            mockMvc.perform(patch(BASE_URL + "/" + CLAIM_ID + "/totalAmount")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalAmount").value(200.00));
        }

        @Test
        @DisplayName("should return 409 when claim is already approved")
        void updateClaimAmount_alreadyApproved_returns409() throws Exception {
            // Given
            UpdateClaimAmountRequest request = UpdateClaimAmountRequest.builder()
                    .itemId(ITEM_ID)
                    .amount(new BigDecimal("200.00"))
                    .build();
            given(claimService.updateClaimAmount(eq(CLAIM_ID), any(UpdateClaimAmountRequest.class)))
                    .willThrow(new ConflictException("Cannot modify a claim with status: APPROVED"));

            // When / Then
            mockMvc.perform(patch(BASE_URL + "/" + CLAIM_ID + "/totalAmount")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Cannot modify a claim with status: APPROVED"));
        }

        @Test
        @DisplayName("should return 400 when itemId is missing")
        void updateClaimAmount_missingItemId_returns400() throws Exception {
            // Given – itemId is @NotBlank
            UpdateClaimAmountRequest invalid = UpdateClaimAmountRequest.builder()
                    .amount(new BigDecimal("200.00"))
                    .build();

            // When / Then
            mockMvc.perform(patch(BASE_URL + "/" + CLAIM_ID + "/totalAmount")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/employees/{employeeId}/claims
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/employees/{employeeId}/claims")
    class GetAllClaims {

        @Test
        @DisplayName("should return 200 with list of claims")
        void getAllClaims_success() throws Exception {
            // Given
            given(claimService.getAllClaims()).willReturn(List.of(claimResponse));

            // When / Then
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(CLAIM_ID))
                    .andExpect(jsonPath("$[0].status").value("PENDING"));
        }
    }
}