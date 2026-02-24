# Authorization Architecture — `patient-service`

> A deep-dive into how every request is authenticated, authorized, and secured inside the `patient-service` microservice.

---

## Table of Contents

1. [High-Level Overview](#1-high-level-overview)
2. [The Security Filter Pipeline](#2-the-security-filter-pipeline)
3. [Data Model — Roles, Modules & Permissions](#3-data-model--roles-modules--permissions)
4. [Component Reference](#4-component-reference)
   - [4.1 SecurityConfig](#41-securityconfig)
   - [4.2 JwtUtils](#42-jwtutils)
   - [4.3 AuthTokenFilter](#43-authtokenfilter)
   - [4.4 AuthEntryPointJwt](#44-authentrypointjwt)
   - [4.5 CustomUserDetailsService](#45-customuserdetailsservice)
   - [4.6 CustomPermissionService](#46-custompermissionservice)
   - [4.7 TokenBlacklistService](#47-tokenblacklistservice)
   - [4.8 Oauth2SuccessHandler & Oauth2utils](#48-oauth2successhandler--oauth2utils)
   - [4.9 ApplicationConfig](#49-applicationconfig)
   - [4.10 DataSeeder](#410-dataseeder)
5. [Auth Endpoints](#5-auth-endpoints)
6. [Protecting Endpoints with @PreAuthorize](#6-protecting-endpoints-with-preauthorize)
7. [Request Lifecycle — Step by Step](#7-request-lifecycle--step-by-step)
8. [OAuth2 / Social Login Flow](#8-oauth2--social-login-flow)
9. [Token Blacklist & Logout](#9-token-blacklist--logout)
10. [Caching Strategy](#10-caching-strategy)
11. [Roles & Permissions Quick Reference](#11-roles--permissions-quick-reference)
12. [Public vs Protected Routes](#12-public-vs-protected-routes)
13. [Security Design Principles](#13-security-design-principles)

---

## 1. High-Level Overview

The `patient-service` uses a **stateless, JWT-based security model** combined with a **fine-grained, database-driven RBAC (Role-Based Access Control)** system.

```
HTTP Request
     │
     ▼
┌──────────────────────────────┐
│      CORS Filter             │  ← Validates cross-origin policies
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│    AuthTokenFilter           │  ← Extracts & validates JWT
│  (OncePerRequestFilter)      │  ← Loads user + authorities into SecurityContext
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│   Spring Security            │  ← Checks if path is public or protected
│   Authorization Rules        │  ← Evaluates @PreAuthorize on controller methods
└──────────────┬───────────────┘
               │
       ┌───────┴────────┐
       ▼                ▼
  ✅ ALLOWED        ❌ DENIED
  (controller)   (AuthEntryPointJwt
                  → 401 JSON response)
```

**Key Characteristics:**
- **Stateless** — No server-side session. All auth state lives in the JWT.
- **RBAC** — Users have Roles → Roles have module-scoped Permissions → Permissions map to `@PreAuthorize` expressions.
- **Social Login** — Google and GitHub OAuth2 flows are supported alongside traditional username/password login.
- **Token Blacklisting** — Logged-out tokens are tracked in memory to prevent reuse.
- **Caching** — User permission lookups are cached to minimise database round-trips.

---

## 2. The Security Filter Pipeline

Spring Security processes every incoming HTTP request through a filter chain. Here is the order of execution:

```
Incoming HTTP Request
        │
        ▼
[1] CorsFilter               → Enforces CORS policy (allowed origins, methods, headers)
        │
        ▼
[2] AuthTokenFilter          → Reads "Authorization: Bearer <token>" header
   ├─ Calls JwtUtils.validateJwtToken()
   ├─ Calls JwtUtils.getUserNameFromJwtToken()
   ├─ Calls CustomUserDetailsService.loadUserByUsername()
   └─ Populates SecurityContextHolder with UsernamePasswordAuthenticationToken
        │
        ▼
[3] UsernamePasswordAuthenticationFilter  (standard Spring Security — skipped for JWT)
        │
        ▼
[4] Spring Security Authorization Filter
   ├─ Public path?  → pass through
   └─ Protected?    → check SecurityContext for valid Authentication
        │
        ▼
[5] @PreAuthorize evaluation (Method Security)
   └─ hasAuthority('MODULE_KEY:PERMISSION')
        │
        ▼
[6] Controller method executes
```

If step [2] finds no valid token, the `SecurityContext` remains anonymous. If a protected endpoint is then reached, `AuthEntryPointJwt` intercepts and returns HTTP `401 Unauthorized`.

---

## 3. Data Model — Roles, Modules & Permissions

The RBAC system is built on four core entities:

```
┌─────────┐       ┌────────────┐       ┌─────────────────┐       ┌──────────────┐
│  User   │───┐   │    Role    │───┐   │ RolePermission  │───┐   │   Module     │
├─────────┤   │   ├────────────┤   │   ├─────────────────┤   │   ├──────────────┤
│ userId  │   │   │ roleId     │   │   │ id              │   │   │ id           │
│ username│   │   │ roleName   │   │   │ role_id (FK)    │   │   │ name         │
│ password│   │   │            │   │   │ module_id (FK)  │───┘   │ moduleKey    │
│ email   │   │   │            │   │   │ grantedPerms[]  │       │ urlPath      │
│ enabled │   │   │            │   │   │  (SET<Perm>)    │       └──────────────┘
│ roles[] │───┘   │            │───┘   └─────────────────┘
└─────────┘       └────────────┘
  ManyToMany        OneToMany             ManyToOne → Module
  (user_roles       (role_permissions     @ElementCollection
   join table)       join table)          (granted_permissions
                                           join table)
```

### The `Permission` Enum

All possible actions are defined in `Permission.java`:

| Value    | Meaning                                        | Typical HTTP Method |
|----------|------------------------------------------------|---------------------|
| `CREATE` | Create a new entity                            | `POST`              |
| `VIEW`   | View details of a single entity                | `GET /{id}`         |
| `EDIT`   | Modify an existing entity                      | `PUT` / `PATCH`     |
| `DELETE` | Remove an entity                               | `DELETE`            |
| `LIST`   | Retrieve a paginated collection of entities    | `GET /all`          |

### How Authorities Are Assembled

When `CustomUserDetailsService.loadUserByUsername()` runs, it builds a flat list of `GrantedAuthority` objects by combining each Role's module permissions:

```
User "superadmin2" has role ROLE_ADMIN
  → ROLE_ADMIN has RolePermission for module PATIENT_MANAGEMENT with [CREATE, VIEW, EDIT, DELETE, LIST]
  → ROLE_ADMIN has RolePermission for module USER_MANAGEMENT with [CREATE, VIEW, EDIT, DELETE, LIST]

Resulting GrantedAuthority list:
  - ROLE_ADMIN
  - PATIENT_MANAGEMENT:CREATE
  - PATIENT_MANAGEMENT:VIEW
  - PATIENT_MANAGEMENT:EDIT
  - PATIENT_MANAGEMENT:DELETE
  - PATIENT_MANAGEMENT:LIST
  - USER_MANAGEMENT:CREATE
  - USER_MANAGEMENT:VIEW
  - USER_MANAGEMENT:EDIT
  - USER_MANAGEMENT:DELETE
  - USER_MANAGEMENT:LIST
```

These authorities are stored in the `SecurityContext` and evaluated by `@PreAuthorize` expressions at the controller level.

---

## 4. Component Reference

### 4.1 `SecurityConfig`

**File:** `security/SecurityConfig.java`  
**Annotations:** `@Configuration`, `@EnableWebSecurity`, `@EnableMethodSecurity`

This is the central security configuration class. It does four important things:

#### a) Registers the JWT Filter

```java
http.addFilterBefore(
    authenticationTokenFilterBean(),
    UsernamePasswordAuthenticationFilter.class
);
```

`AuthTokenFilter` runs *before* Spring Security's default username/password filter, so it intercepts and processes JWTs first.

#### b) Defines Public vs Protected Routes

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(
        "/api/auth/**",       // Login, signup, logout, refresh
        "/api/v1/auth/**",
        "/api/public/**",
        "/v3/api-docs/**",    // Swagger/OpenAPI docs
        "/api/v1/swagger-ui/**",
        "/health",
        "/oauth2/**",         // OAuth2 provider redirects
        "/login/oauth2/**",
        "/verify-email"       // Email verification callback
    ).permitAll()
    .anyRequest().authenticated()   // Everything else needs a valid JWT
)
```

#### c) Configures Stateless Session Management

```java
.sessionManagement(sess ->
    sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

No `HttpSession` is ever created. Spring Security never stores auth state on the server.

#### d) Sets Up CORS

Allowed origins: `http://localhost:*` and `https://mydomain.com`  
Allowed methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`  
Credentials: allowed  
Max age: 1 hour

#### e) Enables Method-Level Security

`@EnableMethodSecurity` activates support for `@PreAuthorize`, `@PostAuthorize`, and `@Secured` annotations on controller methods. Without this annotation, `@PreAuthorize` expressions would be silently ignored.

---

### 4.2 `JwtUtils`

**File:** `security/JwtUtils.java`  
**Annotation:** `@Component`

The single source of truth for all JWT operations. Reads its configuration from `application.properties`:

| Property                      | Purpose                              |
|-------------------------------|--------------------------------------|
| `spring.app.jwtSecret`        | Base64-encoded HMAC-SHA signing key  |
| `spring.app.expirationTimeMS` | Token lifetime in milliseconds       |
| `spring.app.cookieName`       | Cookie name (reserved for future use)|

#### Key Methods

| Method                              | Description                                                                 |
|-------------------------------------|-----------------------------------------------------------------------------|
| `getJwtFromHeader(request)`         | Reads `Authorization` header and strips the `Bearer ` prefix               |
| `generateTokenFromUsername(user)`   | Creates a signed JWT with `sub` (username), `roles` claim, `iat`, and `exp`|
| `getUserNameFromJwtToken(token)`    | Parses and returns the `sub` claim from a valid token                       |
| `validateJwtToken(token)`           | Verifies signature, format, and expiry; logs specific error per failure type|
| `getExpirationFromToken(token)`     | Returns token expiry as `LocalDateTime` (used by blacklist service)         |

#### Token Structure

A generated JWT contains:

```json
{
  "sub": "superadmin2",
  "roles": "Admin",
  "iat": 1720000000,
  "exp": 1720086400
}
```

> ⚠️ **Note:** The `roles` claim in the token is currently hardcoded to `"Admin"`. The authoritative role/permission data is always re-loaded from the database via `CustomUserDetailsService` on each request — the JWT only carries the `username` as a lookup key.

#### Signing Algorithm

Tokens are signed using **HMAC-SHA** (via `Keys.hmacShaKeyFor`) with the Base64-decoded `jwtSecret`. The key length determines the specific SHA variant (e.g., SHA-256, SHA-384, SHA-512).

---

### 4.3 `AuthTokenFilter`

**File:** `security/AuthTokenFilter.java`  
**Extends:** `OncePerRequestFilter`  
**Annotation:** `@Component`

This filter is the gateway for every HTTP request. It runs exactly once per request (guaranteed by `OncePerRequestFilter`).

#### Execution Flow

```
doFilterInternal()
      │
      ├─ parseJwt(request)
      │    └─ reads "Authorization" header
      │    └─ strips "Bearer " prefix → returns raw JWT string
      │
      ├─ jwtUtils.validateJwtToken(jwt)
      │    ├─ valid?  → continue
      │    └─ invalid? → log error, skip auth setup, continue filter chain (anonymous)
      │
      ├─ jwtUtils.getUserNameFromJwtToken(jwt)
      │    └─ extracts "sub" claim → username string
      │
      ├─ userDetailsService.loadUserByUsername(username)
      │    └─ fetches user + roles + permissions from DB (or cache)
      │
      ├─ new UsernamePasswordAuthenticationToken(userDetails, null, authorities)
      │    └─ null credentials (password not needed after token validation)
      │
      ├─ authentication.setDetails(WebAuthenticationDetailsSource...)
      │    └─ attaches IP address and session ID from HTTP request
      │
      └─ SecurityContextHolder.getContext().setAuthentication(authentication)
           └─ marks this request as "authenticated"
```

If **any step fails** (invalid token, user not found, etc.), the filter logs the error and calls `filterChain.doFilter()` without setting authentication. The request proceeds as anonymous, and `AuthEntryPointJwt` will handle the 401 response if the endpoint is protected.

> **Note on Token Blacklisting:** The filter contains a commented-out call to `tokenBlacklistService.isTokenBlacklisted(jwt)`. When uncommented, this check prevents blacklisted (logged-out) tokens from authenticating. The infrastructure is ready; it just needs to be enabled.

---

### 4.4 `AuthEntryPointJwt`

**File:** `security/AuthEntryPointJwt.java`  
**Implements:** `AuthenticationEntryPoint`  
**Annotation:** `@Component`

This is the **error handler** for unauthenticated requests. Spring Security calls `commence()` whenever a request reaches a protected endpoint without valid authentication.

#### Response Format

Instead of returning Spring's default HTML error page, it writes a structured JSON body:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required to access this resource",
  "path": "/api/v1/patients/all-patients",
  "timestamp": "2024-07-04T12:00:00.000Z"
}
```

#### What Triggers It

- Request to any protected URL with **no** `Authorization` header.
- Request with an **expired** JWT token.
- Request with a **malformed** or **invalid signature** JWT.
- JWT token that has been **blacklisted** (logged out).

---

### 4.5 `CustomUserDetailsService`

**File:** `security/CustomUserDetailsService.java`  
**Implements:** `UserDetailsService`  
**Annotations:** `@Service`, `@Transactional(readOnly = true)`

This service is called by `AuthTokenFilter` to load the full user record from the database using the username extracted from the JWT.

#### What It Loads

1. Fetches `User` entity using `userRepository.findByUsernameWithAllPermissions(username)` — a JOIN FETCH query that eagerly loads Roles → RolePermissions → Module + GrantedPermissions in a **single database query**.

2. Builds the `GrantedAuthority` list:
   - **Role names** → e.g., `ROLE_ADMIN`
   - **Module permissions** → e.g., `PATIENT_MANAGEMENT:CREATE`, `USER_MANAGEMENT:VIEW`

3. Returns a Spring Security `UserDetails` object with the full authority set.

#### Authority Format

```
Role name:         ROLE_ADMIN
Module permission: {moduleKey}:{permissionAction}
                   PATIENT_MANAGEMENT:VIEW
                   PATIENT_MANAGEMENT:CREATE
                   USER_MANAGEMENT:DELETE
```

#### Caching

Results are cached under the `"userPermissions"` cache with the username as the key. Subsequent requests for the same user within the cache TTL skip the database entirely.

```java
@Cacheable(value = "userPermissions", key = "#username")
public UserDetails loadUserByUsername(String username) { ... }
```

> ⚠️ **Important:** If a user's roles/permissions change in the database, the cache must be evicted for the change to take effect immediately.

---

### 4.6 `CustomPermissionService`

**File:** `security/CustomPermissionService.java`  
**Annotation:** `@Service("permissionService")`

This is an **alternative** permission evaluation strategy designed for use directly inside `@PreAuthorize` expressions. While `CustomUserDetailsService` bakes permissions into the `SecurityContext` authorities at login time, `CustomPermissionService.hasPermission()` performs a **real-time, database-driven check** at the point of the method call.

#### Usage in SpEL

```java
@PreAuthorize("@permissionService.hasPermission(authentication, 'PATIENT_MANAGEMENT', 'VIEW')")
```

#### How It Works

```
hasPermission(authentication, moduleKey, permission)
      │
      ├─ Is authentication null or anonymous? → return false
      │
      ├─ Does user have SUPER_ADMIN role? → return true immediately
      │
      ├─ Convert permission string to Permission enum (type-safe)
      │    └─ Invalid string? → log warning, return false
      │
      ├─ userRepository.findByUsernameWithRolesAndPermissions(username)
      │    └─ JOIN FETCH: User → Roles → RolePermissions → Module + GrantedPermissions
      │
      └─ user.getRoles().stream()
              .flatMap(role -> role.getRolePermissions().stream())
              .anyMatch(rp ->
                  rp.getModule().getModuleKey().equalsIgnoreCase(moduleKey)
                  && rp.getGrantedPermissions().contains(requiredPermission)
              )
```

#### Caching

Results are cached under `"userPermissions"` with a composite key:

```java
@Cacheable(value = "userPermissions", key = "#authentication.name + '_' + #moduleKey + '_' + #permission.toUpperCase()")
```

Example cache key: `"superadmin2_PATIENT_MANAGEMENT_VIEW"`

#### `SUPER_ADMIN` Bypass

If the authenticated user holds a `SUPER_ADMIN` role, all permission checks are bypassed and `true` is returned without touching the database.

> **Design Note:** The current codebase primarily uses `hasAuthority()` in `@PreAuthorize` (backed by `CustomUserDetailsService`). `CustomPermissionService` is the more advanced, database-live alternative and can be used for dynamic, admin-configurable access control without requiring re-login.

---

### 4.7 `TokenBlacklistService`

**File:** `security/TokenBlacklistService.java`  
**Annotation:** `@Service`

Handles **logout token invalidation**. When a user logs out, their active JWT is added to an in-memory blacklist so it cannot be reused even if it has not yet expired.

#### Storage

```java
private final Map<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();
//                  ▲ raw JWT string        ▲ expiration time
```

Uses `ConcurrentHashMap` for thread-safe concurrent access without synchronization blocks.

#### Key Methods

| Method                                      | Description                                           |
|---------------------------------------------|-------------------------------------------------------|
| `blacklistToken(token, expirationTime)`     | Adds token with explicit expiry                        |
| `blacklistToken(token)`                     | Adds token with default expiry (`app.jwt.expiration-hours`) |
| `isTokenBlacklisted(token)`                 | Returns `true` if token is in the blacklist            |
| `removeExpiredTokens()`                     | Scheduled cleanup — runs every hour (3,600,000 ms)     |
| `clearAllTokens()`                          | Admin-only manual wipe                                 |
| `getBlacklistSize()`                        | Returns current blacklist entry count                  |

#### Automatic Cleanup

```java
@Scheduled(fixedRate = 3600000) // every hour
public void removeExpiredTokens() {
    blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(LocalDateTime.now()));
}
```

This prevents unbounded memory growth. Tokens are removed once they are past their natural expiry (they would be invalid anyway by that point).

#### ⚠️ Current Status

The blacklist check inside `AuthTokenFilter` is **commented out**:

```java
// if (jwt != null && jwtUtils.validateJwtToken(jwt) && !tokenBlacklistService.isTokenBlacklisted(jwt))
```

The infrastructure is complete, but the check is not enforced. Uncomment this line to activate logout invalidation.

#### ⚠️ In-Memory Limitation

The blacklist is stored in application memory. **Blacklisted tokens are lost on server restart** and **not shared across multiple instances** in a clustered deployment. For production, consider migrating to a distributed cache (e.g., Redis).

---

### 4.8 `Oauth2SuccessHandler` & `Oauth2utils`

**Files:** `security/Oauth2SuccessHandler.java`, `security/Oauth2utils.java`

These two classes handle the **OAuth2 social login** flow (Google and GitHub).

#### `Oauth2SuccessHandler`

Called by Spring Security after a successful OAuth2 provider redirect. It:

1. Extracts the `OAuth2AuthenticationToken` from the authentication result.
2. Reads the `registrationId` (e.g., `"google"` or `"github"`).
3. Delegates to `OAuth2UserProcessingService.handleOauth2loginRequest()` to:
   - Look up or create a `User` entity matching the OAuth2 profile.
   - Generate a JWT for that user.
4. Writes the `UserInfoResponse` (containing the JWT) directly to the HTTP response as JSON.

The client receives the same `UserInfoResponse` structure as the regular `/api/auth/signin` endpoint — OAuth2 users are treated identically after the initial handshake.

#### `Oauth2utils`

A stateless utility bean with two helper methods:

| Method                                        | Purpose                                                        |
|-----------------------------------------------|----------------------------------------------------------------|
| `getOauthProvider(registrationId)`            | Maps `"google"` / `"github"` → `AuthProviderType` enum        |
| `determineProviderIdFromOauth2user(user, id)` | Extracts the provider's unique user ID (`sub` for Google, `id` for GitHub) |
| `determineUsernameFromOauth2user(user, id, providerId)` | Prefers `email` attribute; falls back to provider-specific login name |

---

### 4.9 `ApplicationConfig`

**File:** `security/ApplicationConfig.java`  
**Annotations:** `@Configuration`, `@EnableCaching`

Provides two core infrastructure beans:

#### `PasswordEncoder`

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

All passwords are hashed with **BCrypt** before storage. BCrypt is slow by design (adaptive cost factor) to resist brute-force attacks and includes automatic salt generation.

#### `AuthenticationManager`

```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}
```

Exposes Spring's `AuthenticationManager` as a bean so `AuthService` can programmatically call `authenticate()` during the `/api/auth/signin` flow.

#### Caching

`@EnableCaching` activates Spring's caching abstraction, enabling `@Cacheable` annotations in `CustomUserDetailsService` and `CustomPermissionService`.

---

### 4.10 `DataSeeder`

**File:** `security/DataSeeder.java`  
**Implements:** `CommandLineRunner`  
**Annotation:** `@Component`

Automatically seeds the database with default roles, modules, and users **on every application startup** (uses "create if not exists" logic — safe to run repeatedly).

#### What Gets Seeded

**Modules:**

| Module Name        | Module Key           | URL Path            |
|--------------------|----------------------|---------------------|
| Patient Management | `PATIENT_MANAGEMENT` | `/api/v1/patients`  |
| User Management    | `USER_MANAGEMENT`    | `/api/v1/users`     |

**Roles & Their Permissions:**

| Role Name       | Module               | Permissions                                      |
|-----------------|----------------------|--------------------------------------------------|
| `ROLE_USER`     | (none)               | —                                                |
| `ROLE_PATIENT`  | PATIENT_MANAGEMENT   | `VIEW`, `EDIT`                                   |
| `ROLE_ADMIN`    | PATIENT_MANAGEMENT   | `CREATE`, `VIEW`, `EDIT`, `DELETE`, `LIST`       |
| `ROLE_ADMIN`    | USER_MANAGEMENT      | `CREATE`, `VIEW`, `EDIT`, `DELETE`, `LIST`       |

**Seeded Users:**

| Username      | Password      | Role          |
|---------------|---------------|---------------|
| `janesmith`   | `password`    | `ROLE_PATIENT`|
| `superadmin2` | `adminpass`   | `ROLE_ADMIN`  |

> ⚠️ **Security Warning:** The seeded user passwords are hardcoded plaintext values. In any non-development environment these must be changed immediately or loaded from secure environment variables.

---

## 5. Auth Endpoints

All auth endpoints live under `/api/auth/**` and are **publicly accessible** (no JWT required):

| Method | Path                  | Description                                      |
|--------|-----------------------|--------------------------------------------------|
| `POST` | `/api/auth/signup`    | Register a new user account                      |
| `POST` | `/api/auth/signin`    | Authenticate and receive JWT + Refresh Token     |
| `POST` | `/api/auth/logout`    | Blacklist the current JWT token                  |
| `POST` | `/api/auth/refresh`   | Exchange a refresh token for a new access token  |
| `GET`  | `/verify-email`       | Confirm email address via verification link      |
| `GET`  | `/oauth2/authorization/google` | Initiates Google OAuth2 flow            |
| `GET`  | `/oauth2/authorization/github` | Initiates GitHub OAuth2 flow            |

### Successful Sign-In Response (`UserInfoResponse`)

```json
{
  "username": "superadmin2",
  "email": "admin@example.com",
  "jwtToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

The `jwtToken` must be included in all subsequent protected requests as:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 6. Protecting Endpoints with `@PreAuthorize`

Method-level security is applied using the `@PreAuthorize` annotation. The expression is evaluated **after** the JWT filter has populated the `SecurityContext` but **before** the controller method body executes.

### Authority-Based Pattern (Primary Pattern)

```java
@PreAuthorize("hasAuthority('PATIENT_MANAGEMENT:VIEW')")
@GetMapping("/all-patients")
public ResponseEntity<PatientPageResponseDTO> getAllPatients(...) { ... }
```

The authority string `PATIENT_MANAGEMENT:VIEW` is matched against the `GrantedAuthority` objects loaded by `CustomUserDetailsService`.

### Role-Based Pattern

```java
@PreAuthorize("hasRole('ADMIN')")     // matches ROLE_ADMIN
@PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
```

### Combined / Ownership Check

```java
@PreAuthorize("hasAuthority('PATIENT_MANAGEMENT:VIEW') or #username == authentication.name")
```

Allows access if the user has the VIEW permission **or** is accessing their own record.

### Custom Service Pattern

```java
@PreAuthorize("@permissionService.hasPermission(authentication, 'PATIENT_MANAGEMENT', 'VIEW')")
```

Delegates to `CustomPermissionService` for dynamic database-driven checks.

### Full `PatientController` Permission Map

| HTTP Method | Endpoint                          | Required Authority              |
|-------------|-----------------------------------|---------------------------------|
| `GET`       | `/api/v1/patients/all-patients`   | `PATIENT_MANAGEMENT:VIEW`       |
| `POST`      | `/api/v1/patients/add-patient`    | `PATIENT_MANAGEMENT:CREATE`     |
| `PUT`       | `/api/v1/patients/edit-patient`   | `PATIENT_MANAGEMENT:EDIT`       |
| `GET`       | `/api/v1/patients/get-patient/{username}` | `PATIENT_MANAGEMENT:VIEW` or own username |
| `DELETE`    | `/api/v1/patients/delete-patient/{username}` | `PATIENT_MANAGEMENT:DELETE` |

---

## 7. Request Lifecycle — Step by Step

Here is a concrete walkthrough of a `GET /api/v1/patients/all-patients` request made by `superadmin2`:

```
Step 1: Client sends request
  GET /api/v1/patients/all-patients
  Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdXBlcmFkbWluMiJ9...

Step 2: CORS filter
  → Origin: http://localhost:3000
  → Matches allowed origin pattern "http://localhost:*" ✅

Step 3: AuthTokenFilter.doFilterInternal()
  → parseJwt(): extracts "eyJhbGciOiJIUzI1NiJ9..."
  → jwtUtils.validateJwtToken(): verifies HMAC signature + expiry ✅
  → jwtUtils.getUserNameFromJwtToken(): extracts sub → "superadmin2"
  → userDetailsService.loadUserByUsername("superadmin2")
       → Cache miss? → DB query via findByUsernameWithAllPermissions()
       → Builds authorities: [ROLE_ADMIN, PATIENT_MANAGEMENT:CREATE,
                               PATIENT_MANAGEMENT:VIEW, PATIENT_MANAGEMENT:EDIT,
                               PATIENT_MANAGEMENT:DELETE, PATIENT_MANAGEMENT:LIST,
                               USER_MANAGEMENT:CREATE, USER_MANAGEMENT:VIEW, ...]
  → Creates UsernamePasswordAuthenticationToken(userDetails, null, authorities)
  → SecurityContextHolder.getContext().setAuthentication(token)

Step 4: Spring Security authorization
  → Path "/api/v1/patients/all-patients" is NOT in public list
  → Authentication present in SecurityContext? ✅
  → .authenticated() check passes

Step 5: Method Security @PreAuthorize
  → @PreAuthorize("hasAuthority('PATIENT_MANAGEMENT:VIEW')")
  → authorities contains "PATIENT_MANAGEMENT:VIEW" ✅

Step 6: PatientController.getAllPatients() executes
  → Returns 200 OK with patient list

────────────────────────────────────────────────────────────────────
FAILURE SCENARIO: User "janesmith" (ROLE_PATIENT) tries DELETE
────────────────────────────────────────────────────────────────────

Step 3: AuthTokenFilter loads janesmith's authorities:
  → [ROLE_PATIENT, PATIENT_MANAGEMENT:VIEW, PATIENT_MANAGEMENT:EDIT]

Step 5: Method Security @PreAuthorize
  → @PreAuthorize("hasAuthority('PATIENT_MANAGEMENT:DELETE')")
  → authorities does NOT contain "PATIENT_MANAGEMENT:DELETE" ❌
  → AccessDeniedException thrown → HTTP 403 Forbidden
```

---

## 8. OAuth2 / Social Login Flow

```
Step 1: Client navigates to /oauth2/authorization/google
  → Spring Security redirects to Google's OAuth2 consent page

Step 2: User authenticates with Google and consents

Step 3: Google redirects back to /login/oauth2/code/google
  → Spring Security's OAuth2 client processes the authorization code
  → Exchanges code for access token + user profile

Step 4: Oauth2SuccessHandler.onAuthenticationSuccess() is called
  → Extracts registrationId = "google"
  → Extracts OAuth2User (contains name, email, sub, etc.)
  → Calls OAuth2UserProcessingService.handleOauth2loginRequest(user, "google")
       → Oauth2utils.getOauthProvider("google") → AuthProviderType.GOOGLE
       → Oauth2utils.determineProviderIdFromOauth2user() → Google's "sub" field
       → Oauth2utils.determineUsernameFromOauth2user() → user's email
       → Looks up or creates User entity with providerType=GOOGLE, providerId=<sub>
       → Generates JWT via JwtUtils.generateTokenFromUsername(email)
       → Returns UserInfoResponse with JWT

Step 5: Oauth2SuccessHandler writes JSON response to client
  → Same UserInfoResponse format as /api/auth/signin

Step 6: Client uses the JWT for all subsequent API calls
  → Identical flow to traditional login from this point
```

---

## 9. Token Blacklist & Logout

```
POST /api/auth/logout
Body: { "token": "eyJhbGciOiJIUzI1NiJ9..." }

Step 1: AuthController.logoutUser() receives request
Step 2: Calls authService.logoutUser(token)
Step 3: authService calls jwtUtils.getExpirationFromToken(token)
          → extracts exp claim → converts to LocalDateTime
Step 4: tokenBlacklistService.blacklistToken(token, expirationTime)
          → blacklistedTokens.put(token, expirationTime)
Step 5: Returns 200 OK "Logout successful!"

Any subsequent request with the same token:
  → AuthTokenFilter validates JWT ✅ (signature still valid)
  → tokenBlacklistService.isTokenBlacklisted(token) → true ❌
  → Authentication NOT set in SecurityContext
  → AuthEntryPointJwt returns 401
  (⚠️ This step requires uncommenting the blacklist check in AuthTokenFilter)
```

---

## 10. Caching Strategy

The `"userPermissions"` cache is used in two places:

| Location                       | Cache Key                                              | Eviction Needed When                   |
|--------------------------------|--------------------------------------------------------|----------------------------------------|
| `CustomUserDetailsService`     | `{username}`                                           | User roles/permissions change in DB    |
| `CustomPermissionService`      | `{username}_{moduleKey}_{permission}`                  | User roles/permissions change in DB    |

Cache configuration is defined in `CacheConfig.java`. Both caches share the same `"userPermissions"` cache region.

**Cache Invalidation Note:** Currently there is no explicit `@CacheEvict` call when user roles are modified. If roles are updated via an admin operation, the affected user may need to log out and log back in (or the cache entry must be manually evicted) for the change to take effect.

---

## 11. Roles & Permissions Quick Reference

### Defined Application Roles (`AppRoles` enum)

| Role                | Intended For                                      |
|---------------------|---------------------------------------------------|
| `ROLE_USER`         | Default role for newly registered users           |
| `ROLE_ADMIN`        | Full system access                                |
| `ROLE_SUBADMIN`     | Limited admin, created by ROLE_ADMIN              |
| `ROLE_DOCTOR`       | View/manage patients, write prescriptions         |
| `ROLE_PATIENT`      | View own profile and appointments                 |
| `ROLE_NURSE`        | Assist doctors, manage patient vitals             |
| `ROLE_RECEPTIONIST` | Manage appointments and registrations             |
| `ROLE_PHARMACIST`   | Manage medicines and prescriptions                |

### Active Seeded Roles

| Role            | Module               | Permissions Granted                          |
|-----------------|----------------------|----------------------------------------------|
| `ROLE_USER`     | —                    | None                                         |
| `ROLE_PATIENT`  | PATIENT_MANAGEMENT   | `VIEW`, `EDIT`                               |
| `ROLE_ADMIN`    | PATIENT_MANAGEMENT   | `CREATE`, `VIEW`, `EDIT`, `DELETE`, `LIST`   |
| `ROLE_ADMIN`    | USER_MANAGEMENT      | `CREATE`, `VIEW`, `EDIT`, `DELETE`, `LIST`   |

### Authority String Format

```
{MODULE_KEY}:{PERMISSION}
PATIENT_MANAGEMENT:VIEW
PATIENT_MANAGEMENT:CREATE
USER_MANAGEMENT:DELETE
```

---

## 12. Public vs Protected Routes

### Publicly Accessible (No JWT Required)

```
/api/auth/**              → signup, signin, logout, refresh, verify-email
/api/v1/auth/**           → (alias)
/api/public/**            → any explicitly public endpoints
/api/external/**          → external integrations
/v3/api-docs/**           → OpenAPI JSON spec
/api/v1/swagger-ui/**     → Swagger UI
/api/v1/swagger-ui.html   → Swagger UI entry page
/api/v1/docs/**           → API docs
/api/v1/swagger-resources/**
/webjars/**               → Static Swagger/UI assets
/health                   → Health check endpoint
/favicon.ico              → Browser icon request
/oauth2/**                → OAuth2 provider redirect initiation
/login/oauth2/**          → OAuth2 callback endpoint
/api/v1/login/oauth2/**   → (alias)
/verify-email             → Email verification callback
```

### Protected (JWT Required)

```
Everything else, including:
/api/v1/patients/**   → All patient CRUD operations
/api/v1/users/**      → User management
... any custom endpoints you add
```

---

## 13. Security Design Principles

The authorization system is built around these key principles:

### Least Privilege
Users receive only the permissions they explicitly need. `ROLE_PATIENT` only gets `VIEW` and `EDIT` on their own module — never `DELETE` or administrative operations.

### Stateless by Design
No session data is stored server-side. Every request is self-contained with its JWT. This enables horizontal scaling without sticky sessions.

### Fail-Safe Defaults
The default rule is `anyRequest().authenticated()`. New endpoints are protected by default — a developer must explicitly add paths to the whitelist to make them public.

### Defense in Depth
Three overlapping layers of authorization:
1. **URL-level** — `SecurityConfig` blocks unauthenticated requests before they reach controllers.
2. **Method-level** — `@PreAuthorize` checks specific module permissions on each controller method.
3. **Data-level** — `CustomPermissionService` can enforce real-time DB checks for dynamic permission rules.

### Secure Credential Storage
Passwords are never stored in plaintext. BCrypt with automatic salting is used for all password hashing.

### Token Expiry
All JWTs carry an `exp` claim. Even if a token is stolen, it has a limited useful lifetime. Combined with the token blacklist on logout, the exposure window is bounded.

### Separation of Concerns
- `JwtUtils` — only deals with JWT encoding/decoding.
- `AuthTokenFilter` — only handles per-request JWT processing.
- `CustomUserDetailsService` — only loads user + authorities.
- `CustomPermissionService` — only evaluates dynamic permissions.
- `SecurityConfig` — only declares the overall policy.

Each class has a single, well-defined responsibility.
