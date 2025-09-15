package com.patientmanagement.patientservice.controller;

import com.patientmanagement.patientservice.config.AppConstants;
import com.patientmanagement.patientservice.dto.PatientPageResponseDTO;
import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;
import com.patientmanagement.patientservice.service.PatientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;


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
    @PostMapping(value = "/add-patient", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PatientResponseDTO> addPatient(
            @RequestParam("username") String username,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("dateOfBirth") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate dateOfBirth,
            @RequestParam("gender") String gender,
            @RequestParam("addressLine1") String addressLine1,
            @RequestParam(value = "addressLine2", required = false) String addressLine2,
            @RequestParam("city") String city,
            @RequestParam("state") String state,
            @RequestParam("country") String country,
            @RequestParam("postalCode") String postalCode,
            @RequestParam(value = "registrationDate", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate registrationDate,
            @RequestPart(value = "profilePic", required = false) MultipartFile profilePic
    ) {
        PatientRequestDto patientRequestDto = new PatientRequestDto();
        patientRequestDto.setUsername(username);
        patientRequestDto.setFirstName(firstName);
        patientRequestDto.setLastName(lastName);
        patientRequestDto.setDateOfBirth(dateOfBirth);
        patientRequestDto.setGender(gender);
        patientRequestDto.setAddressLine1(addressLine1);
        patientRequestDto.setAddressLine2(addressLine2);
        patientRequestDto.setCity(city);
        patientRequestDto.setState(state);
        patientRequestDto.setCountry(country);
        patientRequestDto.setPostalCode(postalCode);
        patientRequestDto.setRegistrationDate(registrationDate);

        PatientResponseDTO responseDto = patientService.completePatientProfile(patientRequestDto, profilePic);
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
    @PreAuthorize("hasAuthority('PATIENT_MANAGEMENT:VIEW or #username == authentication.name ')")
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