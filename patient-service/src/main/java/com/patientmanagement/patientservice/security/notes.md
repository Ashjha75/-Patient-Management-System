# Key Security Principles

- **Least Privilege**
- **Secure by Design**
- **Fail-safe Defaults**
- **Secure Communication**
- **Input Validation**
- **Auditing and Logging**
- **Regular Updates and Patch Management**

---

## Bcrypt

Bcrypt is a password hashing function designed for secure password storage.  
It automatically handles salt generation and is slow by design to resist brute-force attacks.  
Use it to hash passwords before storing them in your database; **never store plain text passwords**.
---

# JWT – Main Points

- **Compact & Self-contained** → JWTs carry authentication and authorization data in a single token.
- **Structure** → Consists of three parts:
    1. **Header** (algorithm & token type)
    2. **Payload** (claims like user info, roles, expiration)
    3. **Signature** (verifies integrity and authenticity)
- **Stateless** → No need to store session on the server; all info is in the token.
- **Signed** → Ensures data integrity using secret key (HMAC) or public/private keys (RSA/ECDSA).
- **Expiration** → Includes `exp` claim to limit token lifetime for security.
- **Transport** → Commonly sent in the HTTP `Authorization` header as `Bearer <token>`.
- **Use Cases** → Authentication, authorization, and secure information exchange.

---

# `Files 📂`

# Security Components

## JwtUtils

- Contains utility methods for generating, parsing, and validating JWTs.
- Includes:
    - Token generation
    - Token validation
    - Extracting the username from the token

# JwtUtils – Main Responsibilities

- **Extract JWT from Header**
- **Generate Secure JWT Cookie**
- **Clear JWT Cookie (Logout)**
- **Generate JWT from Username**
- **Extract JWT from Cookies**
- **Create Cryptographic Signing Key**
- **Extract Username from JWT**
- **Validate JWT with Error Handling**

---

## AuthTokenFilter

- Filters incoming requests to check for a valid JWT in the header.
- Extracts the JWT from the request header, validates it, and configures the Spring Security context with user details
  if the token is valid.

# AuthTokenFilter – Main Responsibilities

- **Intercept HTTP Requests (OncePerRequestFilter)**
- **Extract JWT (Authorization header → fallback to cookies)**
- **Validate JWT using JwtUtils**
- **Extract Username from JWT**
- **Load UserDetails from Database**
- **Create Authentication Token (UsernamePasswordAuthenticationToken)**
- **Attach Request Details to Authentication**
- **Set Authentication in SecurityContext**
- **Handle Invalid or Missing Token**
- **Continue with Filter Chain**

---

## AuthEntryPointJwt

- Provides custom handling for unauthorized requests (when authentication is required but not supplied or valid).
- When an unauthorized request is detected, it:
    - Logs the error
    - Returns a JSON response with an error message, status code, and attempted path

# AuthEntryPointJwt – Main Responsibilities

- **Intercept Unauthorized Access Attempts**
- **Log Unauthorized Attempts (URI, IP, Error)**
- **Set HTTP 401 Unauthorized Response**
- **Build JSON Error Response (status, error, message, path, timestamp, correlationId)**
- **Extract Client IP Address (X-Forwarded-For, X-Real-IP, fallback remoteAddr)**
- **Generate or Reuse Correlation ID (X-Correlation-ID or new ID)**
- **Write Standardized JSON Error Response to Client**

---

## SecurityConfig

- Configures Spring Security filters and rules for the application.
- Sets up the security filter chain, permitting or denying access based on paths and roles.
- Configures session management to **stateless**, which is crucial for JWT usage.

# WebSecurityConfig – Main Responsibilities

- **Define JWT Authentication Filter Bean**
- **Configure DaoAuthenticationProvider**
- **Provide PasswordEncoder (BCrypt)**
- **Expose AuthenticationManager Bean**
- **Configure CORS Policy**
- **Main SecurityFilterChain Setup**
    - CSRF Disabled
    - CORS Enabled
    - Stateless Session Management
    - Exception Handling with AuthEntryPointJwt
    - URL-based Authorization Rules
    - Security Headers (HSTS, CSP, Frame Options)
    - Register Custom Authentication Provider
    - Insert AuthTokenFilter Before UsernamePasswordAuthenticationFilter
- **Define WebSecurityCustomizer (Ignore Swagger/Static Paths)**

login
logout - blacklisting
signup
refresh-token
rbac