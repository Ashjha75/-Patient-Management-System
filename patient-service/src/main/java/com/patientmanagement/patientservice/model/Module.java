package com.patientmanagement.patientservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "modules")
@Getter
@Setter
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_name", nullable = false, unique = true)
    private String name;

    // This is the unique key you use for permission checks, e.g., "USER_MANAGEMENT"
    @Column(name = "module_key", nullable = false, unique = true)
    private String moduleKey;

    // The base URL path for the module, e.g., "/api/admin/users"
    @Column(name = "url_path", nullable = false)
    private String urlPath;
}