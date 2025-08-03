package com.openteam.service;

import com.openteam.model.User;
import com.openteam.model.UserSession;
import com.openteam.repository.UserRepository;
import com.openteam.repository.UserSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for handling user authentication, session management, and keychain integration.
 * Provides secure login, logout, and session validation functionality.
 */
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    private static final int SESSION_DURATION_HOURS = 8;
    private static final int TOKEN_LENGTH = 32;
    
    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final KeychainService keychainService;
    private final SecureRandom secureRandom;
    
    public AuthenticationService() {
        this.userRepository = new UserRepository();
        this.sessionRepository = new UserSessionRepository();
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.keychainService = new KeychainService();
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Authenticates a user with username and password.
     * 
     * @param username User's username
     * @param password User's plain text password
     * @return AuthenticationResult containing success status and user/session info
     */
    public AuthenticationResult authenticate(String username, String password) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                logger.warn("Authentication failed - user not found: {}", username);
                return AuthenticationResult.failure("Invalid username or password");
            }
            
            User user = userOpt.get();
            
            if (!user.getIsActive()) {
                logger.warn("Authentication failed - user inactive: {}", username);
                return AuthenticationResult.failure("Account is inactive");
            }
            
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                logger.warn("Authentication failed - invalid password for user: {}", username);
                return AuthenticationResult.failure("Invalid username or password");
            }
            
            // Create new session
            String sessionToken = generateSessionToken();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_DURATION_HOURS);
            
            UserSession session = new UserSession(user.getId(), sessionToken, expiresAt);
            session = sessionRepository.save(session);
            
            // Update last login
            userRepository.updateLastLogin(user.getId());
            
            // Store credentials in keychain for auto-fill
            keychainService.storeUserCredentials(user.getUsername(), user.getFullName());
            
            logger.info("User authenticated successfully: {}", username);
            return AuthenticationResult.success(user, sessionToken);
            
        } catch (Exception e) {
            logger.error("Error during authentication for user: " + username, e);
            return AuthenticationResult.failure("Authentication error occurred");
        }
    }
    
    /**
     * Validates a session token and returns the associated user.
     * 
     * @param sessionToken Session token to validate
     * @return Optional containing the user if session is valid
     */
    public Optional<User> validateSession(String sessionToken) {
        try {
            Optional<UserSession> sessionOpt = sessionRepository.findByToken(sessionToken);
            
            if (sessionOpt.isEmpty()) {
                return Optional.empty();
            }
            
            UserSession session = sessionOpt.get();
            
            if (!session.getIsActive() || session.isExpired()) {
                // Clean up expired session
                sessionRepository.invalidateSession(sessionToken);
                return Optional.empty();
            }
            
            // Get user details
            return userRepository.findById(session.getUserId());
            
        } catch (Exception e) {
            logger.error("Error validating session", e);
            return Optional.empty();
        }
    }
    
    /**
     * Logs out a user by invalidating their session.
     * 
     * @param sessionToken Session token to invalidate
     */
    public void logout(String sessionToken) {
        try {
            sessionRepository.invalidateSession(sessionToken);
            logger.debug("User logged out - session invalidated");
        } catch (Exception e) {
            logger.error("Error during logout", e);
        }
    }
    
    /**
     * Logs out all sessions for a specific user.
     * 
     * @param userId User ID
     */
    public void logoutAllSessions(Long userId) {
        try {
            sessionRepository.invalidateAllUserSessions(userId);
            logger.info("All sessions invalidated for user ID: {}", userId);
        } catch (Exception e) {
            logger.error("Error invalidating all sessions for user ID: " + userId, e);
        }
    }
    
    /**
     * Hashes a plain text password for storage.
     * 
     * @param plainPassword Plain text password
     * @return Hashed password
     */
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
    
    /**
     * Changes a user's password.
     * 
     * @param userId User ID
     * @param currentPassword Current password for verification
     * @param newPassword New password
     * @return true if password was changed successfully
     */
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                logger.warn("Password change failed - current password incorrect for user ID: {}", userId);
                return false;
            }
            
            // Hash and save new password
            user.setPasswordHash(hashPassword(newPassword));
            userRepository.save(user);
            
            // Invalidate all existing sessions to force re-login
            logoutAllSessions(userId);
            
            logger.info("Password changed successfully for user ID: {}", userId);
            return true;
            
        } catch (Exception e) {
            logger.error("Error changing password for user ID: " + userId, e);
            return false;
        }
    }
    
    /**
     * Retrieves stored user credentials from keychain for auto-fill.
     * 
     * @return KeychainCredentials containing username and full name
     */
    public KeychainCredentials getStoredCredentials() {
        return keychainService.getStoredCredentials();
    }
    
    /**
     * Cleans up expired sessions from the database.
     * Should be called periodically.
     * 
     * @return Number of sessions cleaned up
     */
    public int cleanupExpiredSessions() {
        try {
            return sessionRepository.cleanupExpiredSessions();
        } catch (Exception e) {
            logger.error("Error cleaning up expired sessions", e);
            return 0;
        }
    }
    
    /**
     * Generates a secure random session token.
     * 
     * @return Base64 encoded session token
     */
    private String generateSessionToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    /**
     * Result of an authentication attempt.
     */
    public static class AuthenticationResult {
        private final boolean success;
        private final String message;
        private final User user;
        private final String sessionToken;
        
        private AuthenticationResult(boolean success, String message, User user, String sessionToken) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.sessionToken = sessionToken;
        }
        
        public static AuthenticationResult success(User user, String sessionToken) {
            return new AuthenticationResult(true, "Authentication successful", user, sessionToken);
        }
        
        public static AuthenticationResult failure(String message) {
            return new AuthenticationResult(false, message, null, null);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public User getUser() {
            return user;
        }
        
        public String getSessionToken() {
            return sessionToken;
        }
    }
    
    /**
     * Credentials stored in keychain.
     */
    public static class KeychainCredentials {
        private final String username;
        private final String fullName;
        
        public KeychainCredentials(String username, String fullName) {
            this.username = username;
            this.fullName = fullName;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getFullName() {
            return fullName;
        }
        
        public boolean isEmpty() {
            return username == null || username.trim().isEmpty();
        }
    }
}