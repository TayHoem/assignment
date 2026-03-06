package org.example.tay.internassign3.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.example.tay.internassign3.entityEnum.PaymentStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payments")
public class Payment {
    @Id
    private ObjectId id;
    @Indexed
    private ObjectId claimId;
    private String employeeNumber;
    private String paymentMethod;
    private BigDecimal paymentAmount;
    private PaymentStatus status;

    @LastModifiedDate
    private LocalDateTime processedDate;

    @CreatedDate
    private LocalDateTime createdDate;
}
