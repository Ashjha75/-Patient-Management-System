package com.patientmanagement.patientservice.serviceImplementation;

import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;
import com.patientmanagement.patientservice.dto.PageInfoDTO;
import com.patientmanagement.patientservice.dto.PatientPageResponseDTO;
import com.patientmanagement.patientservice.exception.ApiException;
import com.patientmanagement.patientservice.exception.ResourceNotFound;
import com.patientmanagement.patientservice.grpc.BillingServiceGrpcClient;
import com.patientmanagement.patientservice.mapper.PatientMapper;
import com.patientmanagement.patientservice.model.Patient;
import com.patientmanagement.patientservice.repository.PatientRepository;
import com.patientmanagement.patientservice.service.PatientService;
import com.patientmanagement.patientservice.util.IdGenerator;
import com.patientmanagement.patientservice.util.enums.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * The type Patient service.
 */
@Service
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;

    public PatientServiceImpl(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
    }

    @Override
    public PatientPageResponseDTO getAllPatients(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Patient> patientPage = patientRepository.findAll(pageDetails);

        List<PatientResponseDTO> patientDTOs = patientPage.getContent()
                .stream()
                .map(PatientMapper::toDTO)
                .toList();

        PatientPageResponseDTO response = new PatientPageResponseDTO();
        response.setData(patientDTOs);

        PageInfoDTO pageInfo = new PageInfoDTO();
        pageInfo.setPageNumber(patientPage.getNumber());
        pageInfo.setPageSize(patientPage.getSize());
        pageInfo.setTotalElements(patientPage.getTotalElements());
        pageInfo.setTotalPages(patientPage.getTotalPages());
        pageInfo.setLastPage(patientPage.isLast());

        response.setPageInfo(pageInfo);

        return response;
    }

    @Override
    public PatientResponseDTO addPatient(PatientRequestDto patientRequestDto) {
        if (patientRepository.existsByUsername(patientRequestDto.getUsername())) {
            throw new ApiException("Username already exists");
        }
        if (patientRepository.existsByEmail(patientRequestDto.getEmail())) {
            throw new ApiException("Email already exists");
        }
        String id = IdGenerator.generatePatientId();
        Patient patient = PatientMapper.toModel(patientRequestDto, id);
        Patient savedPatient = patientRepository.save(patient);
        billingServiceGrpcClient.createBillingAccount(savedPatient.getId(), savedPatient.getFirstName(), savedPatient.getEmail());
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
                throw new ApiException("Invalid email format");
            }
            if (patientRepository.existsByEmail(email)) {
                throw new ApiException("Email already exists");
            }
            patient.setEmail(email);
        }

        // Date of birth validation
        if (patientRequestDto.getDateOfBirth() != null) {
            LocalDate dob = patientRequestDto.getDateOfBirth();
            if (dob.isAfter(LocalDate.now())) {
                throw new ApiException("Date of birth cannot be in the future");
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

    @Override
    public void deletePatient(String username) {
        if (!patientRepository.existsByUsername(username)) {
            throw new ResourceNotFound("Patient", "username", username);
        }
        patientRepository.delete(patientRepository.findByUsername(username));
    }
}