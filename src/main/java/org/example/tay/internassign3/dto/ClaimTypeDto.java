package org.example.tay.internassign3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimTypeDto {

    @NotBlank(message = "Type code is required")
    private String typeCode;

    @NotBlank(message = "Name is required")
    private String name;
}
