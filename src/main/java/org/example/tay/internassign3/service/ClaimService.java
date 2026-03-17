package org.example.tay.internassign3.service;

import org.example.tay.internassign3.dto.request.ClaimRequestDTO;
import org.example.tay.internassign3.dto.response.ClaimResponseDTO;
import org.example.tay.internassign3.entity.Claim;

import java.lang.String;
import java.util.List;


public interface ClaimService {

    ClaimResponseDTO createClaim(String employeeId, ClaimRequestDTO request);

    //ClaimResponseDTO addItemtoClaim(String claimId, ClaimItemDto itemDto);

    ClaimResponseDTO getClaimById(String claimId);

    List<ClaimResponseDTO> getClaimByEmployeeSnapShotEmployeeNumber(String employeeNumber);

    ClaimResponseDTO cliamUpdate(String claimId, ClaimRequestDTO requestDTO);

    Claim getClaimEntityById(String claimId);

    List<ClaimResponseDTO> getAllClaims();

    //ClaimResponseDTO updateClaimAmount(String claimId, UpdateClaimAmountRequest request);

  //   void deleteClaim(String claimId);
}
