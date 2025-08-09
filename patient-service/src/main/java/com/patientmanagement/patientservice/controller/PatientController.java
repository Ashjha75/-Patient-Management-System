package com.patientmanagement.patientservice.controller;

import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;
import com.patientmanagement.patientservice.repository.PatientRepository;
import com.patientmanagement.patientservice.service.PatientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/patients")
@Tag(name = "2. Patient")
public class PatientController {

    private final PatientService patientService;
    private final PatientRepository patientRepository;

    public PatientController(PatientService patientService, PatientRepository patientRepository) {
        this.patientService = patientService;
        this.patientRepository = patientRepository;
    }

    // Get all patients controller method
    @GetMapping("/all-patients")
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        List<PatientResponseDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

//    add patient Controller method

    @PostMapping("/add-patient")
    public PatientResponseDTO addPatient(PatientRequestDto patientRequestDto) {
        Patient patient = patientRepository.save(
                patientRequestDto.toModel(patientRequestDto);
        )
    }
}