package org.example.tay.internassign3.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestDTO {

    @NotBlank(message = "Approver ID is required")
    private String approverId;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "APPROVED|REJECTED", message = "Status must be APPROVED or REJECTED")
    private String status;

    @Size(max = 50, message = "Comments cannot more than 50 characters")
    private String comments;
}
