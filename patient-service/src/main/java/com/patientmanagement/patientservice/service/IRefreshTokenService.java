package com.patientmanagement.patientservice.service;

import com.patientmanagement.patientservice.model.RefreshToken;

import java.util.Optional;

public interface IRefreshTokenService {

    Optional<RefreshToken> findByToken(String token);

    RefreshToken createRefreshToken(String username);

    RefreshToken verifyExpiration(RefreshToken token);

    int deleteByUserId(Long userId);

    void deleteByToken(String token);
}
