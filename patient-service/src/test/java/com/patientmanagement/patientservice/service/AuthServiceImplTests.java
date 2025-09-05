package com.patientmanagement.patientservice.service;

import com.patientmanagement.patientservice.dto.UserInfoResponse;
import com.patientmanagement.patientservice.dto.UserRequestDto;
import com.patientmanagement.patientservice.security.JwtUtils;
import com.patientmanagement.patientservice.security.TokenBlacklistService;
import com.patientmanagement.patientservice.serviceImplementation.AuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTests {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    private UserRequestDto userRequestDto;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto();
        userRequestDto.setUsername("username");
        userRequestDto.setPassword("password");

        mockUserDetails = new User(
                "username",
                "password",
                Collections.emptyList()
        );

        SecurityContextHolder.clearContext();
        log.info("✅ Test setup completed. SecurityContext cleared.");
    }

    @Test
    @DisplayName("Should return UserInfoResponse on successful authentication")
    void authenticateUser_withValidCredentials_shouldReturnSuccessResponse() {
        log.info("➡️ Starting SUCCESS test");

        // ARRANGE
        Authentication mockAuthentication =
                new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        when(jwtUtils.generateTokenFromUsername(userRequestDto.getUsername()))
                .thenReturn("token-mocked");

        // ACT
        ResponseEntity<UserInfoResponse> response = authServiceImpl.authenticateUser(userRequestDto);

        // ASSERT
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        log.info("✅ Success -> Username={}, Token={}",
                response.getBody().getUserName(), response.getBody().getJwtToken());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized on failed authentication")
    void authenticateUser_withInvalidCredentials_shouldReturnUnauthorized() {
        log.info("➡️ Starting FAILURE test (invalid credentials)");

        // ARRANGE
        UserRequestDto invalidUserRequest = new UserRequestDto();
        invalidUserRequest.setUsername("testuser");
        invalidUserRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // ACT
        ResponseEntity<UserInfoResponse> response = authServiceImpl.authenticateUser(invalidUserRequest);

        // ASSERT
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "Expected 401 Unauthorized");
        assertNull(response.getBody(), "Body should be null on failure");
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "SecurityContext should remain empty");

        log.warn("❌ Authentication failed for username={} -> Status={}",
                invalidUserRequest.getUsername(), response.getStatusCode());
    }
}
