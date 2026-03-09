package org.example.tay.internassign3.repository;

import org.bson.types.ObjectId;
import org.example.tay.internassign3.entity.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EmployeeRepository
        extends MongoRepository<Employee, ObjectId> {
    //Optional<Employee> findByEmployeeNumber(String employeeNumber);

    Boolean existsByEmployeeNumber(String employeeNumber);

    Boolean existsByEmail(String email);

    boolean existsByEmployeeNumberAndIdNot(String employeeNumber, ObjectId id);
}
