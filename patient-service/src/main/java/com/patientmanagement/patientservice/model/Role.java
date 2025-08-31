package com.patientmanagement.patientservice.model;

import com.patientmanagement.patientservice.util.enums.AppRoles;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "roles") // ✅ explicit table name
@NoArgsConstructor
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Enumerated(EnumType.STRING) // ✅ stored as String in DB
    @Column(length = 30, name = "role_name", nullable = false, unique = true)
    @ToString.Exclude
    private AppRoles roleName;

    public Role(AppRoles roleName) {
        this.roleName = roleName;
    }
}
