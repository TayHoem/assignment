package org.example.tay.internassign3.repository;

import org.bson.types.ObjectId;
import org.example.tay.internassign3.entity.Approval;
import org.example.tay.internassign3.entityEnum.ApprovalStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ApprovalRepository
        extends MongoRepository<Approval, ObjectId> {

}
