package com.patientmanagement.patientservice.repository;

import com.patientmanagement.patientservice.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(@NotBlank(message = "Email is required", groups = {RegistrationValidationGroup.class}) @Email(message = "Provide valid email") String email);
}
