package org.example.tay.internassign3.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tay.internassign3.dto.request.ApprovalRequestDTO;
import org.example.tay.internassign3.dto.response.ApprovalResponseDTO;
import org.example.tay.internassign3.dto.response.ClaimResponseDTO;
import org.example.tay.internassign3.service.ApprovalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/employees/{employeeId}/claims/{claimId}/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private ApprovalService approvalService;

    @PostMapping
    public ResponseEntity<ApprovalResponseDTO> createApproval(
            @PathVariable String employeeId,
            @PathVariable String claimId,
            @RequestBody ApprovalRequestDTO approvalRequestDTO
    ) {
        log.info("Creating approval for employeeId: {}, claimId: {}", employeeId, claimId);
        ApprovalResponseDTO response = approvalService.createApproval(employeeId, claimId, approvalRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
