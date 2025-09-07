package com.patientmanagement.patientservice.security;

import com.patientmanagement.patientservice.model.Role;
import com.patientmanagement.patientservice.model.User;
import com.patientmanagement.patientservice.repository.RolePermissionRepository;
import com.patientmanagement.patientservice.repository.RoleRepository;
import com.patientmanagement.patientservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;


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


    @Bean
    CommandLineRunner init(
            UserRepository userRepository,
            RoleRepository roleRepository,
            ModuleRepository moduleRepository,
            RolePermissionRepository rolePermissionRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // 1. Create Modules
            Module patientModule = createModuleIfNotFound(moduleRepository, "Patient Management", "PATIENT_MANAGEMENT", "/api/patients");
            Module userModule = createModuleIfNotFound(moduleRepository, "User Management", "USER_MANAGEMENT", "/api/users");

            // 2. Create Roles and grant them Permissions
            Role patientRole = createRoleAndAssignPermissions(
                    roleRepository,
                    rolePermissionRepository,
                    "PATIENT",
                    patientModule,
                    // A patient can only VIEW their own data (controller logic will enforce 'own')
                    Set.of(Permission.VIEW)
            );

            Role superAdminRole = createRoleAndAssignPermissions(
                    roleRepository,
                    rolePermissionRepository,
                    "SUPER_ADMIN",
                    patientModule,
                    // Admin gets all permissions for the patient module
                    Set.of(Permission.CREATE, Permission.VIEW, Permission.EDIT, Permission.DELETE, Permission.LIST)
            );
            // Also give admin rights to user management
            assignPermissionsToRole(rolePermissionRepository, superAdminRole, userModule, Set.of(Permission.CREATE, Permission.VIEW, Permission.EDIT, Permission.DELETE, Permission.LIST));


            // 3. Create Users and assign them Roles
            if (userRepository.findByUsername("johndoe").isEmpty()) {
                User patientUser = new User("johndoe", passwordEncoder.encode("password123"), "john.doe@example.com");
                patientUser.setRoles(Set.of(patientRole));
                userRepository.save(patientUser);
            }

            if (userRepository.findByUsername("superadmin").isEmpty()) {
                User adminUser = new User("superadmin", passwordEncoder.encode("adminpass"), "admin@example.com");
                adminUser.setRoles(Set.of(superAdminRole));
                userRepository.save(adminUser);
            }
        };
    }

    // Helper method to avoid duplicating module creation logic
    private Module createModuleIfNotFound(ModuleRepository repo, String name, String key, String path) {
        return repo.findByModuleKey(key).orElseGet(() -> {
            Module module = new Module();
            module.setName(name);
            module.setModuleKey(key);
            module.setUrlPath(path);
            return repo.save(module);
        });
    }

    // Helper method to create a role and assign its initial permissions
    private Role createRoleAndAssignPermissions(RoleRepository roleRepo, RolePermissionRepository permRepo, String roleName, Module module, Set<Permission> permissions) {
        Role role = roleRepo.findByName(roleName).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(roleName);
            return roleRepo.save(newRole);
        });

        assignPermissionsToRole(permRepo, role, module, permissions);
        return role;
    }

    // Helper method to handle the permission assignment logic
    private void assignPermissionsToRole(RolePermissionRepository permRepo, Role role, Module module, Set<Permission> permissions) {
        // Check if a permission set for this role and module already exists
        Optional<RolePermission> existingPermission = permRepo.findByRoleAndModule(role, module);

        if (existingPermission.isEmpty()) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setModule(module);
            rolePermission.setGrantedPermissions(permissions);
            permRepo.save(rolePermission);
        }
    }

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}