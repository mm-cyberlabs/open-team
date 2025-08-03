package com.openteam.util;

import com.openteam.repository.DatabaseConnection;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility class to generate BCrypt password hashes and update user passwords in the database.
 * This is primarily for testing and initial setup purposes.
 */
public class PasswordHashGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PasswordHashGenerator.class);
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        
        try {
            // Generate hashes for common test passwords
            String adminPassword = "admin123";
            String userPassword = "password";
            
            String adminHash = encoder.encode(adminPassword);
            String userHash = encoder.encode(userPassword);
            
            logger.info("Generated BCrypt hash for 'admin123': {}", adminHash);
            logger.info("Generated BCrypt hash for 'password': {}", userHash);
            
            // Update the database with proper hashes
            updateUserPasswords(dbConnection, adminHash, userHash);
            
            // Verify the updates
            verifyUserPasswords(dbConnection);
            
        } catch (Exception e) {
            logger.error("Error updating passwords", e);
        }
    }
    
    private static void updateUserPasswords(DatabaseConnection dbConnection, String adminHash, String userHash) {
        String[] adminUsers = {"sys_admin", "eng_admin", "mkt_admin", "ops_admin"};
        String[] regularUsers = {"jdoe", "msmith", "rjohnson", "alee"};
        
        try (Connection conn = dbConnection.getConnection()) {
            // Update admin users
            String updateSql = "UPDATE team_comm.users SET password_hash = ? WHERE username = ?";
            
            for (String username : adminUsers) {
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setString(1, adminHash);
                    stmt.setString(2, username);
                    int updated = stmt.executeUpdate();
                    logger.info("Updated password for admin user: {} (rows affected: {})", username, updated);
                }
            }
            
            // Update regular users  
            for (String username : regularUsers) {
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setString(1, userHash);
                    stmt.setString(2, username);
                    int updated = stmt.executeUpdate();
                    logger.info("Updated password for regular user: {} (rows affected: {})", username, updated);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error updating user passwords", e);
            throw new RuntimeException("Failed to update passwords", e);
        }
    }
    
    private static void verifyUserPasswords(DatabaseConnection dbConnection) {
        String selectSql = "SELECT username, full_name, role, password_hash FROM team_comm.users ORDER BY role DESC, username";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql);
             ResultSet rs = stmt.executeQuery()) {
            
            logger.info("=== Current Users in Database ===");
            while (rs.next()) {
                String username = rs.getString("username");
                String fullName = rs.getString("full_name");
                String role = rs.getString("role");
                String passwordHash = rs.getString("password_hash");
                
                boolean isValidBCrypt = passwordHash != null && passwordHash.startsWith("$2a$");
                
                logger.info("User: {} ({}) - Role: {} - Valid BCrypt: {}", 
                           username, fullName, role, isValidBCrypt);
            }
            
        } catch (SQLException e) {
            logger.error("Error verifying user passwords", e);
        }
    }
}