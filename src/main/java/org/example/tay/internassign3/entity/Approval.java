package org.example.tay.internassign3.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.example.tay.internassign3.entityEnum.ApprovalStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "approvals")
public class Approval {
    @Id
    private ObjectId id;
    @Indexed
    private ObjectId claimId;
    private String approverId;
    private ApprovalStatus status;
    private String comments;
    @CreatedDate
    private LocalDateTime actionDate;
}
