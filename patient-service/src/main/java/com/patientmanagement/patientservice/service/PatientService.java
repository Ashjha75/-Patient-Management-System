package com.patientmanagement.patientservice.service;

import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;

import java.util.List;

public interface PatientService {
    List<PatientResponseDTO> getAllPatients();

    PatientResponseDTO addPatient(PatientRequestDto patientRequestDto);
}