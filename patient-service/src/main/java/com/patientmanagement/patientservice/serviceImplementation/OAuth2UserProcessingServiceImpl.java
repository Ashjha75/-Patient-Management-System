package com.patientmanagement.patientservice.serviceImplementation;

// ... (all necessary imports)

import com.patientmanagement.patientservice.dto.UserInfoResponse;
import com.patientmanagement.patientservice.exception.ApiException;
import com.patientmanagement.patientservice.model.User;
import com.patientmanagement.patientservice.repository.UserRepository;
import com.patientmanagement.patientservice.security.Oauth2utils;
import com.patientmanagement.patientservice.service.OAuth2UserProcessingService;
import com.patientmanagement.patientservice.util.enums.AuthProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2UserProcessingServiceImpl implements OAuth2UserProcessingService {

    private final UserRepository userRepository;
    private final Oauth2utils oauth2utils;

    @Override
    public ResponseEntity<UserInfoResponse> handleOauth2loginRequest(OAuth2User oAuth2User, String registrationId) {
        // 1. Determine provider type & ID
        AuthProviderType providerType = oauth2utils.getOauthProvider(registrationId);
        String providerId = oauth2utils.determineProviderIdFromOauth2user(oAuth2User, registrationId);

        // 2. Try to find existing user by providerId + providerType
        User user = userRepository.findByProviderIdAndProviderType(providerId, providerType).orElse(null);

        // 3. Fetch email from OAuth2 user
        String email = oAuth2User.getAttribute("email");
        User userByEmail = (email != null) ? userRepository.findByEmail(email).orElse(null) : null;

        // 4. Handle new user signup
        if (user == null && userByEmail == null) {
            String username = oauth2utils.determineUsernameFromOauth2user(oAuth2User, registrationId, providerId);
            user = registerNewOauth2User(username, email, providerId, providerType);
        }
        // 5. Existing provider user → update email if needed
        else if (user != null) {
            if (email != null && !email.isBlank() && !email.equals(user.getEmail())) {
                user.setEmail(email);
                userRepository.save(user);
            }
        }
        // 6. Email conflict
        else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new UserInfoResponse(null, null, email, "Username or email already exists"));
        }

        // ✅ Build response with proper fields
        UserInfoResponse response = new UserInfoResponse(
                String.valueOf(user.getUsername()),  // id as String (or change DTO type to Long)
                user.getUsername(),
                user.getEmail(),
                providerType.name()
        );

        return ResponseEntity.ok(response);
    }


    public User registerNewOauth2User(String username, String email, String providerId, AuthProviderType providerType) {

        if (username == null || username.isBlank()) {
            
            throw new IllegalArgumentException("Username must not be empty");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ApiException("Username already exists");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new ApiException("Email already exists");
        }

        User newUser = new User(username, null, email); // password null for OAuth2
        newUser.setProviderId(providerId);
        newUser.setProviderType(providerType);
        return userRepository.save(newUser);
    }
}