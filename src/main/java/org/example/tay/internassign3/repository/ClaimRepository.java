package org.example.tay.internassign3.repository;

import org.bson.types.ObjectId;
import org.example.tay.internassign3.entity.Claim;
import org.example.tay.internassign3.entityEnum.ClaimStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClaimRepository
        extends MongoRepository<Claim, ObjectId> {
//    List<Claim> findByEmployeeNumber(ObjectId employeeId);
//
//    Optional<Claim> findByIdAndEmployeeNumber(ObjectId id, ObjectId employeeId);

    // Find PENDING claims for the same employee + same claimType code
    @Query("{ 'employeeSnapshot.id': ?0, 'claimType.typeCode': ?1, 'status': 'PENDING' }")
    List<Claim> findPendingByEmployeeIdAndClaimTypeCode(ObjectId employeeId, String claimTypeCode);
}
