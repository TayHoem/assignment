package org.example.tay.internassign3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO {
    private String id;
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String email;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}