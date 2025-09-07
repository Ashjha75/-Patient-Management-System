package com.patientmanagement.patientservice.repository;

import com.patientmanagement.patientservice.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Integer> {
    Optional<Module> findByModuleKey(String key);
}
