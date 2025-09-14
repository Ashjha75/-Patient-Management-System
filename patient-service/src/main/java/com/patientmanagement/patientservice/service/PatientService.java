package com.patientmanagement.patientservice.service;

import com.patientmanagement.patientservice.dto.PatientPageResponseDTO;
import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;
import org.springframework.web.multipart.MultipartFile;


public interface PatientService {
    PatientPageResponseDTO getAllPatients(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    PatientResponseDTO completePatientProfile(PatientRequestDto patientRequestDto, MultipartFile file);

    //    update patients by username
    PatientResponseDTO updatePatient(String username, PatientRequestDto patientRequestDto);

    PatientResponseDTO getPatientByUsername(String username);


    void deletePatient(String username);

}