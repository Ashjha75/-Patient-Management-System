package com.patientmanagement.patientservice.service;

import com.patientmanagement.patientservice.dto.UserInfoResponse;
import com.patientmanagement.patientservice.dto.UserRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

public interface AuthService {
    ResponseEntity<UserInfoResponse> authenticateUser(UserRequestDto userRequest);

    ResponseEntity<String> registerUser(UserRequestDto userRequest);

    ResponseEntity<Map<String, Object>> logout(HttpServletRequest request);

    ResponseEntity<String> completeProfile(UserRequestDto userRequest);

    ResponseEntity<UserInfoResponse> handleOauth2loginRequest(OAuth2User user, String accessToken);
}
