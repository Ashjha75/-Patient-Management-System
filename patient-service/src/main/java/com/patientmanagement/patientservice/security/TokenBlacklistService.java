package com.patientmanagement.patientservice.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for managing JWT token blacklist to handle logout and token invalidation.
 * Maintains a thread-safe cache of blacklisted tokens with automatic cleanup.
 *
 * @author Patient Management System
 * @version 1.0
 */
@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    // Map to store blacklisted tokens with their expiration times
    private final Map<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();

    @Value("${app.jwt.expiration-hours:24}")
    private int tokenExpirationHours;

    /**
     * Adds a token to the blacklist with its expiration time.
     *
     * @param token The JWT token to blacklist
     * @param expirationTime The time when the token expires
     */
    public void blacklistToken(String token, LocalDateTime expirationTime) {
        if (token == null || token.trim().isEmpty()) {
            logger.warn("Attempted to blacklist null or empty token");
            return;
        }

        blacklistedTokens.put(token, expirationTime);

        // Log for security monitoring (avoid logging sensitive data)
        String tokenPrefix = token.length() > 20 ? token.substring(0, 20) + "..." : token + "...";
        logger.info("Token blacklisted successfully. Token prefix: {}, Expiration: {}",
                   tokenPrefix, expirationTime);
    }

    /**
     * Overloaded method that uses default expiration time.
     *
     * @param token The JWT token to blacklist
     */
    public void blacklistToken(String token) {
        LocalDateTime expirationTime = LocalDateTime.now().plusHours(tokenExpirationHours);
        blacklistToken(token, expirationTime);
    }
    
    /**
     * Checks if a token is blacklisted.
     *
     * @param token The token to check
     * @return true if the token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.debug("Null or empty token provided for blacklist check");
            return false;
        }

        boolean isBlacklisted = blacklistedTokens.containsKey(token);

        if (isBlacklisted) {
            logger.debug("Token found in blacklist");
        }

        return isBlacklisted;
    }
    
    /**
     * Removes expired tokens from the blacklist to prevent memory leaks.
     * This method is scheduled to run every hour.
     */
    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 ms)
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        AtomicInteger removedCount = new AtomicInteger(0);

        // Remove expired tokens
        blacklistedTokens.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(now)) {
                removedCount.incrementAndGet();
                return true;
            }
            return false;
        });

        if (removedCount.get() > 0) {
            logger.info("Cleaned up {} expired tokens from blacklist. Current blacklist size: {}",
                       removedCount.get(), blacklistedTokens.size());
        }

        // Log warning if blacklist is getting too large
        if (blacklistedTokens.size() > 10000) {
            logger.warn("Token blacklist is large ({}). Consider reviewing token expiration strategy.",
                       blacklistedTokens.size());
        }
    }

    /**
     * Manually clears all tokens from the blacklist.
     * Use with caution - only for administrative purposes.
     */
    public void clearAllTokens() {
        int clearedCount = blacklistedTokens.size();
        blacklistedTokens.clear();
        logger.warn("Manually cleared all {} tokens from blacklist", clearedCount);
    }

    /**
     * Gets the current size of the blacklist.
     *
     * @return The number of tokens currently blacklisted
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }

    /**
     * Checks if a specific token exists and when it expires.
     *
     * @param token The token to check
     * @return The expiration time if token exists, null otherwise
     */
    public LocalDateTime getTokenExpirationTime(String token) {
        return blacklistedTokens.get(token);
    }
}
