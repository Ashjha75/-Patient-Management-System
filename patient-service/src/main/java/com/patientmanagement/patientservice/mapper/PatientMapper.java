package com.patientmanagement.patientservice.mapper;

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
}
