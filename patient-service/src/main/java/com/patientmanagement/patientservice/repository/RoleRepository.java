package com.patientmanagement.patientservice.repository;

import com.patientmanagement.patientservice.model.Role;
import com.patientmanagement.patientservice.util.enums.AppRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRoles roleName);
}
