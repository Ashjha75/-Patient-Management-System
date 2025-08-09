package com.patientmanagement.patientservice.controller;

import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;
import com.patientmanagement.patientservice.mapper.PatientMapper;
import com.patientmanagement.patientservice.model.Patient;
import com.patientmanagement.patientservice.repository.PatientRepository;
import com.patientmanagement.patientservice.service.PatientService;
import com.patientmanagement.patientservice.util.IdGenerator;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/all-patients")
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        List<PatientResponseDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    @PostMapping("/add-patient")
    public ResponseEntity<PatientResponseDTO> addPatient(@RequestBody PatientRequestDto patientRequestDto) {
        String id = IdGenerator.generatePatientId();
        Patient patient = PatientMapper.toModel(patientRequestDto, id);
        Patient savedPatient = patientRepository.save(patient);
        // You need a mapper to convert Patient to PatientResponseDTO
        PatientResponseDTO responseDTO = PatientMapper.toDTO(savedPatient);
        return ResponseEntity.ok(responseDTO);
    }
}