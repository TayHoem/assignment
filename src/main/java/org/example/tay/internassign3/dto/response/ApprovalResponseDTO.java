package org.example.tay.internassign3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalResponseDTO {
    private String id;
    private String claimId;
    private String approverId;
    private String status;
    private String comments;
    private LocalDateTime actionDate;
}
