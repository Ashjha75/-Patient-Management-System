package com.patientmanagement.patientservice.security;

import com.patientmanagement.patientservice.model.Role;
import com.patientmanagement.patientservice.model.User; // ✅ IMPORTANT: Import your custom User entity
import com.patientmanagement.patientservice.repository.RoleRepository;
import com.patientmanagement.patientservice.repository.UserRepository;
import com.patientmanagement.patientservice.util.enums.AppRoles;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;
    // ✅ Renamed fields to follow Java conventions
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // ✅ Renamed parameters to match fields
    public SecurityConfig(AuthEntryPointJwt unauthorizedHandler, UserRepository userRepository, RoleRepository roleRepository) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Bean
    public AuthTokenFilter authenticationTokenFilterBean() {
        return new AuthTokenFilter();
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/public/**", "/api/v1/docs/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/api/v1/swagger-ui/**", "/api/v1/swagger-ui.html", "/api/v1/swagger-resources/**", "/webjars/**").permitAll()
                        .requestMatchers("/health", "/favicon.ico").permitAll()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        http.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "https://mydomain.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(Duration.ofHours(1));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // ✅ REMOVED the conflicting JdbcUserDetailsManager bean

    @Bean
    CommandLineRunner init(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Role userRole = roleRepository.findByRoleName(AppRoles.ROLE_PATIENT)
                    .orElseGet(() -> roleRepository.save(new Role(AppRoles.ROLE_PATIENT)));

            Role adminRole = roleRepository.findByRoleName(AppRoles.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(AppRoles.ROLE_ADMIN)));

            if (userRepository.findByUsername("user1").isEmpty()) {
                // ✅ Correctly creating an instance of YOUR User entity
                User user1 = new User();
                user1.setUsername("user1");
                user1.setPassword(passwordEncoder.encode("password1"));
                user1.setEmail("user1@example.com");
                user1.setEnabled(true);
                user1.setRoles(new HashSet<>(Set.of(userRole)));
                userRepository.save(user1);
            }

            if (userRepository.findByUsername("admin").isEmpty()) {
                // ✅ Correctly creating an instance of YOUR User entity
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin1"));
                admin.setEmail("admin@example.com");
                admin.setEnabled(true);
                admin.setRoles(new HashSet<>(Set.of(adminRole)));
                userRepository.save(admin);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}