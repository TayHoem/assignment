package org.example.tay.internassign3.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tay.internassign3.dto.ClaimItemDto;
import org.example.tay.internassign3.dto.ClaimTypeDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRequestDTO {
    //Request DTO for creating/updating claims
    @NotNull(message = "Claim Type is required")
    @Valid
    private ClaimTypeDto claimType;

    @Valid
    private List<ClaimItemDto> claimItems;
}
