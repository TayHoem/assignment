package org.example.tay.internassign3;

import org.bson.types.ObjectId;
import org.example.tay.internassign3.dto.request.PaymentRequestDTO;
import org.example.tay.internassign3.dto.response.PaymentResponseDTO;
import org.example.tay.internassign3.entity.Claim;
import org.example.tay.internassign3.entity.ClaimItem;
import org.example.tay.internassign3.entity.EmployeeSnapshot;
import org.example.tay.internassign3.entity.Payment;
import org.example.tay.internassign3.entityEnum.ClaimStatus;
import org.example.tay.internassign3.entityEnum.PaymentStatus;
import org.example.tay.internassign3.exception.ConflictException;
import org.example.tay.internassign3.mapper.PaymentMapper;
import org.example.tay.internassign3.repository.PaymentRepository;
import org.example.tay.internassign3.service.ClaimService;
import org.example.tay.internassign3.service.serviceImpl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl Unit Tests")
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private ClaimService claimService;
    @Mock private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private ObjectId claimObjectId;
    private String claimId;
    private ObjectId employeeObjectId;
    private String employeeId;
    private Claim approvedClaim;
    private Payment savedPayment;
    private PaymentResponseDTO paymentResponse;

    @BeforeEach
    void setUp() {
        claimObjectId    = new ObjectId();
        claimId          = claimObjectId.toHexString();
        employeeObjectId = new ObjectId();
        employeeId       = employeeObjectId.toHexString();

        EmployeeSnapshot snap = EmployeeSnapshot.builder()
                .id(employeeObjectId)
                .employeeNumber("EMP001")
                .build();

        ClaimItem item = ClaimItem.builder()
                .id(new ObjectId())
                .expenseDate(LocalDate.now())
                .amount(new BigDecimal("250.00"))
                .categoryCode("TRAVEL")
                .build();

        approvedClaim = Claim.builder()
                .id(claimObjectId)
                .employeeSnapshot(snap)
                .items(List.of(item))
                .totalAmount(new BigDecimal("250.00"))
                .status(ClaimStatus.APPROVED)
                .build();

        savedPayment = Payment.builder()
                .id(new ObjectId())
                .claimId(claimObjectId)
                .employeeNumber("EMP001")
                .paymentMethod("BANK_TRANSFER")
                .paymentAmount(new BigDecimal("250.00"))
                .status(PaymentStatus.PENDING)
                .createdDate(LocalDateTime.now())
                .build();

        paymentResponse = PaymentResponseDTO.builder()
                .id(savedPayment.getId().toHexString())
                .claimId(claimId)
                .employeeNumber("EMP001")
                .paymentMethod("BANK_TRANSFER")
                .paymentAmount(new BigDecimal("250.00"))
                .status("PENDING")
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // createPayment
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createPayment")
    class CreatePayment {

        @Test
        @DisplayName("should create payment successfully for an approved claim")
        void shouldCreatePaymentForApprovedClaim() {
            // Given
            PaymentRequestDTO request = new PaymentRequestDTO("BANK_TRANSFER");

            given(claimService.getClaimEntityById(claimId)).willReturn(approvedClaim);
            given(paymentRepository.existsByClaimId(claimObjectId)).willReturn(false);
            given(paymentRepository.save(any(Payment.class))).willReturn(savedPayment);
            given(paymentMapper.toResponse(savedPayment)).willReturn(paymentResponse);

            // When
            PaymentResponseDTO result = paymentService.createPayment(employeeId, claimId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPaymentAmount()).isEqualByComparingTo("250.00");
            assertThat(result.getPaymentMethod()).isEqualTo("BANK_TRANSFER");
            then(paymentRepository).should().save(argThat(p ->
                    p.getStatus() == PaymentStatus.PENDING &&
                    p.getPaymentAmount().compareTo(new BigDecimal("250.00")) == 0));
        }

        @Test
        @DisplayName("should throw ConflictException when claim does not belong to employee")
        void shouldThrowWhenClaimNotOwnedByEmployee() {
            // Given
            String otherEmployeeId = new ObjectId().toHexString();
            PaymentRequestDTO request = new PaymentRequestDTO("BANK_TRANSFER");

            given(claimService.getClaimEntityById(claimId)).willReturn(approvedClaim);

            // When / Then
            assertThatThrownBy(() -> paymentService.createPayment(otherEmployeeId, claimId, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("does not belong");
        }

        @Test
        @DisplayName("should throw ConflictException when claim is PENDING (not yet approved)")
        void shouldThrowWhenClaimIsPending() {
            // Given
            approvedClaim.setStatus(ClaimStatus.PENDING);
            PaymentRequestDTO request = new PaymentRequestDTO("BANK_TRANSFER");

            given(claimService.getClaimEntityById(claimId)).willReturn(approvedClaim);

            // When / Then
            assertThatThrownBy(() -> paymentService.createPayment(employeeId, claimId, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("APPROVED")
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("should throw ConflictException when claim is REJECTED")
        void shouldThrowWhenClaimIsRejected() {
            // Given
            approvedClaim.setStatus(ClaimStatus.REJECTED);
            PaymentRequestDTO request = new PaymentRequestDTO("BANK_TRANSFER");

            given(claimService.getClaimEntityById(claimId)).willReturn(approvedClaim);

            // When / Then
            assertThatThrownBy(() -> paymentService.createPayment(employeeId, claimId, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("REJECTED");
        }

        @Test
        @DisplayName("should throw ConflictException when payment already exists for claim")
        void shouldThrowWhenDuplicatePayment() {
            // Given
            PaymentRequestDTO request = new PaymentRequestDTO("BANK_TRANSFER");

            given(claimService.getClaimEntityById(claimId)).willReturn(approvedClaim);
            given(paymentRepository.existsByClaimId(claimObjectId)).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> paymentService.createPayment(employeeId, claimId, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already exists");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // getPayment
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getPayment")
    class GetPayment {

        @Test
        @DisplayName("should return list of all payments")
        void shouldReturnAllPayments() {
            // Given
            given(paymentRepository.findAll()).willReturn(List.of(savedPayment));
            given(paymentMapper.toResponse(savedPayment)).willReturn(paymentResponse);

            // When
            var result = paymentService.getPayment();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmployeeNumber()).isEqualTo("EMP001");
        }

        @Test
        @DisplayName("should return empty list when no payments exist")
        void shouldReturnEmptyList() {
            // Given
            given(paymentRepository.findAll()).willReturn(List.of());

            // When
            var result = paymentService.getPayment();

            // Then
            assertThat(result).isEmpty();
        }
    }
}
