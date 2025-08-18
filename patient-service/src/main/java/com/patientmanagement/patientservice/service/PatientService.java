package com.patientmanagement.patientservice.service;

import com.patientmanagement.patientservice.dto.PatientPageResponseDTO;
import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;

import java.util.List;


public interface PatientService {
    PatientPageResponseDTO getAllPatients(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    PatientResponseDTO addPatient(PatientRequestDto patientRequestDto);

    //    update patients by username
    PatientResponseDTO updatePatient(String username, PatientRequestDto patientRequestDto);

    PatientResponseDTO getPatientByUsername(String username);


    void deletePatient(String username);
}