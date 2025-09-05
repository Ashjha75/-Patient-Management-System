package com.patientmanagement.patientservice.service;

import com.patientmanagement.patientservice.dto.UserInfoResponse;
import com.patientmanagement.patientservice.dto.UserRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AuthService {
    ResponseEntity<UserInfoResponse> authenticateUser(UserRequestDto userRequest);

    ResponseEntity<Map<String, Object>> logout(HttpServletRequest request);
}
