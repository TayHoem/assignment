package org.example.tay.internassign3.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.example.tay.internassign3.entityEnum.ClaimStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "claims")
public class Claim {
    @Id
    private ObjectId id;

    private EmployeeSnapshot employeeSnapshot;

    private ClaimType claimType;
    private BigDecimal totalAmount;

    private List<ClaimItem> items;

    private ClaimStatus status;

    @LastModifiedDate
    private LocalDateTime lastUpdatedDate;

    @CreatedDate
    private LocalDateTime createdDate;


}
