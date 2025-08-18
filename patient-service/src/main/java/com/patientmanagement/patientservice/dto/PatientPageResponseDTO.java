package com.patientmanagement.patientservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class PatientPageResponseDTO {
    private List<PatientResponseDTO> patients;
    private PageInfoDTO pageInfo;
}
