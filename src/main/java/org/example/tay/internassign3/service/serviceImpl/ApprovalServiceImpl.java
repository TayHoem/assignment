package org.example.tay.internassign3.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.example.tay.internassign3.dto.request.ApprovalRequestDTO;
import org.example.tay.internassign3.dto.response.ApprovalResponseDTO;
import org.example.tay.internassign3.entity.Approval;
import org.example.tay.internassign3.entity.Claim;
import org.example.tay.internassign3.entityEnum.ApprovalStatus;
import org.example.tay.internassign3.entityEnum.ClaimStatus;
import org.example.tay.internassign3.exception.ConflictException;
import org.example.tay.internassign3.exception.ResourceNotFoundException;
import org.example.tay.internassign3.mapper.ApprovalMapper;
import org.example.tay.internassign3.repository.ApprovalRepository;
import org.example.tay.internassign3.repository.ClaimRepository;
import org.example.tay.internassign3.service.ApprovalService;
import org.example.tay.internassign3.service.ClaimService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final ClaimRepository claimRepository;
    private final ClaimService claimService;
    private final ApprovalMapper approvalMapper;

    @Override
    public ApprovalResponseDTO createApproval(String employeeId, String claimId, ApprovalRequestDTO request) {
        // Implementation logic to create an approval
        // This would typically involve validating the employee and claim,
        // creating an Approval entity, saving it to the repository, and returning a response DTO.

        log.debug("createApproval - start");
        // Placeholder for actual implementation
        Claim claim = claimService.getClaimEntityById(claimId);

        if (!claim.getEmployeeSnapshot().getId().toHexString().equals(employeeId)) {
            log.error("Claim does not belong to employee: {}", employeeId);
            throw new ConflictException("Claim does not belong to the specified employee: " + employeeId);
        }

        //Check if claim is already approved or rejected
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new ConflictException("Claim already processed with status: " + claim.getStatus());
        }
        if (claim.getItems().isEmpty()) {
            throw new ConflictException("Cannot approve a claim with no items");
        }

        ApprovalStatus approvalStatus = approvalMapper.toApprovalStatus(request.getStatus());

        // Create and save the approval
        Approval approval = Approval.builder()
                .claimId(new ObjectId(claimId))
                .approverId(request.getApproverId())
                .status(approvalMapper.toApprovalStatus(request.getStatus()))
                .comments(request.getComments())
                .build();

        Approval savedApproval = approvalRepository.save(approval);

        if (approvalStatus == ApprovalStatus.APPROVED) {
            claim.setStatus(ClaimStatus.APPROVED);
        } else {
            claim.setStatus(ClaimStatus.REJECTED);
        }

        claimRepository.save(claim);

        log.debug("createApproval - end");
        return approvalMapper.toResponse(savedApproval);
    }

    @Override
    public ApprovalResponseDTO findApprovalById(String Id) {
        log.debug("findApprovalById {}", Id);
        return approvalRepository.findById(new ObjectId(Id))
                .map(approvalMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found with id" + Id));
    }
}
