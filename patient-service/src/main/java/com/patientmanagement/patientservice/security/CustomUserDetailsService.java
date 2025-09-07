package com.patientmanagement.patientservice.security;

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

import java.util.List;
import java.util.stream.Collectors;


//Validates username input - Checks if username is not null/empty
//Searches user in database - Uses userRepository.findByUsername()
//Throws exception if user not found - UsernameNotFoundException
//Converts user roles to authorities - Maps roles to GrantedAuthority
//Returns Spring Security UserDetails - Creates User object with credentials and authorities
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;


    //    @CacheEvict(value = "userDetails", key = "#user.username")
//  use evict to remove cache in update user when role and data changes to not provide satle info
    @Override
    @Cacheable("userDetails")
    @Transactional(readOnly = true)
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
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getRoleName()))
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
