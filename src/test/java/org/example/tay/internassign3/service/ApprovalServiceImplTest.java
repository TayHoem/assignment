package org.example.tay.internassign3.service;

import org.bson.types.ObjectId;
import org.example.tay.internassign3.dto.request.ApprovalRequestDTO;
import org.example.tay.internassign3.dto.response.ApprovalResponseDTO;
import org.example.tay.internassign3.entity.*;
import org.example.tay.internassign3.entityEnum.ApprovalStatus;
import org.example.tay.internassign3.entityEnum.ClaimStatus;
import org.example.tay.internassign3.exception.ConflictException;
import org.example.tay.internassign3.exception.ResourceNotFoundException;
import org.example.tay.internassign3.mapper.ApprovalMapper;
import org.example.tay.internassign3.repository.ApprovalRepository;
import org.example.tay.internassign3.repository.ClaimRepository;
import org.example.tay.internassign3.service.serviceImpl.ApprovalServiceImpl;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalServiceImpl Unit Tests")
class ApprovalServiceImplTest {

    @Mock private ApprovalRepository approvalRepository;
    @Mock private ClaimRepository claimRepository;
    @Mock private ClaimService claimService;
    @Mock private EmployeeService employeeService;
    @Mock private ApprovalMapper approvalMapper;

    @InjectMocks
    private ApprovalServiceImpl approvalService;


    private ObjectId claimObjectId;
    private String claimId;
    private ObjectId employeeObjectId;
    private String employeeId;
    private ObjectId approvalObjectId;
    private String approvalId;
    private Claim pendingClaim;
    private Approval savedApproval;
    private ApprovalResponseDTO approvalResponse;
    private Employee approver;

