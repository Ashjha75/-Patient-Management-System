package com.patientmanagement.patientservice.controller;

import com.patientmanagement.patientservice.config.AppConstants;
import com.patientmanagement.patientservice.dto.PatientPageResponseDTO;
import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;
import com.patientmanagement.patientservice.dto.validators.CreatePatientValidationGroup;
import com.patientmanagement.patientservice.service.PatientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "2. Patient")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * <b>Security Rule:</b> Access is granted if the authenticated user has the
     * 'PATIENT_MANAGEMENT:VIEW' authority. This permission is derived from the user's
     * roles and their associated permissions stored in the database.
     */
    @PreAuthorize("hasAuthority('PATIENT_MANAGEMENT:VIEW')")
    @GetMapping("/all-patients")
    public ResponseEntity<PatientPageResponseDTO> getAllPatients(
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_PATIENTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        PatientPageResponseDTO patients = patientService.getAllPatients(pageNumber, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok(patients);
    }

    /**
     * <b>Security Rule:</b> Access is granted if the authenticated user has the
     * 'PATIENT_MANAGEMENT:CREATE' authority.
     */
    @PreAuthorize("hasAuthority('PATIENT_MANAGEMENT:CREATE')")
    @PostMapping("/add-patient")
    public ResponseEntity<PatientResponseDTO> addPatient(@Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDto patientRequestDto) {
        PatientResponseDTO responseDto = patientService.completePatientProfile(patientRequestDto);
        return ResponseEntity.status(201).body(responseDto);
    }

    /**
     * <b>Security Rule:</b> Access is granted if the authenticated user has the
     * 'PATIENT_MANAGEMENT:EDIT' authority.
     */
    @PreAuthorize("hasAuthority('PATIENT_MANAGEMENT:EDIT')")
    @PutMapping("/edit-patient")
    public ResponseEntity<PatientResponseDTO> editPatient(@Validated(Default.class)
                                                          @RequestParam(name = "username") String username,
                                                          @RequestBody PatientRequestDto patientRequestDto) {

        PatientResponseDTO responseDto = patientService.updatePatient(username, patientRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * <b>Security Rule:</b> Access is granted if the authenticated user has the
     * 'PATIENT_MANAGEMENT:VIEW' authority.
     */
    @PreAuthorize("hasAuthority('PATIENT_MANAGEMENT:VIEW')")
    @GetMapping("/get-patient/{patientusername}")
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable("patientusername") String patientusername) {

        PatientResponseDTO patient = patientService.getPatientByUsername(patientusername);
        return ResponseEntity.ok(patient);
    }

    /**
     * <b>Security Rule:</b> Access is granted if the authenticated user has the
     * 'PATIENT_MANAGEMENT:DELETE' authority. For critical operations, this can be
     * combined with role checks, e.g., "hasAuthority('ROLE_ADMIN') or hasAuthority('PATIENT_MANAGEMENT:DELETE')".
     */
    @PreAuthorize("hasAuthority('PATIENT_MANAGEMENT:DELETE')")
    @DeleteMapping("/delete-patient/{patientusername}")
    public ResponseEntity<PatientResponseDTO> deletePatient(@PathVariable("patientusername") String patientusername) {

        patientService.deletePatient(patientusername);
        return ResponseEntity.noContent().build();
    }
}