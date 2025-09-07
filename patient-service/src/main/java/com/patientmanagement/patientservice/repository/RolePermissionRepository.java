package com.patientmanagement.patientservice.repository;

import com.patientmanagement.patientservice.model.Module;
import com.patientmanagement.patientservice.model.Role;
import com.patientmanagement.patientservice.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Integer> {
    Optional<RolePermission> findByRoleAndModule(Role role, Module module);
}
