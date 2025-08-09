package com.patientmanagement.patientservice.mapper;

import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;
import com.patientmanagement.patientservice.model.Patient;
import com.patientmanagement.patientservice.util.enums.Gender;

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

        public static Patient toModel(PatientRequestDto dto, String id) {
            Patient patient = new Patient();
            patient.setId(id);
            patient.setFirstName(dto.getFirstName());
            patient.setLastName(dto.getLastName());
            patient.setUsername(dto.getUsername());
            patient.setEmail(dto.getEmail());
            patient.setDateOfBirth(dto.getDateOfBirth());
            patient.setGender(Gender.valueOf(dto.getGender().toUpperCase()));
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
