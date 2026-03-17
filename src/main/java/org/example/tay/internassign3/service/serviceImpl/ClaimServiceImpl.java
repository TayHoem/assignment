package org.example.tay.internassign3.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.tay.internassign3.dto.ClaimItemDto;
import org.example.tay.internassign3.dto.request.ClaimRequestDTO;
import org.example.tay.internassign3.dto.response.ClaimResponseDTO;
import org.example.tay.internassign3.entity.Claim;
import org.example.tay.internassign3.entity.ClaimItem;
import org.example.tay.internassign3.entity.Employee;
import org.example.tay.internassign3.entityEnum.ClaimStatus;
import org.example.tay.internassign3.exception.BadRequestException;
import org.example.tay.internassign3.exception.ConflictException;
import org.example.tay.internassign3.exception.ResourceNotFoundException;
import org.example.tay.internassign3.mapper.ClaimMapper;
import org.example.tay.internassign3.mapper.EmployeeMapper;
import org.example.tay.internassign3.repository.ClaimRepository;
import org.example.tay.internassign3.service.ClaimService;
import org.example.tay.internassign3.service.EmployeeService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;
    private final ClaimMapper claimMapper;

    @Override
    public ClaimResponseDTO createClaim(String employeeId, ClaimRequestDTO request) {
        log.debug("Creating claim for employee: {}", employeeId);
        if (request == null){
            throw new BadRequestException("Create Claim Request cannot be null");
        }
        Employee employee = employeeService.getEmployeeEntityById(employeeId);

        // 1. Fetch ALL historical claims for this employee/type
        List<Claim> existingClaims = claimRepository
                .findAllByEmployeeIdAndClaimTypeCode(
                        new ObjectId(employeeId),
                        request.getClaimType().getTypeCode()
                );

        // 2. Advanced Duplicate Check
        if (request.getClaimItems() != null && !request.getClaimItems().isEmpty()) {

            // Create unique keys for incoming items: "Date | Category | Amount"
            Set<String> incomingKeys = request.getClaimItems().stream()
                    .map(item -> item.getExpenseDate() + "|" + item.getCategoryCode() + "|" + item.getAmount())
                    .collect(Collectors.toSet());

            for (Claim existingClaim : existingClaims) {
                for (ClaimItem existingItem : existingClaim.getItems()) {
                    String existingKey = existingItem.getExpenseDate() + "|" +
                            existingItem.getCategoryCode() + "|" +
                            existingItem.getAmount();

                    if (incomingKeys.contains(existingKey)) {
                        // Customize the message based on the status of the duplicate
                        String statusMsg = switch (existingClaim.getStatus()) {
                            case PENDING -> "is currently under review.";
                            case APPROVED, PAID -> "has already been approved/paid.";
                            case REJECTED -> "was previously rejected. Please contact Finance if you need to resubmit.";
                            default -> "already exists.";
                        };

                        throw new ConflictException(
                                String.format("Duplicate Item Found: An expense for %s on %s for amount %s %s",
                                        existingItem.getCategoryCode(),
                                        existingItem.getExpenseDate(),
                                        existingItem.getAmount(),
                                        statusMsg)
                        );
                    }
                }
            }
        }

        // 3. Calculation and Mapping (Rest of your code remains the same)
        List<ClaimItem> items = request.getClaimItems().stream()
                .map(dto -> {
                    ClaimItem item = claimMapper.toClaimItem(dto);
                    item.setId(new ObjectId());
                    return item;
                }).toList();

        BigDecimal total = items.stream()
                .map(ClaimItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Claim claim = Claim.builder()
                .employeeSnapshot(employeeMapper.toEmployeeSnapshot(employee))
                .claimType(claimMapper.toClaimType(request.getClaimType()))
                .items(items)
                .totalAmount(total)
                .status(ClaimStatus.PENDING)
                .createdDate(LocalDateTime.now())
                .lastUpdatedDate(LocalDateTime.now())
                .build();

        return claimMapper.toResponse(claimRepository.save(claim));
    }

    @Override
    public ClaimResponseDTO cliamUpdate(String claimId, ClaimRequestDTO requestDTO){
        log.debug("Updating claim: {}", claimId);
        if (requestDTO == null){
            throw new BadRequestException("Update Claim Request cannot be null");
        }
        // 1. Find the existing claim
        Claim claim = claimRepository.findById(new ObjectId(claimId))
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        // 2. Security Check: Ensure the claim type and name haven't changed if they are required to stay the same
        // (Optional: You can add logic here to update typeCode/Name if allowed)
        if (requestDTO.getClaimType().getTypeCode() != null) claim.getClaimType().setTypeCode(requestDTO.getClaimType().getTypeCode());
        if (requestDTO.getClaimType().getName() != null) claim.getClaimType().setName(requestDTO.getClaimType().getName());

        // 3. Update items based on Index (0 to 0, 1 to 1, etc.)
        if (requestDTO.getClaimItems() != null && !requestDTO.getClaimItems().isEmpty()) {
            List<ClaimItem> existingItems = claim.getItems();
            List<ClaimItemDto> newItems = requestDTO.getClaimItems();

            for (int i = 0; i < newItems.size(); i++) {
                // Only update if the existing list actually has an item at this index
                if (i < existingItems.size()) {
                    ClaimItem existingItem = existingItems.get(i);
                    ClaimItemDto requestItem = newItems.get(i);

                    existingItem.setExpenseDate(LocalDate.parse(requestItem.getExpenseDate()));
                    existingItem.setCategoryCode(requestItem.getCategoryCode());
                    existingItem.setAmount(requestItem.getAmount());
                } else {
                    // If request has MORE items than existing, add them as new items
                    log.info("Adding new item at index {}", i);
                    existingItems.add(claimMapper.toClaimItem(newItems.get(i)));
                }
            }
        }

        // 4. Recalculate Total Amount
        BigDecimal newTotalAmount = claim.getItems().stream()
                .map(ClaimItem::getAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        claim.setTotalAmount(newTotalAmount);

        // 5. Save back to DB
        Claim savedClaim = claimRepository.save(claim);
        log.info("Claim updated successfully. New Total Amount: {}", newTotalAmount);

        return claimMapper.toResponse(savedClaim);

    }

    @Override
    public List<ClaimResponseDTO> getAllClaims() {
        log.debug("Fetching all claims");
        return claimRepository.findAll().stream()
                .map(claimMapper::toResponse)
                .toList();
    }

    @Override
    public List<ClaimResponseDTO> getClaimByEmployeeSnapShotEmployeeNumber(String employeeNumber){
        log.info("Fetching claims by employee number {}", employeeNumber);
        return claimRepository.findByEmployeeSnapshot_EmployeeNumber(employeeNumber).stream()
                .map(claimMapper::toResponse)
                .toList();
    }

    @Override
    public ClaimResponseDTO getClaimById(String claimId) {
        log.debug("Fetching claim by id: {}", claimId);
        return claimRepository.findById(new ObjectId(claimId))
                .map(claimMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));
    }

    @Override
    public Claim getClaimEntityById(String claimId){
        log.debug("Fetching claim entity by id: {}", claimId);
        return claimRepository.findById(new ObjectId(claimId))
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));
    }

    private BigDecimal recalculateTotalAmount(List<ClaimItem> items) {
        return items.stream()
                .map(ClaimItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
