package com.patientmanagement.patientservice.security;

import com.patientmanagement.patientservice.model.Role;
import com.patientmanagement.patientservice.model.RolePermission;
import com.patientmanagement.patientservice.model.User;
import com.patientmanagement.patientservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Cacheable(value = "userPermissions", key = "#username")
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (!StringUtils.hasText(username)) {
            log.warn("Attempted to load user with empty or null username");
            throw new UsernameNotFoundException("Username cannot be null or empty");
        }

        String trimmedUsername = username.trim();
        log.debug("Loading user details for username: {}", trimmedUsername);

        User user = userRepository.findByUsernameWithRolesAndPermissions(trimmedUsername).orElseThrow(() -> {
            log.warn("User not found with username: {}", trimmedUsername);
            return new UsernameNotFoundException("User not found with username: " + trimmedUsername);
        });

        log.debug("Successfully loaded user: {} with {} roles", user.getUsername(), user.getRoles().size());

        // ============================ CORE FIX STARTS HERE ============================
        // Create a flat collection to hold ALL authorities: roles AND permissions.
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 1. Get the user's roles
        Set<Role> roles = user.getRoles();

        // 2. Loop through each role to add both the role and its permissions
        for (Role role : roles) {
            // Add the role itself as an authority (e.g., "ROLE_ADMIN")
            // Note: If your role name in the DB already has "ROLE_", use role.getRoleName() directly.
            // If not, you might need to add it: "ROLE_" + role.getRoleName().
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));

            // 3. Get the permissions for THIS role and add them as authorities
            Set<RolePermission> permissions = role.getPermissions();
            for (RolePermission permission : permissions) {
                authorities.add(new SimpleGrantedAuthority(permission.getRole().getRoleName())); // e.g., "PATIENT_MANAGEMENT:VIEW"
            }
        }
        // ============================= CORE FIX ENDS HERE =============================

        // ✅ Handle local vs OAuth users
        if (user.getProviderType() != null && !user.getProviderType().name().equalsIgnoreCase("LOCAL")) {
            // OAuth user → no password required, provide empty string
            return new org.springframework.security.core.userdetails.User(user.getUsername(), "",
                    user.isEnabled(), true, true, true, authorities);
        }

        // Local user → password must be present
        if (!StringUtils.hasText(user.getPassword())) {
            log.error("Local user {} has no password set", user.getUsername());
            throw new UsernameNotFoundException("Local user has no password configured");
        }

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.isEnabled(), true, true, true, authorities);
    }
}