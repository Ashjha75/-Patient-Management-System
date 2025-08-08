package com.patientmanagement.patientservice.service;

import com.patientmanagement.patientservice.model.Patient;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;
import com.patientmanagement.patientservice.repository.PatientRepository;
import com.patientmanagement.patientservice.mapper.PatientMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponseDTO> getAllPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream()
                .map(PatientMapper::toDTO) // Assuming PatientMapper has a static method to convert Patient to PatientResponseDTO
                .toList();
    }
}