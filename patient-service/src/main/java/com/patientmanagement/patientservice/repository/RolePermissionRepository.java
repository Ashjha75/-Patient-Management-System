package com.patientmanagement.patientservice.repository;

import com.patientmanagement.patientservice.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Integer> {
}
