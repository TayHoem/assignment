package org.example.tay.internassign3.repository;

import org.bson.types.ObjectId;
import org.example.tay.internassign3.entity.Claim;
import org.example.tay.internassign3.entityEnum.ClaimStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ClaimRepository
        extends MongoRepository<Claim, ObjectId> {
    List<Claim> findByEmployeeSnapshot_Id(ObjectId employeeId);

    Optional<Claim> findByIdAndEmployeeSnapshot_Id(ObjectId id, ObjectId employeeId);
}
