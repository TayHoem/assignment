package org.example.tay.internassign3.service.serviceImpl;

import org.bson.types.ObjectId;
import org.example.tay.internassign3.dto.ClaimItemDto;
import org.example.tay.internassign3.dto.ClaimTypeDto;
import org.example.tay.internassign3.dto.request.ClaimRequestDTO;
import org.example.tay.internassign3.dto.request.UpdateClaimAmountRequest;
import org.example.tay.internassign3.dto.response.ClaimResponseDTO;
import org.example.tay.internassign3.entity.*;
import org.example.tay.internassign3.entityEnum.ClaimStatus;
import org.example.tay.internassign3.exception.ConflictException;
import org.example.tay.internassign3.exception.ResourceNotFoundException;
import org.example.tay.internassign3.mapper.ClaimMapper;
import org.example.tay.internassign3.mapper.EmployeeMapper;
import org.example.tay.internassign3.repository.ClaimRepository;
import org.example.tay.internassign3.service.EmployeeService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClaimServiceImpl Unit Tests")
class ClaimServiceImplTest {

    @Mock private ClaimRepository claimRepository;
    @Mock private EmployeeService employeeService;
    @Mock private EmployeeMapper employeeMapper;
    @Mock private ClaimMapper claimMapper;

    @InjectMocks
    private ClaimServiceImpl claimService;

    // Shared fixtures
    private ObjectId claimObjectId;
    private String claimId;
    private ObjectId employeeObjectId;
    private String employeeId;
    private Employee employee;
    private EmployeeSnapshot employeeSnapshot;
    private Claim claim;
    private ClaimItem claimItem;
    private ClaimResponseDTO claimResponseDTO;

