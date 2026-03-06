package org.example.tay.internassign3.service;

import org.example.tay.internassign3.dto.request.ApprovalRequestDTO;
import org.example.tay.internassign3.dto.response.ApprovalResponseDTO;

public interface ApprovalService  {
    ApprovalResponseDTO createApproval(String employeeId, String claimId, ApprovalRequestDTO request);

    ApprovalResponseDTO findApprovalById(String Id);
}
