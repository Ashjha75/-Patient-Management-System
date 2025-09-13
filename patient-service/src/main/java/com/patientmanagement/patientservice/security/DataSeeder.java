package com.patientmanagement.patientservice.security;

import com.patientmanagement.patientservice.model.Role;
import com.patientmanagement.patientservice.model.RolePermission;
import com.patientmanagement.patientservice.model.User;
import com.patientmanagement.patientservice.repository.ModuleRepository;
import com.patientmanagement.patientservice.repository.RolePermissionRepository;
import com.patientmanagement.patientservice.repository.RoleRepository;
import com.patientmanagement.patientservice.repository.UserRepository;
import com.patientmanagement.patientservice.util.enums.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Create Modules
        Module patientModule = createModuleIfNotFound("Patient Management", "PATIENT_MANAGEMENT", "/api/v1/patients");
        Module userModule = createModuleIfNotFound("User Management", "USER_MANAGEMENT", "/api/v1/users");

        // ============================ CORE FIX ============================
        // 2. Create the default role that the signup process needs.
        // We can create it without any permissions, or give it basic ones.
        Role userRole = roleRepository.findByRoleName("ROLE_USER").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setRoleName("ROLE_USER");
            return roleRepository.save(newRole);
        });
        // =================================================================

        // 3. Create other Roles and grant them Permissions
        Role patientRole = createRoleAndAssignPermissions("ROLE_PATIENT", patientModule,
                Set.of(Permission.VIEW_OWN, Permission.EDIT_OWN));

        Role adminRole = createRoleAndAssignPermissions("ROLE_ADMIN", patientModule,
                Set.of(Permission.CREATE, Permission.VIEW, Permission.EDIT, Permission.DELETE, Permission.LIST));

        assignPermissionsToRole(adminRole, userModule, Set.of(Permission.CREATE, Permission.VIEW, Permission.EDIT, Permission.DELETE, Permission.LIST));


        // 4. Create sample Users and assign them Roles
        if (userRepository.findByUsername("janesmith").isEmpty()) {
            User patientUser = new User("janesmith", passwordEncoder.encode("password"), "jane.smith@example.com");
            patientUser.setRoles(Set.of(patientRole)); // This user is a patient
            userRepository.save(patientUser);
        }

        if (userRepository.findByUsername("superadmin2").isEmpty()) {
            User adminUser = new User("superadmin2", passwordEncoder.encode("adminpass"), "admin@example.com");
            adminUser.setRoles(Set.of(adminRole)); // This user is an admin
            userRepository.save(adminUser);
        }
    }

    // Helper methods remain the same
    private Module createModuleIfNotFound(String name, String key, String path) {
        return moduleRepository.findByModuleKey(key).orElseGet(() -> {
            Module module = new Module();
            module.setName(name);
            module.setModuleKey(key);
            module.setUrlPath(path);
            return moduleRepository.save(module);
        });
    }

    private Role createRoleAndAssignPermissions(String roleName, Module module, Set<Permission> permissions) {
        Role role = roleRepository.findByRoleName(roleName).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setRoleName(roleName);
            return roleRepository.save(newRole);
        });
        assignPermissionsToRole(role, module, permissions);
        return role;
    }

    private void assignPermissionsToRole(Role role, Module module, Set<Permission> permissions) {
        Optional<RolePermission> existingPermission = rolePermissionRepository.findByRoleAndModule(role, module);
        if (existingPermission.isEmpty()) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setModule(module);
            rolePermission.setGrantedPermissions(permissions);
            rolePermissionRepository.save(rolePermission);
        }
    }
}