package com.patientmanagement.patientservice.serviceImplementation;

import com.patientmanagement.patientservice.exception.ResourceNotFound;
import com.patientmanagement.patientservice.exception.TokenRefreshException;
import com.patientmanagement.patientservice.model.RefreshToken;
import com.patientmanagement.patientservice.repository.RefreshTokenRepository;
import com.patientmanagement.patientservice.repository.UserRepository;
import com.patientmanagement.patientservice.service.IRefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    @Value("${spring.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFound("User", "username", username));

        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }


    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Override
    @Transactional
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(
                userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"))
        );
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }
}
