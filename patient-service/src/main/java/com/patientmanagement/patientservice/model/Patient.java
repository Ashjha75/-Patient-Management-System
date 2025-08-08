package com.patientmanagement.patientservice.model;


    import jakarta.persistence.*;
    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.*;
    import org.hibernate.annotations.CreationTimestamp;
    import org.hibernate.annotations.UpdateTimestamp;

    import java.io.Serial;
    import java.io.Serializable;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.UUID;

    @Entity
    @Table(name = "patients")
    @Data // Lombok: generates getters, setters, toString, equals, hashCode
    @NoArgsConstructor // Lombok: no-args constructor (required by JPA)
    @AllArgsConstructor // Lombok: all-args constructor
    //@Builder // Lombok: builder pattern
    public class Patient implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @NotNull
        @Column(nullable = false)
        private String firstName;

        @NotNull
        @Column(nullable = false)
        private String lastName;

        @NotNull
        @Size(min = 3, max = 20)
        @Column(unique = true, nullable = false)
        private String username;

        @NotNull
        @Email
        @Column(unique = true, nullable = false)
        private String email;

        @NotNull
        @Column(nullable = false)
        private LocalDate dateOfBirth;

        @NotNull
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private Gender gender;

        @NotNull
        @Column(nullable = false)
        private String addressLine1;

        private String addressLine2;

        @NotNull
        @Column(nullable = false)
        private String city;

        @NotNull
        @Column(nullable = false)
        private String state;

        @NotNull
        @Column(nullable = false)
        private String country;

        @NotNull
        @Column(nullable = false)
        private String postalCode;

        @NotNull
        @Column(nullable = false)
        private LocalDate registrationDate;

        @CreationTimestamp
        @Column(updatable = false)
        // Automatically set when the entity is created
        private LocalDateTime createdAt;

        @UpdateTimestamp
        // Automatically updated when the entity is changed
        private LocalDateTime updatedAt;

        // Enum for gender to restrict values
        public enum Gender {
            MALE, FEMALE, OTHER
        }
    }