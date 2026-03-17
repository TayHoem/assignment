package org.example.tay.internassign3.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tay.internassign3.dto.request.ClaimRequestDTO;
import org.example.tay.internassign3.dto.response.ClaimResponseDTO;
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
        log.info("createClaim request");
        ClaimResponseDTO response = claimService.createClaim(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{claimId}")
    public ResponseEntity<ClaimResponseDTO> updateClaim(
            @PathVariable String claimId,
            @Valid @RequestBody ClaimRequestDTO request
    ){
        log.info("updateClaim request");
        ClaimResponseDTO claim = claimService.cliamUpdate(claimId, request);
        return ResponseEntity.ok(claim);
    }

    @GetMapping
    public ResponseEntity<List<ClaimResponseDTO>> getAllClaims() {
        log.info("getAllClaims request");
        List<ClaimResponseDTO> claims = claimService.getAllClaims();
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimResponseDTO> getClaimById(@PathVariable String claimId) {
        log.info("getClaimById request");
        ClaimResponseDTO claim = claimService.getClaimById(claimId);
        return ResponseEntity.ok(claim);
    }

    @GetMapping("/employee/{employeeNumber}")
    public ResponseEntity<List<ClaimResponseDTO>> getClaimsByEmployeeSnapShotEmployeeNumber(String employeeNumber) {
        log.info("getClaimsByEmployeeSnapShotEmployeeNumber request");
        List<ClaimResponseDTO> claimResponseDTOList = claimService.getClaimByEmployeeSnapShotEmployeeNumber(employeeNumber);
        return ResponseEntity.ok(claimResponseDTOList);
    }
}
