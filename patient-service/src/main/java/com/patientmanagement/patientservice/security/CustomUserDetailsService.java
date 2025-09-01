package com.patientmanagement.patientservice.security;

import com.patientmanagement.patientservice.repository.UserRepository;
import com.patientmanagement.patientservice.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (!StringUtils.hasText(username)) {
            log.warn("Attempted to load user with empty or null username");
            throw new UsernameNotFoundException("Username cannot be null or empty");
        }

        String trimmedUsername = username.trim();
        log.debug("Loading user details for username: {}", trimmedUsername);

        User user = userRepository.findByUsername(trimmedUsername)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", trimmedUsername);
                    return new UsernameNotFoundException("User not found with username: " + trimmedUsername);
                });

        log.debug("Successfully loaded user: {} with {} roles",
                user.getUsername(), user.getRoles().size());

        // Convert roles to GrantedAuthority
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toList());

        // Return Spring Security User
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                authorities
        );
    }
}
