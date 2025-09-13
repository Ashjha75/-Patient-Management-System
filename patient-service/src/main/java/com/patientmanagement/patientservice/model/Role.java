package com.patientmanagement.patientservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id; // Using Long is generally safer for IDs than Integer

    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;

    // ✅ RENAMED from "permissions" to "rolePermissions" to match the query.
    // FetchType.LAZY is strongly recommended to avoid performance issues.
    // The JOIN FETCH in your query will override this and load them eagerly when needed.
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<RolePermission> rolePermissions = new HashSet<>();

    public Role(String roleName) {
        this.roleName = roleName;
    }
}