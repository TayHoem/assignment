package org.example.tay.internassign3.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequestDTO {
    @NotBlank(message = "Employee number is required")
    private String employeeNumber;

    @NotBlank(message = "First name is required")
    @Size(max = 30, message = "First Name cannot exceed 30 characters")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "First Name must contain only letters and spaces"
    )
    private String firstName;

    @Size(max = 50, message = "Name cannot exceed 30 characters")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Last Name must contain only letters and spaces"
    )
    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid @")
    private String email;
}