    @BeforeEach
    void setUp() {
        claimObjectId    = new ObjectId();
        claimId          = claimObjectId.toHexString();
        employeeObjectId = new ObjectId();
        employeeId       = employeeObjectId.toHexString();
        approvalObjectId = new ObjectId();
        approvalId       = approvalObjectId.toHexString();


        EmployeeSnapshot snap = EmployeeSnapshot.builder()
                .id(employeeObjectId)
                .employeeNumber("EMP001")
                .firstName("Alice")
                .build();

         approver = Employee.builder()
                .id(approvalObjectId)
                .employeeNumber("AP001")
                .firstName("John")
                .build();

        ClaimItem item = ClaimItem.builder()
                .id(new ObjectId())
                .expenseDate(LocalDate.now())
                .amount(new BigDecimal("100.00"))
                .categoryCode("TRAVEL")
                .build();

        pendingClaim = Claim.builder()
                .id(claimObjectId)
                .employeeSnapshot(snap)
                .items(List.of(item))
                .totalAmount(new BigDecimal("100.00"))
                .status(ClaimStatus.PENDING)
                .build();

        savedApproval = Approval.builder()
                .id(new ObjectId())
                .claimId(claimObjectId)
                .approverId(new ObjectId(approvalId))
                .status(ApprovalStatus.APPROVED)
                .actionDate(LocalDateTime.now())
                .build();

        approvalResponse = ApprovalResponseDTO.builder()
                .id(savedApproval.getId().toHexString())
                .claimId(claimId)
                .approverId(approvalId)
                .status("APPROVED")
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // createApproval
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createApproval")
    class CreateApproval {

        @Test
        @DisplayName("should create APPROVED approval and update claim status to APPROVED")
        void getRequest_CreateApprovalAndSetClaimApproved() {
            // Given
            ApprovalRequestDTO request = ApprovalRequestDTO.builder()
                    .approverId(approvalId)
                    .status("APPROVED")
                    .comments("Looks good")
                    .build();

            given(employeeService.getEmployeeEntityById(approvalId)).willReturn(approver); // ← ADD THIS
            given(claimService.getClaimEntityById(claimId)).willReturn(pendingClaim);
            given(approvalMapper.toApprovalStatus("APPROVED")).willReturn(ApprovalStatus.APPROVED);
            given(approvalRepository.save(any(Approval.class))).willReturn(savedApproval);
            given(claimRepository.save(any(Claim.class))).willReturn(pendingClaim);
            given(approvalMapper.toResponse(savedApproval)).willReturn(approvalResponse);

            // When
            ApprovalResponseDTO result = approvalService.createApproval(employeeId, claimId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("APPROVED");
            then(claimRepository).should().save(argThat(c -> c.getStatus() == ClaimStatus.APPROVED));
        }

        @Test
        @DisplayName("should create REJECTED approval and update claim status to REJECTED")
        void sendRequest_CreateApprovalAndSetClaimRejected() {
            // Given
            ApprovalRequestDTO request = ApprovalRequestDTO.builder()
                    .approverId(approvalId)
                    .status("REJECTED")
                    .comments("Missing receipts")
                    .build();

            Approval rejectedApproval = Approval.builder()
                    .id(new ObjectId())
                    .claimId(claimObjectId)
                    .status(ApprovalStatus.REJECTED)
                    .build();

            ApprovalResponseDTO rejectedResponse = ApprovalResponseDTO.builder()
                    .status("REJECTED").build();

            given(employeeService.getEmployeeEntityById(approvalId)).willReturn(approver); // ← ADD THIS
            given(claimService.getClaimEntityById(claimId)).willReturn(pendingClaim);
            given(approvalMapper.toApprovalStatus("REJECTED")).willReturn(ApprovalStatus.REJECTED);
            given(approvalRepository.save(any(Approval.class))).willReturn(rejectedApproval);
            given(claimRepository.save(any(Claim.class))).willReturn(pendingClaim);
            given(approvalMapper.toResponse(rejectedApproval)).willReturn(rejectedResponse);

            // When
            ApprovalResponseDTO result = approvalService.createApproval(employeeId, claimId, request);

            // Then
            assertThat(result.getStatus()).isEqualTo("REJECTED");
            then(claimRepository).should().save(argThat(c -> c.getStatus() == ClaimStatus.REJECTED));
        }

        @Test
        @DisplayName("should throw ConflictException when claim does not belong to the employee")
        void sendRequest_createApproval_ThrowConflictWhenClaimNotOwnedByEmployee() {
            // Given
            String differentEmployeeId = new ObjectId().toHexString();
            ApprovalRequestDTO request = ApprovalRequestDTO.builder()
                    .approverId(approvalId).status("APPROVED").build();

            given(employeeService.getEmployeeEntityById(approvalId)).willReturn(approver); // ← ADD THIS
            given(claimService.getClaimEntityById(claimId)).willReturn(pendingClaim);

            // When / Then
            assertThatThrownBy(() -> approvalService.createApproval(differentEmployeeId, claimId, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining(differentEmployeeId);
        }

        @Test
        @DisplayName("should throw ConflictException when claim is already APPROVED")
        void sendRequest_CreateApproval_ThrowConflictWhenClaimAlreadyApproved() {
            // Given
            pendingClaim.setStatus(ClaimStatus.APPROVED);
            ApprovalRequestDTO request = ApprovalRequestDTO.builder()
                    .approverId(approvalId).status("APPROVED").build();

            given(employeeService.getEmployeeEntityById(approvalId)).willReturn(approver); // ← ADD THIS
            given(claimService.getClaimEntityById(claimId)).willReturn(pendingClaim);

            // When / Then
            assertThatThrownBy(() -> approvalService.createApproval(employeeId, claimId, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("APPROVED");
        }

        @Test
        @DisplayName("should throw ConflictException when claim has no items")
        void sendRequest_createApproval_ThrowConflictWhenClaimHasNoItems() {
            // Given
            pendingClaim.setItems(List.of());
            ApprovalRequestDTO request = ApprovalRequestDTO.builder()
                    .approverId(approvalId).status("APPROVED").build();

            given(employeeService.getEmployeeEntityById(approvalId)).willReturn(approver); // ← ADD THIS
            given(claimService.getClaimEntityById(claimId)).willReturn(pendingClaim);

            // When / Then
            assertThatThrownBy(() -> approvalService.createApproval(employeeId, claimId, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("no items");
        }

        // ──  approver-not-found logic ──────────────
        @Test
        @DisplayName("should throw ResourceNotFoundException when approverId does not exist")
        void sendRequest_createApproval_ThrowNotFoundWhenApproverNotFound() {
            // Given
            ApprovalRequestDTO request = ApprovalRequestDTO.builder()
                    .approverId(approvalId).status("APPROVED").build();

            given(employeeService.getEmployeeEntityById(approvalId))
                    .willThrow(new ResourceNotFoundException("Approver not found"));

            // When / Then
            assertThatThrownBy(() -> approvalService.createApproval(employeeId, claimId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Approver not found");

            // claimService should never be reached
            then(claimService).should(never()).getClaimEntityById(any());
        }

        // ── Self-approval guard ───────────────────────
        @Test
        @DisplayName("should throw ConflictException when approver is the same as the claim employee")
        void sendRequest_createApproval_ThrowConflictWhenSelfApproval() {
            // Given — approverId == employeeId (self-approval)
            ApprovalRequestDTO request = ApprovalRequestDTO.builder()
                    .approverId(employeeId)   // same as the claim owner
                    .status("APPROVED").build();

            Employee claimOwnerAsApprover = Employee.builder()
                    .id(employeeObjectId)
                    .employeeNumber("EMP001")
                    .build();

            given(employeeService.getEmployeeEntityById(employeeId)).willReturn(claimOwnerAsApprover);
            given(claimService.getClaimEntityById(claimId)).willReturn(pendingClaim);

            // When / Then
            assertThatThrownBy(() -> approvalService.createApproval(employeeId, claimId, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining(employeeId);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // findApprovalById
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findApprovalById")
    class FindApprovalById {

        @Test
        @DisplayName("should return ApprovalResponseDTO when approval found")
        void sendRequest_getApproval_shouldReturnApprovalWhenFound() {
            // Given
            String approvalId = savedApproval.getId().toHexString();
            given(approvalRepository.findById(savedApproval.getId())).willReturn(Optional.of(savedApproval));
            given(approvalMapper.toResponse(savedApproval)).willReturn(approvalResponse);

            // When
            ApprovalResponseDTO result = approvalService.findApprovalById(approvalId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getClaimId()).isEqualTo(claimId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when approval not found")
        void sendRequest_getApproval_shouldThrowWhenApprovalNotFound() {
            // Given
            String nonExistentId = new ObjectId().toHexString();
            given(approvalRepository.findById(any(ObjectId.class))).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> approvalService.findApprovalById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
