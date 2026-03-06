package org.example.tay.internassign3.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.tay.internassign3.dto.ClaimItemDto;
import org.example.tay.internassign3.dto.request.ClaimRequestDTO;
import org.example.tay.internassign3.dto.request.UpdateClaimAmountRequest;
import org.example.tay.internassign3.dto.response.ClaimResponseDTO;
import org.example.tay.internassign3.entity.Claim;
import org.example.tay.internassign3.entity.ClaimItem;
import org.example.tay.internassign3.entity.Employee;
import org.example.tay.internassign3.entityEnum.ClaimStatus;
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
import java.util.ArrayList;
import java.util.List;

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
        Employee employee = employeeService.getEmployeeEntityById(employeeId);


        List<ClaimItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        if (request.getClaimItems() != null) {
            items = request.getClaimItems().stream()
                    .map(dto -> {
                        ClaimItem item = claimMapper.toClaimItem(dto);
                        item.setId(new ObjectId());
                        return item;
                    })
                    .toList();

            total = items.stream()
                    .map(ClaimItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        Claim claim = Claim.builder()
                .employeeSnapshot(employeeMapper.toEmployeeSnapshot(employee))
                .claimType(claimMapper.toClaimType(request.getClaimType()))
                .items(items)
                .totalAmount(total)
                .status(ClaimStatus.PENDING)
                .createdDate(LocalDateTime.now())
                .lastUpdatedDate(LocalDateTime.now())
                .build();

        Claim savedClaim = claimRepository.save(claim);
        log.info("Created claim: {}", savedClaim.getId());
        return claimMapper.toResponse(savedClaim);
    }

    @Override
    public ClaimResponseDTO addItemtoClaim(String claimId, ClaimItemDto itemDto) {
        log.debug("Adding item to claim: {}", claimId);
        Claim claim = claimRepository.findById(new ObjectId(claimId))
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));

        ClaimItem item = claimMapper.toClaimItem(itemDto);
        item.setId(new ObjectId());
        claim.getItems().add(item);
        claim.setTotalAmount(claim.getTotalAmount().add(item.getAmount()));
        claim.setLastUpdatedDate(LocalDateTime.now());

        Claim updatedClaim = claimRepository.save(claim);
        log.info("Added item to claim: {}", updatedClaim.getId());
        return claimMapper.toResponse(updatedClaim);
    }

    @Override
    public ClaimResponseDTO updateClaimAmount(String claimId, UpdateClaimAmountRequest request){
        Claim claim = getClaimEntityById(claimId);

        if (ClaimStatus.APPROVED.equals(claim.getStatus()) || ClaimStatus.PAID.equals(claim.getStatus())) {
            throw new ConflictException("Cannot modify a claim with status: " + claim.getStatus());
        }

        int index = request.getItemIndex();
        if (index < 0 || index >= claim.getItems().size()) {
            throw new IllegalArgumentException("Invalid item index: " + index);
        }

        claim.getItems().get(index).setAmount(request.getAmount());
        claim.setTotalAmount(recalculateTotalAmount(claim.getItems()));

        Claim saved = claimRepository.save(claim);
        log.info("Updated item amount in claim: {}", claimId);
        return claimMapper.toResponse(saved);

    }

    @Override
    public List<ClaimResponseDTO> getAllClaims() {
        log.debug("Fetching all claims");
        return claimRepository.findAll().stream()
                .map(claimMapper::toResponse)
                .toList();
    }

    @Override
    public ClaimResponseDTO getClaimById(String claimId) {
        log.debug("Fetching claim by id: {}", claimId);
        return claimRepository.findById(new ObjectId(claimId))
                .map(claimMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
    }

    @Override
    public Claim getClaimEntityById(String claimId){
        log.debug("Fetching claim entity by id: {}", claimId);
        return claimRepository.findById(new ObjectId(claimId))
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
    }

    private BigDecimal recalculateTotalAmount(List<ClaimItem> items) {
        return items.stream()
                .map(ClaimItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

//    private Claim getClaimForEmployee(String claimId, String employeeId) {
//        return claimRepository.findByIdAndEmployeeSnapshot_Id(new ObjectId(claimId), new ObjectId(employeeId))
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Claim not found with id: " + claimId + " for employee: " + employeeId));
//    }
}
