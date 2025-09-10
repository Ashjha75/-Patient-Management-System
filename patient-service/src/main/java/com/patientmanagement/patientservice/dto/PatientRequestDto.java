package com.patientmanagement.patientservice.dto;

import com.patientmanagement.patientservice.dto.validators.CreatePatientValidationGroup;
import com.patientmanagement.patientservice.dto.validators.UpdatePatientValidationGroup;
import com.patientmanagement.patientservice.dto.validators.ValidAge;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRequestDto {
    // For update only, not required for creation
    @NotNull(groups = UpdatePatientValidationGroup.class, message = "id field is required for updates")
    private String id;

    // Optional: S3/dummy image URL
    @Schema(description = "Profile image URL (S3)")
    private String userImage;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Schema(defaultValue = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Schema(defaultValue = "Doe")
    private String lastName;


    @ValidAge(min = 18, message = "Patient must be at least 18 years old")
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

    @NotNull(groups = CreatePatientValidationGroup.class, message = "Registration date is required")
    @Schema(defaultValue = "2024-01-01")
    private LocalDate registrationDate;
}