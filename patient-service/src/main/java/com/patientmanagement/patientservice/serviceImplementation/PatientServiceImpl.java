package com.patientmanagement.patientservice.serviceImplementation;

import com.patientmanagement.patientservice.dto.PageInfoDTO;
import com.patientmanagement.patientservice.dto.PatientPageResponseDTO;
import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.dto.PatientResponseDTO;
import com.patientmanagement.patientservice.exception.ApiException;
import com.patientmanagement.patientservice.exception.ResourceNotFound;
import com.patientmanagement.patientservice.grpc.BillingServiceGrpcClient;
import com.patientmanagement.patientservice.mapper.PatientMapper;
import com.patientmanagement.patientservice.model.Patient;
import com.patientmanagement.patientservice.repository.PatientRepository;
import com.patientmanagement.patientservice.repository.UserRepository;
import com.patientmanagement.patientservice.service.PatientService;
import com.patientmanagement.patientservice.util.IdGenerator;
import com.patientmanagement.patientservice.util.enums.Gender;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final UserRepository userRepository;

    @Override
    public PatientPageResponseDTO getAllPatients(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Patient> patientPage = patientRepository.findAll(pageDetails);

        List<PatientResponseDTO> patientDTOs = patientPage.getContent().stream().map(PatientMapper::toDTO).toList();

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
    @Transactional
    public PatientResponseDTO completePatientProfile(PatientRequestDto patientRequestDto) {
        // 1. Validate username/email uniqueness
        if (!userRepository.existsByUsername(patientRequestDto.getUsername())) {
            throw new ResourceNotFound("User", "username", patientRequestDto.getUsername());
        }
        // 2. Check if patient already exists for this username
        if (patientRepository.existsByUsername(patientRequestDto.getUsername())) {
            throw new ApiException("Patient profile already exists for this user");
        }
        // 2. Generate patient ID
        String patientId = IdGenerator.generatePatientId();

        // 3. Set default profile image if none provided
        if (patientRequestDto.getUserImage() == null || patientRequestDto.getUserImage().trim().isEmpty()) {
            patientRequestDto.setUserImage("https://dummyimage.com/400x400/cccccc/000000.png&text=Profile");
        }

        // 4. Ensure registrationDate is set (default to today if not provided)
        if (patientRequestDto.getRegistrationDate() == null) {
            patientRequestDto.setRegistrationDate(LocalDate.now());
        }

        // 5. Map DTO -> Entity
        Patient patient = PatientMapper.toModel(patientRequestDto, patientId);

        // 6. Save patient
        Patient savedPatient = patientRepository.save(patient);

        // 7. Create billing account
        billingServiceGrpcClient.createBillingAccount(savedPatient.getFirstName(), savedPatient.getEmail());

        // 8. Return DTO
        return PatientMapper.toDTO(savedPatient);
    }

    // Java
    @Override
    public PatientResponseDTO updatePatient(String username, PatientRequestDto dto) {
        Patient patient = patientRepository.findByUsername(username);
        if (patient == null) {
            throw new ResourceNotFound("Username", "id", username);
        }

        updateName(patient, dto);
        updateDateOfBirth(patient, dto);
        updateGender(patient, dto);
        updateAddress(patient, dto);

        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toDTO(updatedPatient);
    }

    private void updateName(Patient patient, PatientRequestDto dto) {
        if (dto.getFirstName() != null && !dto.getFirstName().trim().isEmpty()) {
            patient.setFirstName(dto.getFirstName().trim());
        }
        if (dto.getLastName() != null && !dto.getLastName().trim().isEmpty()) {
            patient.setLastName(dto.getLastName().trim());
        }
    }


    private void updateDateOfBirth(Patient patient, PatientRequestDto dto) {
        if (dto.getDateOfBirth() != null) {
            LocalDate dob = dto.getDateOfBirth();
            if (dob.isAfter(LocalDate.now())) {
                throw new ApiException("Date of birth cannot be in the future");
            }
            patient.setDateOfBirth(dob);
        }
    }

    private void updateGender(Patient patient, PatientRequestDto dto) {
        if (dto.getGender() != null && !dto.getGender().trim().isEmpty()) {
            try {
                patient.setGender(Gender.valueOf(dto.getGender().trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ApiException("Invalid gender value. Allowed values: " + String.join(", ", java.util.Arrays.stream(Gender.values()).map(Enum::name).toArray(String[]::new)));
            }
        }
    }

    private void updateAddress(Patient patient, PatientRequestDto dto) {
        if (dto.getAddressLine1() != null && !dto.getAddressLine1().trim().isEmpty()) {
            patient.setAddressLine1(dto.getAddressLine1().trim());
        }
        if (dto.getAddressLine2() != null) {
            patient.setAddressLine2(dto.getAddressLine2().trim());
        }
        if (dto.getCity() != null && !dto.getCity().trim().isEmpty()) {
            patient.setCity(dto.getCity().trim());
        }
        if (dto.getState() != null && !dto.getState().trim().isEmpty()) {
            patient.setState(dto.getState().trim());
        }
        if (dto.getCountry() != null && !dto.getCountry().trim().isEmpty()) {
            patient.setCountry(dto.getCountry().trim());
        }
        if (dto.getPostalCode() != null && !dto.getPostalCode().trim().isEmpty()) {
            patient.setPostalCode(dto.getPostalCode().trim());
        }
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