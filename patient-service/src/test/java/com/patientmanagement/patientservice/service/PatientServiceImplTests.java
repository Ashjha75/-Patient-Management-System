package com.patientmanagement.patientservice.service;

import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;
import com.patientmanagement.patientservice.exception.InvalidInputException;
import com.patientmanagement.patientservice.grpc.BillingServiceGrpcClient;
import com.patientmanagement.patientservice.model.Patient;
import com.patientmanagement.patientservice.repository.PatientRepository;
import com.patientmanagement.patientservice.serviceImplementation.PatientServiceImpl;
import com.patientmanagement.patientservice.util.enums.Gender;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class) // Enables Mockito to work with JUnit 5
class PatientServiceImplTest {

    // --- 1. Consolidated Field Declarations ---
    // All mocks and the service instance are declared here, once.
    @Mock
    private PatientRepository patientRepository;

    @Mock
    private BillingServiceGrpcClient billingServiceGrpcClient;

    @InjectMocks
    private PatientServiceImpl patientService;
//    eg
//    patientService = new patientService(patientRepository, billingServiceGrpcClient)

    // All test data objects are declared here.
    private Patient patientForGet;
    private PatientRequestDto patientRequestDto;
    private Patient savedPatient;

    // --- 2. Merged Setup Method ---
    // This single method now prepares all data needed for all tests in this class.
    @BeforeEach
    void setUp() {
        // Data for testing getAllPatients
        patientForGet = new Patient();
        patientForGet.setId("P001");
        patientForGet.setUsername("johndoe");
        patientForGet.setFirstName("John");
        patientForGet.setLastName("Doe");
        patientForGet.setEmail("john.doe@example.com");
        patientForGet.setDateOfBirth(LocalDate.of(1990, 1, 15));
        patientForGet.setGender(Gender.MALE);

        // Data for testing addPatient
        patientRequestDto = new PatientRequestDto();
        patientRequestDto.setUsername("newuser");
        patientRequestDto.setFirstName("Jane");
        patientRequestDto.setLastName("Doe");
        patientRequestDto.setEmail("jane.doe@example.com");
        patientRequestDto.setDateOfBirth(LocalDate.of(1995, 5, 20));
        patientRequestDto.setGender("FEMALE");

        savedPatient = new Patient();
        savedPatient.setId("some-generated-id");
        savedPatient.setUsername("newuser");
        savedPatient.setFirstName("Jane");
        savedPatient.setLastName("Doe");
        savedPatient.setEmail("jane.doe@example.com");
        savedPatient.setDateOfBirth(LocalDate.of(1995, 5, 20));
        savedPatient.setGender(Gender.FEMALE);
    }

    // --- Test for getAllPatients ---
    @Test
    @DisplayName("Should Return Paginated List Of Patients")
    void getAllPatients_shouldReturnPatientPageResponseDTO() {
        log.info("Testing getAllPatients...");
        // ARRANGE
        List<Patient> patientList = Collections.singletonList(patientForGet);
        Page<Patient> patientPage = new PageImpl<>(patientList);
        when(patientRepository.findAll(any(Pageable.class))).thenReturn(patientPage);

        // ACT
        var result = patientService.getAllPatients(0, 10, "firstName", "asc");

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(0, result.getPageInfo().getPageNumber());
        assertEquals("John", result.getData().getFirst().getFirstName());
    }

    // --- Tests for addPatient ---
    @Test
    @DisplayName("Should Add Patient Successfully When Data Is Valid")
    void addPatient_whenDataIsValid_shouldSaveAndReturnPatient() {
        log.info("Testing addPatient success case...");
        // ARRANGE
        when(patientRepository.existsByUsername(anyString())).thenReturn(false);
        when(patientRepository.existsByEmail(anyString())).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // ACT
        PatientResponseDTO result = patientService.addPatient(patientRequestDto);

        // ASSERT
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("some-generated-id", result.getId());
        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(billingServiceGrpcClient, times(1)).createBillingAccount(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should Throw InvalidInputException When Username Exists")
    void addPatient_whenUsernameExists_shouldThrowException() {
        log.info("Testing addPatient failure for duplicate username...");
        // ARRANGE
        when(patientRepository.existsByUsername("newuser")).thenReturn(true);

        // ACT & ASSERT
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> patientService.addPatient(patientRequestDto));
        assertEquals("Username already exists", exception.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
        verify(billingServiceGrpcClient, never()).createBillingAccount(any(), any(), any());
    }


    @Test
    @DisplayName("Should Throw InvalidInputException When Email Exists")
    void addPatient_whenEmailExists_shouldThrowException() {
        log.info("Testing addPatient failure for duplicate email...");
        // ARRANGE
        when(patientRepository.existsByUsername("newuser")).thenReturn(false);
        when(patientRepository.existsByEmail("jane.doe@example.com")).thenReturn(true);

        // ACT & ASSERT
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> patientService.addPatient(patientRequestDto));
        assertEquals("Email already exists", exception.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
        verify(billingServiceGrpcClient, never()).createBillingAccount(any(), any(), any());
    }
}