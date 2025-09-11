package com.patientmanagement.patientservice.model;

import com.patientmanagement.patientservice.util.enums.AuthProviderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    private boolean enabled = true;
    // Roles (ADMIN, SUBADMIN, DOCTOR, PATIENT, etc.)
    @ManyToMany(fetch = FetchType.EAGER)
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Add the roles themselves (e.g., "ROLE_ADMIN"), which can still be useful.
        for (Role role : this.roles) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
        }

        // 2. Add the specific permissions derived from the roles.
        // This is the core of the dynamic RBAC system.
        this.roles.stream()
                // Go into each role
                .flatMap(role -> role.getPermissions().stream())
                // For each RolePermission mapping...
                .forEach(rolePermission -> {
                    // Get the module key (e.g., "PATIENT")
                    String moduleKey = rolePermission.getModule().getModuleKey();
                    // Get the granted permissions (e.g., CREATE, VIEW)
                    rolePermission.getGrantedPermissions().forEach(permission -> {
                        // Create the final permission string and add it as an authority
                        authorities.add(new SimpleGrantedAuthority(moduleKey + ":" + permission.name()));
                    });
                });

        return authorities;
    }
    

}
