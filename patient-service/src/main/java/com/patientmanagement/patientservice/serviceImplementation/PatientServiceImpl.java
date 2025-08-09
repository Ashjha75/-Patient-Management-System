package com.patientmanagement.patientservice.serviceImplementation;

import com.patientmanagement.patientservice.exception.ApiException;
import com.patientmanagement.patientservice.exception.ResourceNotFound;
import com.patientmanagement.patientservice.model.Patient;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;
import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.repository.PatientRepository;
import com.patientmanagement.patientservice.mapper.PatientMapper;
import com.patientmanagement.patientservice.service.PatientService;
import com.patientmanagement.patientservice.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public List<PatientResponseDTO> getAllPatients() {
        List<Patient> patients = patientRepository.findAll();
        System.out.println("Fetching all patients from the database..." + patients.size());
        return patients.stream()
                .map(PatientMapper::toDTO)
                .toList();
    }

    @Override
    public PatientResponseDTO addPatient(PatientRequestDto patientRequestDto) {
        if (patientRepository.existsByUsername(patientRequestDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (patientRepository.existsByEmail(patientRequestDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        String id = IdGenerator.generatePatientId();
        Patient patient = PatientMapper.toModel(patientRequestDto, id);
        Patient savedPatient = patientRepository.save(patient);
        return PatientMapper.toDTO(savedPatient);
    }

    @Override
    public PatientResponseDTO updatePatient(String username, PatientRequestDto patientRequestDto) {
        // Check if findByUsername returns Optional<Patient>
        Patient patient = patientRepository.findByUsername(username);
        if (patient == null) {
            throw new ResourceNotFound("Username","id",username);
        }

//        update teh details




        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toDTO(updatedPatient);
    }
}