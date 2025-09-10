package com.patientmanagement.patientservice.repository;

import com.patientmanagement.patientservice.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {


    boolean existsByUsername(String username);


    Patient findByUsername(String username);
}
