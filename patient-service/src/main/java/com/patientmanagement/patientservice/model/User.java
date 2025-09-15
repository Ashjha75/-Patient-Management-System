package com.patientmanagement.patientservice.model;

import com.patientmanagement.patientservice.util.enums.AuthProviderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_provider", columnList = "provider_id, provider_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true)
    private String username;


    @Column(nullable = true)
    private String password;


    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private boolean enabled = true;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Patient patient;

    private String providerId;

    @Column(name = "user_image")
    private String userImage; // S3 URL for profile image

    @Enumerated(EnumType.STRING)
    private AuthProviderType providerType;


    public User(String username, String password, String email) {
        this.email = email;
        this.password = password;
        this.username = username;
    }

// In your com.patientmanagement.patientservice.model.User class

    // In your com.patientmanagement.patientservice.model.User class

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // We still add the basic roles from the database
        if (this.roles != null) {
            for (Role role : this.roles) {
                authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
            }
        }

        // ======================= TEMPORARY DEBUGGING TEST =======================
        // This block will manually add permissions for the 'superadmin' user
        // to see if the @PreAuthorize check itself works.
        if (this.getUsername().equals("superadmin")) {
            System.out.println("--- DEBUG: HARDCODING PERMISSIONS FOR 'superadmin' ---");
            authorities.add(new SimpleGrantedAuthority("PATIENT_MANAGEMENT:VIEW"));
            authorities.add(new SimpleGrantedAuthority("PATIENT_MANAGEMENT:CREATE"));
            authorities.add(new SimpleGrantedAuthority("PATIENT_MANAGEMENT:EDIT"));
            authorities.add(new SimpleGrantedAuthority("PATIENT_MANAGEMENT:DELETE"));
        }
        // ===================== END OF TEMPORARY DEBUGGING TEST =====================

        System.out.println("--- Final generated authorities for user '" + this.getUsername() + "': " + authorities);

        return authorities;
    }
}

