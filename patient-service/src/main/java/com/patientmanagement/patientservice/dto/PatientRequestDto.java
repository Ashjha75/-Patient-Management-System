package com.patientmanagement.patientservice.dto;

import com.patientmanagement.patientservice.dto.validators.CreatePatientValidationGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRequestDto {

//        @Schema(defaultValue = "PAT1234567890")
//        private String id;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Schema(defaultValue = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Schema(defaultValue = "Doe")
    private String lastName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Schema(defaultValue = "johndoe")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(defaultValue = "john.doe@example.com")
    private String email;

    @NotNull(message = "Date of birth is required")
    @Schema(defaultValue = "2000-01-01")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    @Schema(defaultValue = "MALE")
    private String gender;

    @NotBlank(message = "Address Line 1 is required")
    @Schema(defaultValue = "123 Main St")
    private String addressLine1;

    @Schema(defaultValue = "Apt 4B")
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Schema(defaultValue = "New York")
    private String city;

    @NotBlank(message = "State is required")
    @Schema(defaultValue = "NY")
    private String state;

    @NotBlank(message = "Country is required")
    @Schema(defaultValue = "USA")
    private String country;

    @NotBlank(message = "Postal code is required")
    @Schema(defaultValue = "10001")
    private String postalCode;

    @NotBlank(groups = CreatePatientValidationGroup.class, message = "Registration date is required")
    @Schema(defaultValue = "2024-01-01")
    private LocalDate registrationDate;
}