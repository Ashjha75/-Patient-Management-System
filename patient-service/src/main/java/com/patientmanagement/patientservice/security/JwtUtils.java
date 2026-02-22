package com.patientmanagement.patientservice.security;

import com.patientmanagement.patientservice.exception.ApiException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Utility class for handling JSON Web Tokens (JWT) in the application.
 * <p>
 * Main responsibilities:
 * <ul>
 *   <li>Extract JWT from HTTP headers</li>
 *   <li>Generate secure JWT cookies</li>
 *   <li>Clear JWT cookies for logout</li>
 *   <li>Generate JWT from username</li>
 *   <li>Extract JWT from cookies</li>
 *   <li>Create cryptographic signing key</li>
 *   <li>Extract username from JWT</li>
 *   <li>Validate JWT with error handling</li>
 * </ul>
 * <p>
 * Also includes methods for:
 * <ul>
 *   <li>Token generation</li>
 *   <li>Token validation</li>
 *   <li>Parsing and extracting claims from JWTs</li>
 * </ul>
 */

@Component
public class JwtUtils {

	private static final Logger logger = LoggerFactory.getLogger(
		JwtUtils.class
	);

	@Value("${spring.app.jwtSecret}")
	private String jwtSecret;

	@Value("${spring.app.expirationTimeMS}")
	private String expirationTimeMS;

	@Value("${spring.app.cookieName}")
	private String cookieName;

	//    1. Extract JWT from HTTP headers

	public String getJwtFromHeader(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		logger.debug("Processing JWT token from Authorization header");

		if (
			StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")
		) {
			String token = authHeader.substring(7).trim();
			return StringUtils.hasText(token) ? token : null;
		}
		return null;
	}

	//    2. Generate JWT from username
	public String generateTokenFromUsername(String username) {
		if (!StringUtils.hasText(username)) {
			throw new IllegalArgumentException("Username cannot be empty");
		}

		Date issuedDate = new Date();
		Date expirationDate = new Date(
			issuedDate.getTime() + Long.parseLong(expirationTimeMS)
		);

		return Jwts.builder()
			.subject(username.trim())
			.claim("roles", "Admin")
			.issuedAt(issuedDate)
			.expiration(expirationDate)
			.signWith(secureKey())
			.compact();
	}

	//    3. Extract username from JWT
	public String getUserNameFromJwtToken(String token) {
		if (!StringUtils.hasText(token)) {
			throw new IllegalArgumentException("Token cannot be empty");
		}

		try {
			Claims claims = Jwts.parser()
				.verifyWith((SecretKey) secureKey())
				.build()
				.parseSignedClaims(token.trim())
				.getPayload();

			return claims.getSubject();
		} catch (Exception e) {
			throw new ApiException("Invalid JWT token");
		}
	}

	//    4. Validate JWT with error handling

	public boolean validateJwtToken(String token) {
		if (!StringUtils.hasText(token)) {
			throw new IllegalArgumentException("Token cannot be empty");
		}

		try {
			Jwts.parser()
				.verifyWith((SecretKey) secureKey())
				.build()
				.parseSignedClaims(token.trim());
			logger.debug("JWT token validation successful");
			return true;
		} catch (SecurityException ex) {
			logger.error("Invalid JWT signature: {}", ex.getMessage());
		} catch (MalformedJwtException ex) {
			logger.error("Invalid JWT format: {}", ex.getMessage());
		} catch (ExpiredJwtException ex) {
			logger.error("JWT token expired: {}", ex.getMessage());
		} catch (UnsupportedJwtException ex) {
			logger.error("Unsupported JWT token: {}", ex.getMessage());
		} catch (IllegalArgumentException ex) {
			logger.error("JWT claims string is empty: {}", ex.getMessage());
		} catch (Exception ex) {
			logger.error(
				"Unexpected JWT validation error: {}",
				ex.getMessage()
			);
		}

		return false;
	}

	// Add this method to your existing JwtUtils class

	/**
	 * Extracts the expiration time from a JWT token.
	 *
	 * @param token The JWT token
	 * @return LocalDateTime representing when the token expires
	 */
	public LocalDateTime getExpirationFromToken(String token) {
		if (!StringUtils.hasText(token)) {
			throw new IllegalArgumentException("Token cannot be empty");
		}

		try {
			Claims claims = Jwts.parser()
				.verifyWith((SecretKey) secureKey())
				.build()
				.parseSignedClaims(token.trim())
				.getPayload();

			Date expiration = claims.getExpiration();

			// Convert Date to LocalDateTime
			return expiration
				.toInstant()
				.atZone(java.time.ZoneId.systemDefault())
				.toLocalDateTime();
		} catch (Exception e) {
			logger.error(
				"Failed to extract expiration from JWT: {}",
				e.getMessage()
			);
			// Return current time + default expiration as fallback
			return LocalDateTime.now().plusHours(24);
		}
	}

	/*HELPER METHOD __*/
	private Key secureKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}

	public Key getSecretKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}
}
