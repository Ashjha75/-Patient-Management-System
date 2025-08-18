package com.patientmanagement.patientservice.dto;

import lombok.Data;

@Data
public class PageInfoDTO {
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean lastPage;
}