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
import com.patientmanagement.patientservice.util.enums.Gender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * The type Patient service.
 */
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
        Patient patient = patientRepository.findByUsername(username);
        if (patient == null) {
            throw new ResourceNotFound("Username", "id", username);
        }

        // Update patient details with validation
        if (patientRequestDto.getFirstName() != null && !patientRequestDto.getFirstName().trim().isEmpty()) {
            patient.setFirstName(patientRequestDto.getFirstName().trim());
        }

        if (patientRequestDto.getLastName() != null && !patientRequestDto.getLastName().trim().isEmpty()) {
            patient.setLastName(patientRequestDto.getLastName().trim());
        }

        // Email validation and uniqueness check
        if (patientRequestDto.getEmail() != null && !patientRequestDto.getEmail().trim().isEmpty()
                && !patient.getEmail().equals(patientRequestDto.getEmail().trim())) {
            String email = patientRequestDto.getEmail().trim();
            if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            if (patientRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already exists");
            }
            patient.setEmail(email);
        }

        // Date of birth validation
        if (patientRequestDto.getDateOfBirth() != null) {
            LocalDate dob = patientRequestDto.getDateOfBirth();
            if (dob.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Date of birth cannot be in the future");
            }
            patient.setDateOfBirth(dob);
        }

        // Gender validation
        if (patientRequestDto.getGender() != null && !patientRequestDto.getGender().trim().isEmpty()) {
            try {
                patient.setGender(Gender.valueOf(patientRequestDto.getGender().trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ApiException("Invalid gender value. Allowed values: " +
                        String.join(", ", java.util.Arrays.stream(Gender.values())
                                .map(Enum::name)
                                .toArray(String[]::new)));
            }
        }

        // Address validation
        if (patientRequestDto.getAddressLine1() != null && !patientRequestDto.getAddressLine1().trim().isEmpty()) {
            patient.setAddressLine1(patientRequestDto.getAddressLine1().trim());
        }

        // AddressLine2 can be empty, so just check for null
        if (patientRequestDto.getAddressLine2() != null) {
            patient.setAddressLine2(patientRequestDto.getAddressLine2().trim());
        }

        if (patientRequestDto.getCity() != null && !patientRequestDto.getCity().trim().isEmpty()) {
            patient.setCity(patientRequestDto.getCity().trim());
        }

        if (patientRequestDto.getState() != null && !patientRequestDto.getState().trim().isEmpty()) {
            patient.setState(patientRequestDto.getState().trim());
        }

        if (patientRequestDto.getCountry() != null && !patientRequestDto.getCountry().trim().isEmpty()) {
            patient.setCountry(patientRequestDto.getCountry().trim());
        }

        if (patientRequestDto.getPostalCode() != null && !patientRequestDto.getPostalCode().trim().isEmpty()) {
            patient.setPostalCode(patientRequestDto.getPostalCode().trim());
        }

        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toDTO(updatedPatient);
    }

    @Override
    public PatientResponseDTO getPatientByUsername(String username) {
        if (!patientRepository.existsByUsername(username)) {
            throw new ResourceNotFound("Patient", "username", username);
        }
        return PatientMapper.toDTO(patientRepository.findByUsername(username));
    }
}