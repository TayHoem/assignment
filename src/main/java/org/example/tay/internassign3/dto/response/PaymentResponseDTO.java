package org.example.tay.internassign3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private String id;
    private String claimId;
    private String employeeNumber;
    private String paymentMethod;
    private BigDecimal paymentAmount;
    private String status;
    private LocalDateTime processedDate;
    private LocalDateTime createdDate;
}
