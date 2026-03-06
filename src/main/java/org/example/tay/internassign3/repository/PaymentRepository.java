package org.example.tay.internassign3.repository;


import org.example.tay.internassign3.entity.Payment;
import org.example.tay.internassign3.entityEnum.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository
        extends MongoRepository<Payment, Long> {

    List<Payment> findByEmployeeNumber(String employeeNumber);
    List<Payment> findByStatus(PaymentStatus status);

    Boolean existsByClaimId(Object claimId);
}
