package org.example.tay.internassign3.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimItemDto {
    @NotBlank(message = "Expense date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String expenseDate;

    @NotNull(message = "Amount is required")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 2 decimal places")
    @DecimalMin( value ="0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Category code is required")
    private String categoryCode;

}
