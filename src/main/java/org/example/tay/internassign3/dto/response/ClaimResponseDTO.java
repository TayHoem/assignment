package org.example.tay.internassign3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tay.internassign3.dto.ClaimItemDto;
import org.example.tay.internassign3.dto.ClaimTypeDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponseDTO {
    private String id;
    private String employeeNumber;
    private ClaimTypeDto claimType;
    private BigDecimal totalAmount;
    private List<ClaimItemDto> items;
    private String status;

    private LocalDateTime lastUpdatedDate;
    private LocalDateTime createdDate;
}
