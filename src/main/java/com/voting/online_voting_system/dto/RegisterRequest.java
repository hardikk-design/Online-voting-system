package com.voting.online_voting_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Student ID is required")
    @Pattern(
            regexp = "\\d{11}",
            message = "Student ID must contain exactly 11 digits"
    )
    private String studentId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            message = "Password must contain at least 8 characters"
    )
    private String password;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Course is required")
    private String course;

    @NotNull(message = "Year is required")
    @Min(
            value = 1,
            message = "Year must be at least 1"
    )
    private Integer year;
}