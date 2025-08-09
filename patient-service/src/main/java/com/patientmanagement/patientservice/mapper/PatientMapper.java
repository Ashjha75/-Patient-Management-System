package com.patientmanagement.patientservice.mapper;

import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;
import com.patientmanagement.patientservice.model.Patient;

public class PatientMapper {

    // This class will contain methods to map between Patient and PatientDTO
    // For example, you can create a method like this:

     public static PatientResponseDTO toDTO(Patient patient) {
         PatientResponseDTO dto = new PatientResponseDTO();  // Create a new instance of PatientDTO
         dto.setId(patient.getId());
         dto.setFirstName(patient.getFirstName());
         dto.setLastName(patient.getLastName());
         dto.setUsername(patient.getUsername());
         dto.setEmail(patient.getEmail());
         dto.setDateOfBirth(patient.getDateOfBirth());
         dto.setGender(patient.getGender().name());
         dto.setAddressLine1(patient.getAddressLine1());
         dto.setAddressLine2(patient.getAddressLine2());
         dto.setCity(patient.getCity());
         dto.setState(patient.getState());
         dto.setCountry(patient.getCountry());
         dto.setPostalCode(patient.getPostalCode());
         dto.setRegistrationDate(patient.getRegistrationDate());
         return dto;
     }

    public static Patient toModel(PatientRequestDto dto) {
        Patient patient = new Patient();
        // Use provided ID or generate a new one with IdGenerator
        patient.setId(dto.getId() != null ? dto.getId() : com.patientmanagement.patientservice.util.IdGenerator.generatePatientId());
        patient.setFirstName(dto.getFirstName());
        patient.setLastName(dto.getLastName());
        patient.setUsername(dto.getUsername());
        patient.setEmail(dto.getEmail());
        patient.setDateOfBirth(dto.getDateOfBirth());
        try {
            patient.setGender(Patient.Gender.valueOf(dto.getGender().toUpperCase()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid gender value: " + dto.getGender());
        }
        patient.setAddressLine1(dto.getAddressLine1());
        patient.setAddressLine2(dto.getAddressLine2());
        patient.setCity(dto.getCity());
        patient.setState(dto.getState());
        patient.setCountry(dto.getCountry());
        patient.setPostalCode(dto.getPostalCode());
        patient.setRegistrationDate(dto.getRegistrationDate());
        return patient;
    }
}
