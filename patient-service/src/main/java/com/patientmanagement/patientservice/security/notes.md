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

# Security Components

## JwtUtils

- Contains utility methods for generating, parsing, and validating JWTs.
- Includes:
    - Token generation
    - Token validation
    - Extracting the username from the token

## AuthTokenFilter

- Filters incoming requests to check for a valid JWT in the header.
- Extracts the JWT from the request header, validates it, and configures the Spring Security context with user details if the token is valid.

## AuthEntryPointJwt

- Provides custom handling for unauthorized requests (when authentication is required but not supplied or valid).
- When an unauthorized request is detected, it:
    - Logs the error
    - Returns a JSON response with an error message, status code, and attempted path

## SecurityConfig

- Configures Spring Security filters and rules for the application.
- Sets up the security filter chain, permitting or denying access based on paths and roles.
- Configures session management to **stateless**, which is crucial for JWT usage.
