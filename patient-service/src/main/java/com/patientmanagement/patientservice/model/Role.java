package com.patientmanagement.patientservice.model;

import com.patientmanagement.patientservice.util.enums.AppRoles;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    //    as default Enum is Integer in Database
    @ToString.Exclude
    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "role_name")
    private AppRoles roleName;

    public Role(AppRoles role) {
        this.roleName = role;
    }
}
