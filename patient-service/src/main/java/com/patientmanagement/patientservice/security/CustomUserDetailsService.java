package com.patientmanagement.patientservice.security;

import com.patientmanagement.patientservice.model.Role;
import com.patientmanagement.patientservice.model.RolePermission;
import com.patientmanagement.patientservice.model.User;
import com.patientmanagement.patientservice.repository.UserRepository;
import com.patientmanagement.patientservice.util.enums.Permission;
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

        log.info("Executing loadUserByUsername for {} - Cache was missed.", username);

        if (!StringUtils.hasText(username)) {
            throw new UsernameNotFoundException("Username cannot be null or empty");
        }

        User user = userRepository.findByUsernameWithAllPermissions(username.trim()).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username: " + username.trim())
        );

        List<GrantedAuthority> authorities = new ArrayList<>();
        Set<Role> roles = user.getRoles();

        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
            for (RolePermission rolePermission : role.getRolePermissions()) {
                String moduleKey = rolePermission.getModule().getModuleKey();
                for (Permission permissionAction : rolePermission.getGrantedPermissions()) {
                    String fullPermission = moduleKey + ":" + permissionAction.name();
                    authorities.add(new SimpleGrantedAuthority(fullPermission));
                }
            }
        }

        log.info("Final authorities loaded for user {}: {}", username, authorities);

        if (!StringUtils.hasText(user.getPassword())) {
            throw new UsernameNotFoundException("Local user has no password configured");
        }

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.isEnabled(), true, true, true, authorities);
    }
}