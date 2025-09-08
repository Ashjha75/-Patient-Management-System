package com.patientmanagement.patientservice.security;

import com.patientmanagement.patientservice.util.enums.AuthProviderType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class Oauth2utils {

    public AuthProviderType getOauthProvider(String registrationId) {
        if (registrationId == null) {
            throw new IllegalArgumentException("RegistrationId cannot be null");
        }

        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProviderType.GOOGLE;
            case "github" -> AuthProviderType.GITHUB;
            default -> throw new IllegalArgumentException("Invalid provider type: " + registrationId);
        };
    }


    public String determineProviderIdFromOauth2user(OAuth2User oAuth2User, String registrationId) {
        if (oAuth2User == null || registrationId == null) {
            throw new IllegalArgumentException("OAuth2User and registrationId must not be null");
        }

        String providerId = switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub"); // Google user ID
            case "github" -> Objects.toString(oAuth2User.getAttribute("id"), null); // GitHub user ID
            default -> throw new IllegalArgumentException("Invalid provider type: " + registrationId);
        };

        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("Invalid provider id for provider: " + registrationId);
        }

        return providerId;
    }


}
