package org.example.tay.internassign3.entity;


import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "employees")
public class Employee {
    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String employeeNumber;

    private String firstName;
    private String lastName;

    @Indexed(unique = true)
    private String email;
}
