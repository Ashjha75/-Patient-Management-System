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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTests {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    private UserRequestDto userRequestDto;
    private User mockUserDetails;

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto();
        userRequestDto.setUsername("username");
        userRequestDto.setPassword("password");

        mockUserDetails = new User(
                userRequestDto.getUsername(),
                userRequestDto.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))
        );

        log.info("✅ Test setup completed with username={} and role=ROLE_PATIENT", userRequestDto.getUsername());
    }

    @Test
    @DisplayName("Should return UserInfoResponse on successful authentication")
    void authenticateUser_withValidCredentials_shouldReturnSuccessResponse() {
        log.info("➡️ Starting test: authenticateUser_withValidCredentials_shouldReturnSuccessResponse");

        // Arrange
        Authentication mockAuthentication =
                new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        when(jwtUtils.generateTokenFromUsername(userRequestDto.getUsername()))
                .thenReturn("token-mocked");

        log.debug("🔧 Mocks configured: AuthenticationManager + JwtUtils");

        // Act
        ResponseEntity<UserInfoResponse> response = authServiceImpl.authenticateUser(userRequestDto);

        log.info("📡 Service call completed. HTTP Status = {}", response.getStatusCode());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected OK response");
        assertNotNull(response.getBody(), "Response body should not be null");

        UserInfoResponse body = response.getBody();
        log.info("✅ Authentication successful -> Username={}, Token={}, Roles={}",
                body.getUserName(), body.getJwtToken(), body.getRoles());

        assertEquals("username", body.getUserName());
        assertEquals("token-mocked", body.getJwtToken());
        assertEquals(List.of("ROLE_PATIENT"), body.getRoles());

        log.info("🎉 Test passed: authenticateUser_withValidCredentials_shouldReturnSuccessResponse");
    }
}
