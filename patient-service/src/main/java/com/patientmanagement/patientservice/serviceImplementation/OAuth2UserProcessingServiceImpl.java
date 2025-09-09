package com.patientmanagement.patientservice.serviceImplementation;

// ... (all necessary imports)

import com.patientmanagement.patientservice.dto.UserInfoResponse;
import com.patientmanagement.patientservice.exception.ApiException;
import com.patientmanagement.patientservice.model.User;
import com.patientmanagement.patientservice.repository.UserRepository;
import com.patientmanagement.patientservice.security.JwtUtils;
import com.patientmanagement.patientservice.security.Oauth2utils;
import com.patientmanagement.patientservice.service.OAuth2UserProcessingService;
import com.patientmanagement.patientservice.util.enums.AuthProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserProcessingServiceImpl implements OAuth2UserProcessingService {

    private final UserRepository userRepository;
    private final Oauth2utils oauth2utils;
    private final JwtUtils jwtUtils;

    @Override
    public ResponseEntity<UserInfoResponse> handleOauth2loginRequest(OAuth2User oAuth2User, String registrationId) {
        AuthProviderType providerType = oauth2utils.getOauthProvider(registrationId);
        String providerId = oauth2utils.determineProviderIdFromOauth2user(oAuth2User, registrationId);

        User user = userRepository.findByProviderIdAndProviderType(providerId, providerType).orElse(null);

        String email = oAuth2User.getAttribute("email");
        User userByEmail = (email != null) ? userRepository.findByEmail(email).orElse(null) : null;

        if (user == null && userByEmail == null) {
            log.info("Registering new user via OAuth2");
            String username = oauth2utils.determineUsernameFromOauth2user(oAuth2User, registrationId, providerId);
            user = registerNewOauth2User(username, email, providerId, providerType);
        } else if (user != null) {
            log.info("Updating existing OAuth2 user");
            if (email != null && !email.isBlank() && !email.equals(user.getEmail())) {
                user.setEmail(email);
                userRepository.save(user);
            }
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new UserInfoResponse(null, null, email, "Username or email already exists"));
        }

        // ✅ assign roles (default: USER, or from DB)
        List<String> roles = List.of("ROLE_USER");

        // ✅ generate JWT
        String token = jwtUtils.generateTokenFromUsername(user.getUsername());

        // ✅ return JWT response
        UserInfoResponse response = new UserInfoResponse(token, user.getUsername(), roles);

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

        User newUser = new User(username, null, email);
        newUser.setProviderId(providerId);
        newUser.setProviderType(providerType);
        return userRepository.save(newUser);
    }
}
