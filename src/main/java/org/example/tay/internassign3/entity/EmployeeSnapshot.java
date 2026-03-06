package org.example.tay.internassign3.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSnapshot {
    private ObjectId id;
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String email;
}

