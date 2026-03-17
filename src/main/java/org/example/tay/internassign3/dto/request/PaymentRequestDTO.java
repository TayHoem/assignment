package org.example.tay.internassign3.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDTO {
    @Pattern(regexp = "BANK_TRANSFER|CASH|CHEQUE|CREDIT_CARD",
            message = "Invalid payment method. Must be BANK_TRANSFER, CASH, CHEQUE, or CREDIT_CARD")
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}
