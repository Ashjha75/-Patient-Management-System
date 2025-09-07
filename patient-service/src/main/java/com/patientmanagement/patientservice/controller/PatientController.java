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
     * <b>Security Rule:</b> This endpoint is protected by a dynamic permission check using the
     * {@code @PreAuthorize} annotation. The expression must evaluate to true for access.
     * <p>
     * Expression Breakdown:
     * {@code "@permissionService.hasPermission(authentication, 'PATIENT_MANAGEMENT', 'LIST') and @permissionService.hasPermission(authentication, 'PATIENT_MANAGEMENT', 'VIEW')"}
     * <ul>
     * <li><b>{@code @permissionService}</b>: Calls our custom security bean to check permissions.</li>
     * <li><b>{@code authentication}</b>: The security context of the currently logged-in user.</li>
     * <li><b>{@code 'PATIENT_MANAGEMENT'}</b>: The specific module key being checked against.</li>
     * <li><b>{@code 'LIST'} and {@code 'VIEW'}</b>: The specific permissions required. The 'and' operator ensures the user must have both.</li>
     * </ul>
     * Access is granted only if the user's role has both permissions for the specified module.
     */

    @PreAuthorize("@permissionService.hasPermission(authentication, 'PATIENT_MANAGEMENT', 'LIST') and @permissionService.hasPermission(authentication, 'PATIENT_MANAGEMENT', 'VIEW')")
    @GetMapping("/all-patients")
    public ResponseEntity<PatientPageResponseDTO> getAllPatients(
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_PATIENTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        PatientPageResponseDTO patients = patientService.getAllPatients(pageNumber, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok(patients);

    }

    @PostMapping("/add-patient")
    public ResponseEntity<PatientResponseDTO> addPatient(@Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDto patientRequestDto) {
        PatientResponseDTO responseDto = patientService.addPatient(patientRequestDto);
        return ResponseEntity.status(201).body(responseDto);
    }

    @PutMapping("/edit-patient")
    public ResponseEntity<PatientResponseDTO> editPatient(@Validated(Default.class)
                                                          @RequestParam(name = "username") String username,
                                                          @RequestBody PatientRequestDto patientRequestDto) {

        PatientResponseDTO responseDto = patientService.updatePatient(username, patientRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/get-patient/{patientusername}")
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable("patientusername") String patientusername) {

        PatientResponseDTO patient = patientService.getPatientByUsername(patientusername);
        return ResponseEntity.ok(patient);
    }

    @DeleteMapping("/delete-patient/{patientusername}")
    public ResponseEntity<PatientResponseDTO> deletePatient(@PathVariable("patientusername") String patientusername) {

        patientService.deletePatient(patientusername);
        return ResponseEntity.noContent().build();
    }
}