package com.patientmanagement.patientservice.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // For demonstration, return a dummy user. Replace with DB lookup in production.
        if (username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("Username is empty");
        }
        // Password is 'password123' encoded with bcrypt for demo purposes
        String encodedPassword = "$2a$10$Dow1QwQwQwQwQwQwQwQwQeQwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQw";
        return new User(username, encodedPassword, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}

