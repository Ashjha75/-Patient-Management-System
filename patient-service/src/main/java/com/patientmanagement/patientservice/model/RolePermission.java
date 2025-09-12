package com.patientmanagement.patientservice.model;

import com.patientmanagement.patientservice.util.enums.Permission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "role_permissions")
@Getter
@Setter
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    // This stores the set of granted permissions (CREATE, VIEW, etc.) for this module
    @ElementCollection(targetClass = Permission.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "granted_permissions", joinColumns = @JoinColumn(name = "role_permission_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    private Set<Permission> grantedPermissions;
}