package com.patientmanagement.patientservice.security;

import com.patientmanagement.patientservice.model.User;
import com.patientmanagement.patientservice.repository.UserRepository;
import com.patientmanagement.patientservice.util.enums.Permission;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * A service for handling dynamic, database-driven permission checks.
 * This service is intended to be called from Spring Security's @PreAuthorize annotation.
 * It features caching to ensure high performance for repeated checks.
 */
@Service("permissionService")
@RequiredArgsConstructor
public class CustomPermissionService {

    private static final Logger log = LoggerFactory.getLogger(CustomPermissionService.class);
    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN"; // Define the super admin role name

    private final UserRepository userRepository;


    /**
     * Checks if the authenticated user has a specific permission for a given module.
     * The results of this method are cached in the 'userPermissions' cache.
     * The cache key is a combination of the user's name, the module, and the permission.
     *
     * @param authentication The Authentication object from the SecurityContext.
     * @param moduleKey      The unique key of the module to check (e.g., "PATIENT_MANAGEMENT").
     * @param permission     The permission to check (e.g., "CREATE", "VIEW").
     * @return true if the user has the permission, false otherwise.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userPermissions", key = "#authentication.name + '_' + #moduleKey + '_' + #permission.toUpperCase()")
    public boolean hasPermission(Authentication authentication, String moduleKey, String permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        log.debug("Checking permission '{}' on module '{}' for user '{}'", permission, moduleKey, username);

        // 1. Super Admin Check: If the user has the SUPER_ADMIN role, grant access immediately.
        if (hasRole(userDetails.getAuthorities(), SUPER_ADMIN_ROLE)) {
            log.debug("User '{}' is a SUPER_ADMIN. Granting access.", username);
            return true;
        }

        // 2. Convert string permission to enum for type-safe comparison
        Permission requiredPermission;
        try {
            requiredPermission = Permission.valueOf(permission.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid permission string '{}' provided. Denying access.", permission);
            return false;
        }

        // 3. Fetch User with all roles and permissions in a single, efficient query.
        // This is the most critical part for performance.
        User user = userRepository.findByUsernameWithRolesAndPermissions(username).orElse(null);

        if (user == null) {
            log.warn("No user found with username '{}'. Denying access.", username);
            return false;
        }

        // 4. Use Java Streams to check for the permission in a declarative way.
        boolean isPermitted = user.getRoles().stream()
                // Get a stream of all RolePermission objects from all roles the user has
                .flatMap(role -> role.getPermissions().stream())
                // Find a match where the module key and the required permission are present
                .anyMatch(rolePermission ->
                        rolePermission.getModule().getModuleKey().equalsIgnoreCase(moduleKey) &&
                                rolePermission.getGrantedPermissions().contains(requiredPermission)
                );

        log.debug("Permission check for user '{}' on module '{}' with permission '{}' result: {}", username, moduleKey, permission, isPermitted);
        return isPermitted;
    }

    /**
     * A helper method to check if a collection of authorities contains a specific role name.
     *
     * @param authorities The collection of GrantedAuthority objects.
     * @param roleName    The role name to check for (e.g., "SUPER_ADMIN").
     * @return true if the role is found, false otherwise.
     */
    private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String roleName) {
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleName));
    }
}