package com.patientmanagement.patientservice.repository;

import com.patientmanagement.patientservice.model.RefreshToken;
import com.patientmanagement.patientservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // Use this to delete the token when a user logs out or is disabled
    @Modifying
    int deleteByUser(User user);

    Optional<RefreshToken> findByUser(User user);
}