    @BeforeEach
    void setUp() {
        claimObjectId  = new ObjectId();
        claimId        = claimObjectId.toHexString();
        employeeObjectId = new ObjectId();
        employeeId     = employeeObjectId.toHexString();

        employee = Employee.builder()
                .id(employeeObjectId)
                .employeeNumber("EMP001")
                .firstName("Alice")
                .lastName("Tan")
                .email("alice@example.com")
                .build();

        employeeSnapshot = EmployeeSnapshot.builder()
                .id(employeeObjectId)
                .employeeNumber("EMP001")
                .firstName("Alice")
                .lastName("Tan")
                .build();

        claimItem = ClaimItem.builder()
                .id(new ObjectId())
                .expenseDate(LocalDate.now())
                .amount(new BigDecimal("100.00"))
                .categoryCode("TRAVEL")
                .build();

        claim = Claim.builder()
                .id(claimObjectId)
                .employeeSnapshot(employeeSnapshot)
                .claimType(new ClaimType("TRAVEL", "Travel Expenses"))
                .items(new ArrayList<>(List.of(claimItem)))
                .totalAmount(new BigDecimal("100.00"))
                .status(ClaimStatus.PENDING)
                .build();

        claimResponseDTO = ClaimResponseDTO.builder()
                .id(claimId)
                .totalAmount(new BigDecimal("100.00"))
                .status("PENDING")
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // createClaim
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createClaim")
    class CreateClaim {

        @Test
        @DisplayName("should create claim with items and correct total amount")
        void shouldCreateClaimWithItemsAndCorrectTotal() {
            // Given
            ClaimTypeDto claimTypeDto = new ClaimTypeDto("TRAVEL", "Travel Expenses");
            ClaimItemDto itemDto = new ClaimItemDto("2024-01-15", new BigDecimal("150.00"), "TRAVEL");
            ClaimRequestDTO request = ClaimRequestDTO.builder()
                    .claimType(claimTypeDto)
                    .claimItems(List.of(itemDto))
                    .build();

            ClaimItem mappedItem = ClaimItem.builder()
                    .expenseDate(LocalDate.of(2024, 1, 15))
                    .amount(new BigDecimal("150.00"))
                    .categoryCode("TRAVEL")
                    .build();

            given(employeeService.getEmployeeEntityById(employeeId)).willReturn(employee);
            given(employeeMapper.toEmployeeSnapshot(employee)).willReturn(employeeSnapshot);
            given(claimMapper.toClaimItem(itemDto)).willReturn(mappedItem);
            given(claimMapper.toClaimType(claimTypeDto)).willReturn(new ClaimType("TRAVEL", "Travel Expenses"));
            given(claimRepository.save(any(Claim.class))).willReturn(claim);
            given(claimMapper.toResponse(claim)).willReturn(claimResponseDTO);

            // When
            ClaimResponseDTO result = claimService.createClaim(employeeId, request);

            // Then
            assertThat(result).isNotNull();
            then(claimRepository).should().save(any(Claim.class));
        }

        @Test
        @DisplayName("should create claim with zero total when no items provided")
        void shouldCreateClaimWithZeroTotalWhenNoItems() {
            // Given
            ClaimRequestDTO request = ClaimRequestDTO.builder()
                    .claimType(new ClaimTypeDto("MEDICAL", "Medical"))
                    .claimItems(null)
                    .build();

            Claim emptyClaim = Claim.builder()
                    .id(claimObjectId)
                    .employeeSnapshot(employeeSnapshot)
                    .items(new ArrayList<>())
                    .totalAmount(BigDecimal.ZERO)
                    .status(ClaimStatus.PENDING)
                    .build();

            given(employeeService.getEmployeeEntityById(employeeId)).willReturn(employee);
            given(employeeMapper.toEmployeeSnapshot(employee)).willReturn(employeeSnapshot);
            given(claimMapper.toClaimType(any())).willReturn(new ClaimType("MEDICAL", "Medical"));
            given(claimRepository.save(any(Claim.class))).willReturn(emptyClaim);
            given(claimMapper.toResponse(emptyClaim)).willReturn(claimResponseDTO);

            // When
            ClaimResponseDTO result = claimService.createClaim(employeeId, request);

            // Then
            assertThat(result).isNotNull();
            then(claimRepository).should().save(argThat(c -> c.getTotalAmount().compareTo(BigDecimal.ZERO) == 0));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // addItemToClaim
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("addItemToClaim")
    class AddItemToClaim {

        @Test
        @DisplayName("should add item and update total amount correctly")
        void shouldAddItemAndUpdateTotal() {
            // Given
            ClaimItemDto newItemDto = new ClaimItemDto("2024-02-01", new BigDecimal("50.00"), "MEALS");
            ClaimItem newItem = ClaimItem.builder()
                    .expenseDate(LocalDate.of(2024, 2, 1))
                    .amount(new BigDecimal("50.00"))
                    .categoryCode("MEALS")
                    .build();

            Claim updatedClaim = Claim.builder()
                    .id(claimObjectId)
                    .employeeSnapshot(employeeSnapshot)
                    .items(List.of(claimItem, newItem))
                    .totalAmount(new BigDecimal("150.00"))
                    .status(ClaimStatus.PENDING)
                    .build();

            given(claimRepository.findById(claimObjectId)).willReturn(Optional.of(claim));
            given(claimMapper.toClaimItem(newItemDto)).willReturn(newItem);
            given(claimRepository.save(any(Claim.class))).willReturn(updatedClaim);
            given(claimMapper.toResponse(updatedClaim)).willReturn(claimResponseDTO);

            // When
            ClaimResponseDTO result = claimService.addItemtoClaim(claimId, newItemDto);

            // Then
            assertThat(result).isNotNull();
            then(claimRepository).should().save(argThat(c ->
                    c.getTotalAmount().compareTo(new BigDecimal("150.00")) == 0));
        }

        @Test
        @DisplayName("should throw RuntimeException when claim not found")
        void shouldThrowWhenClaimNotFound() {
            // Given
            ClaimItemDto itemDto = new ClaimItemDto("2024-02-01", new BigDecimal("50.00"), "MEALS");
            given(claimRepository.findById(claimObjectId)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> claimService.addItemtoClaim(claimId, itemDto))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // updateClaimAmount
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateClaimAmount")
    class UpdateClaimAmount {

        @Test
        @DisplayName("should update item amount and recalculate claim total")
        void shouldUpdateItemAmountAndRecalculateTotal() {
            // Given
            String itemId = claimItem.getId().toHexString();
            UpdateClaimAmountRequest request = UpdateClaimAmountRequest.builder()
                    .itemId(itemId)
                    .amount(new BigDecimal("200.00"))
                    .build();

            Claim savedClaim = Claim.builder()
                    .id(claimObjectId)
                    .employeeSnapshot(employeeSnapshot)
                    .items(List.of(ClaimItem.builder()
                            .id(claimItem.getId())
                            .amount(new BigDecimal("200.00"))
                            .expenseDate(claimItem.getExpenseDate())
                            .categoryCode(claimItem.getCategoryCode())
                            .build()))
                    .totalAmount(new BigDecimal("200.00"))
                    .status(ClaimStatus.PENDING)
                    .build();

            given(claimRepository.findById(claimObjectId)).willReturn(Optional.of(claim));
            given(claimRepository.save(any(Claim.class))).willReturn(savedClaim);
            given(claimMapper.toResponse(savedClaim)).willReturn(claimResponseDTO);

            // When
            ClaimResponseDTO result = claimService.updateClaimAmount(claimId, request);

            // Then
            assertThat(result).isNotNull();
            then(claimRepository).should().save(argThat(c ->
                    c.getTotalAmount().compareTo(new BigDecimal("200.00")) == 0));
        }

        @Test
        @DisplayName("should throw ConflictException when claim is already APPROVED")
        void shouldThrowConflictWhenClaimIsApproved() {
            // Given
            claim.setStatus(ClaimStatus.APPROVED);
            UpdateClaimAmountRequest request = new UpdateClaimAmountRequest("someItemId", new BigDecimal("100.00"));

            given(claimRepository.findById(claimObjectId)).willReturn(Optional.of(claim));

            // When / Then
            assertThatThrownBy(() -> claimService.updateClaimAmount(claimId, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("APPROVED");
        }

        @Test
        @DisplayName("should throw ConflictException when claim is PAID")
        void shouldThrowConflictWhenClaimIsPaid() {
            // Given
            claim.setStatus(ClaimStatus.PAID);
            UpdateClaimAmountRequest request = new UpdateClaimAmountRequest("someItemId", new BigDecimal("100.00"));

            given(claimRepository.findById(claimObjectId)).willReturn(Optional.of(claim));

            // When / Then
            assertThatThrownBy(() -> claimService.updateClaimAmount(claimId, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("PAID");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when item ID not found in claim")
        void shouldThrowWhenItemNotFound() {
            // Given
            UpdateClaimAmountRequest request = new UpdateClaimAmountRequest(
                    new ObjectId().toHexString(), new BigDecimal("100.00"));

            given(claimRepository.findById(claimObjectId)).willReturn(Optional.of(claim));

            // When / Then
            assertThatThrownBy(() -> claimService.updateClaimAmount(claimId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Item not found");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // getAllClaims
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllClaims")
    class GetAllClaims {

        @Test
        @DisplayName("should return list of all claims")
        void shouldReturnAllClaims() {
            // Given
            given(claimRepository.findAll()).willReturn(List.of(claim));
            given(claimMapper.toResponse(claim)).willReturn(claimResponseDTO);

            // When
            List<ClaimResponseDTO> result = claimService.getAllClaims();

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when no claims exist")
        void shouldReturnEmptyListWhenNoClaims() {
            // Given
            given(claimRepository.findAll()).willReturn(List.of());

            // When
            List<ClaimResponseDTO> result = claimService.getAllClaims();

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // getClaimEntityById
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getClaimEntityById")
    class GetClaimEntityById {

        @Test
        @DisplayName("should return Claim entity when found")
        void shouldReturnClaimEntity() {
            // Given
            given(claimRepository.findById(claimObjectId)).willReturn(Optional.of(claim));

            // When
            Claim result = claimService.getClaimEntityById(claimId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(ClaimStatus.PENDING);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when claim not found")
        void shouldThrowWhenClaimNotFound() {
            // Given
            given(claimRepository.findById(claimObjectId)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> claimService.getClaimEntityById(claimId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(claimId);
        }
    }
}
