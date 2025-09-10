package com.patientmanagement.patientservice.util;

import java.util.regex.Pattern;

public class IsValidEmail {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Validates whether the given email is non-null, non-empty, and matches pattern.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String trimmed = email.trim();
        return !trimmed.isEmpty() && EMAIL_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Example usage in service method.
     */
//    public void updatePatientEmail(Patient patient, PatientRequestDto patientRequestDto) {
//        String newEmail = patientRequestDto.getEmail();
//
//        if (!isValidEmail(newEmail)) {
//            throw new IllegalArgumentException("Invalid email format");
//        }
//
//        // Compare once, using trimmed
//        String trimmedEmail = newEmail.trim();
//        if (!Objects.equals(patient.getEmail(), trimmedEmail)) {
//            // Perform uniqueness check in DB
//        }
//    }

}
