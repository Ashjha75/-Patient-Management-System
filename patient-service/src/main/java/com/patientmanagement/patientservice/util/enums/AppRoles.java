package com.patientmanagement.patientservice.util.enums;

public enum AppRoles {
    ROLE_ADMIN,        // Super admin → can manage everything
    ROLE_SUBADMIN,     // Created by ADMIN, limited permissions
    ROLE_DOCTOR,       // Can view/manage patients, write prescriptions
    ROLE_PATIENT,      // Normal user (signs up), can view their profile & appointments
    ROLE_NURSE,        // Assist doctors, manage patient vitals
    ROLE_RECEPTIONIST, // Manage appointments & registrations
    ROLE_PHARMACIST    // Can manage medicines / prescriptions (optional if needed)
}
