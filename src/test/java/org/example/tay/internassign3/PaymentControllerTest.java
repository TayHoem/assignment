package org.example.tay.internassign3;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tay.internassign3.controller.PaymentController;
import org.example.tay.internassign3.dto.request.PaymentRequestDTO;
import org.example.tay.internassign3.dto.response.PaymentResponseDTO;
import org.example.tay.internassign3.exception.ConflictException;
import org.example.tay.internassign3.exception.GlobalExceptionHandler;
import org.example.tay.internassign3.exception.ResourceNotFoundException;
import org.example.tay.internassign3.service.PaymentService;
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
@WebMvcTest(controllers = {PaymentController.class, GlobalExceptionHandler.class})

@DisplayName("PaymentController Integration Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    private static final String EMPLOYEE_ID = "507f1f77bcf86cd799439011";
    private static final String CLAIM_ID    = "507f1f77bcf86cd799439022";
    private static final String PAYMENT_ID  = "507f1f77bcf86cd799439077";
    private static final String BASE_URL    =
            "/api/employees/" + EMPLOYEE_ID + "/claims/" + CLAIM_ID + "/payments";

    private PaymentResponseDTO paymentResponse;

    @BeforeEach
    void setUp() {
        paymentResponse = PaymentResponseDTO.builder()
                .id(PAYMENT_ID)
                .claimId(CLAIM_ID)
                .employeeNumber("EMP001")
                .paymentMethod("BANK_TRANSFER")
                .paymentAmount(new BigDecimal("100.00"))
                .status("PENDING")
                .createdDate(LocalDateTime.now())
                .build();
    }

    // ─────────────────────────────────────────────
    // POST /api/employees/{employeeId}/claims/{claimId}/payments
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/employees/{employeeId}/claims/{claimId}/payments")
    class CreatePayment {

        @Test
        @DisplayName("should return 200 when payment is created successfully for approved claim")
        void createPayment_success() throws Exception {
            // Given
            PaymentRequestDTO request = PaymentRequestDTO.builder()
                    .paymentMethod("BANK_TRANSFER")
                    .build();
            given(paymentService.createPayment(eq(EMPLOYEE_ID), eq(CLAIM_ID), any(PaymentRequestDTO.class)))
                    .willReturn(paymentResponse);

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PAYMENT_ID))
                    .andExpect(jsonPath("$.paymentMethod").value("BANK_TRANSFER"))
                    .andExpect(jsonPath("$.paymentAmount").value(100.00))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("should return 409 when claim is not approved")
        void createPayment_claimNotApproved_returns409() throws Exception {
            // Given
            PaymentRequestDTO request = PaymentRequestDTO.builder()
                    .paymentMethod("BANK_TRANSFER")
                    .build();
            given(paymentService.createPayment(eq(EMPLOYEE_ID), eq(CLAIM_ID), any(PaymentRequestDTO.class)))
                    .willThrow(new ConflictException(
                            "Payment can only be created for APPROVED claims. Current status: PENDING"));

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(
                            "Payment can only be created for APPROVED claims. Current status: PENDING"));
        }

        @Test
        @DisplayName("should return 409 when payment already exists for claim")
        void createPayment_duplicate_returns409() throws Exception {
            // Given
            PaymentRequestDTO request = PaymentRequestDTO.builder()
                    .paymentMethod("BANK_TRANSFER")
                    .build();
            given(paymentService.createPayment(eq(EMPLOYEE_ID), eq(CLAIM_ID), any(PaymentRequestDTO.class)))
                    .willThrow(new ConflictException("Payment already exists for claim: " + CLAIM_ID));

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("should return 400 when paymentMethod is blank")
        void createPayment_missingPaymentMethod_returns400() throws Exception {
            // Given – paymentMethod is @NotBlank
            PaymentRequestDTO invalid = PaymentRequestDTO.builder().build();

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 404 when claim not found")
        void createPayment_claimNotFound_returns404() throws Exception {
            // Given
            PaymentRequestDTO request = PaymentRequestDTO.builder()
                    .paymentMethod("BANK_TRANSFER")
                    .build();
            given(paymentService.createPayment(eq(EMPLOYEE_ID), eq(CLAIM_ID), any(PaymentRequestDTO.class)))
                    .willThrow(new ResourceNotFoundException("Claim not found with id: " + CLAIM_ID));

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 409 when claim does not belong to employee")
        void createPayment_wrongEmployee_returns409() throws Exception {
            // Given
            PaymentRequestDTO request = PaymentRequestDTO.builder()
                    .paymentMethod("BANK_TRANSFER")
                    .build();
            given(paymentService.createPayment(eq(EMPLOYEE_ID), eq(CLAIM_ID), any(PaymentRequestDTO.class)))
                    .willThrow(new ConflictException("Claim does not belong to the specified employee"));

            // When / Then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Claim does not belong to the specified employee"));
        }
    }
}