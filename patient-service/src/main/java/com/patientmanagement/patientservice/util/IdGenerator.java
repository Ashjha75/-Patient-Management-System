package com.patientmanagement.patientservice.util;

import java.security.SecureRandom;

/**
 * Utility class for generating unique, random alphanumeric IDs for patients.
 * <p>
 * This class uses a secure random number generator to create a Base62 string
 * of configurable length. The generated IDs are suitable for use as primary keys
 * in high-scale systems and are URL-safe.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 *     String patientId = IdGenerator.generatePatientId();
 * </pre>
 * </p>
 */
public class IdGenerator {
    // Characters allowed in the generated ID (Base62)
    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    // Length of the generated ID
    private static final int ID_LENGTH = 14;
    // Secure random number generator for strong randomness
    private static final SecureRandom RANDOM = new SecureRandom();

    // Prevent instantiation
    private IdGenerator() {
    }

    /**
     * Generates a new unique patient ID.
     *
     * @return a random alphanumeric string of length {@link #ID_LENGTH}
     */
    public static String generatePatientId() {
        StringBuilder sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}

//Patient patient = new Patient();
//patient.setId(IdGenerator.generatePatientId());
// set other fields...
//        patientRepository.save(patient);