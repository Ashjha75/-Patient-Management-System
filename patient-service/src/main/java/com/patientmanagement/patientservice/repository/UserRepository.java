package com.patientmanagement.patientservice.repository;

import com.patientmanagement.patientservice.model.User;
import com.patientmanagement.patientservice.util.enums.AuthProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u " + "LEFT JOIN FETCH u.roles r " + "LEFT JOIN FETCH r.permissions p " + "LEFT JOIN FETCH p.module " + "WHERE u.username = :username")
    Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);

    Optional<User> findByProviderIdAndProviderType(String providerId, AuthProviderType authProviderType);
}
