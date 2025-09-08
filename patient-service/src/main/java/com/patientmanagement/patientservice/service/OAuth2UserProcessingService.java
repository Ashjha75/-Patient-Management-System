package com.patientmanagement.patientservice.service;

import com.patientmanagement.patientservice.dto.UserInfoResponse;
import com.patientmanagement.patientservice.model.User;
import com.patientmanagement.patientservice.util.enums.AuthProviderType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserProcessingService {
    ResponseEntity<UserInfoResponse> handleOauth2loginRequest(OAuth2User oAuth2User, String registrationId);

    User registerNewOauth2User(String username, String email, String providerId, AuthProviderType providerType);
}