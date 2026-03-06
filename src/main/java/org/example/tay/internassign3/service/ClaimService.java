package org.example.tay.internassign3.service;

import org.example.tay.internassign3.dto.ClaimItemDto;
import org.example.tay.internassign3.dto.request.ClaimRequestDTO;
import org.example.tay.internassign3.dto.request.UpdateClaimAmountRequest;
import org.example.tay.internassign3.dto.response.ClaimResponseDTO;
import org.example.tay.internassign3.entity.Claim;
import org.example.tay.internassign3.entity.ClaimItem;

import java.lang.String;
import java.math.BigDecimal;
import java.util.List;


public interface ClaimService {

    ClaimResponseDTO createClaim(String employeeId, ClaimRequestDTO request);

    ClaimResponseDTO addItemtoClaim(String claimId, ClaimItemDto itemDto);

    ClaimResponseDTO getClaimById(String claimId);

    Claim getClaimEntityById(String claimId);

    List<ClaimResponseDTO> getAllClaims();

    ClaimResponseDTO updateClaimAmount(String claimId, UpdateClaimAmountRequest request);

  //   void deleteClaim(String claimId);
}
