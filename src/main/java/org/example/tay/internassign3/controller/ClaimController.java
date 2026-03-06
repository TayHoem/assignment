package org.example.tay.internassign3.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tay.internassign3.dto.ClaimItemDto;
import org.example.tay.internassign3.dto.request.ClaimRequestDTO;
import org.example.tay.internassign3.dto.request.UpdateClaimAmountRequest;
import org.example.tay.internassign3.dto.response.ClaimResponseDTO;
import org.example.tay.internassign3.entity.Claim;
import org.example.tay.internassign3.service.ClaimService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/employees/{employeeId}/claims")
@RequiredArgsConstructor
public class ClaimController {
    private final ClaimService claimService;

    @PostMapping
    public ResponseEntity<ClaimResponseDTO> createClaim(
            @PathVariable String employeeId,
            @Valid @RequestBody ClaimRequestDTO request) {
        ClaimResponseDTO response = claimService.createClaim(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{claimId}/items")
    public ResponseEntity<ClaimResponseDTO> addItemToExistingClaim(
            @PathVariable String claimId,
            @Valid @RequestBody ClaimItemDto request
    ){
        ClaimResponseDTO response = claimService.addItemtoClaim(claimId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{claimId}/totalAmount")
    public ResponseEntity<ClaimResponseDTO> updateClaimAmount(
            @PathVariable String claimId,
            @Valid @RequestBody UpdateClaimAmountRequest request
    ){
        ClaimResponseDTO response = claimService.updateClaimAmount(claimId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ClaimResponseDTO>> getAllClaims() {
        List<ClaimResponseDTO> claims = claimService.getAllClaims();
        return ResponseEntity.ok(claims);
    }
}
