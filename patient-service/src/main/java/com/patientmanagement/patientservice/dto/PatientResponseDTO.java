package com.patientmanagement.patientservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientResponseDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private LocalDate dateOfBirth;
    private String gender;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private LocalDate registrationDate;

    // Getters and setters
    // (Generate with your IDE or Lombok if preferred)
}