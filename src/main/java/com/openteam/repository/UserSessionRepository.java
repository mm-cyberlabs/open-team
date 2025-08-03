package com.openteam.repository;

import com.openteam.model.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing UserSession entities in the database.
 * Handles session tracking for authentication.
 */
public class UserSessionRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserSessionRepository.class);
    
    private final DatabaseConnection dbConnection;
    
    public UserSessionRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Finds a session by token.
     * 
     * @param sessionToken Session token to search for
     * @return Optional containing the session if found and valid
     */
    public Optional<UserSession> findByToken(String sessionToken) {
        String sql = """
            SELECT id, user_id, session_token, expires_at, created_at, is_active
            FROM team_comm.user_sessions
            WHERE session_token = ? AND is_active = true
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sessionToken);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUserSession(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding session by token", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Finds all active sessions for a user.
     * 
     * @param userId User ID
     * @return List of active sessions for the user
     */
    public List<UserSession> findActiveSessionsByUserId(Long userId) {
        String sql = """
            SELECT id, user_id, session_token, expires_at, created_at, is_active
            FROM team_comm.user_sessions
            WHERE user_id = ? AND is_active = true AND expires_at > CURRENT_TIMESTAMP
            ORDER BY created_at DESC
            """;
        
        List<UserSession> sessions = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToUserSession(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding active sessions for user ID: " + userId, e);
        }
        
        return sessions;
    }
    
    /**
     * Creates a new session.
     * 
     * @param session Session to create
     * @return Created session with generated ID
     */
    public UserSession save(UserSession session) {
        String sql = """
            INSERT INTO team_comm.user_sessions (user_id, session_token, expires_at)
            VALUES (?, ?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, session.getUserId());
            stmt.setString(2, session.getSessionToken());
            stmt.setTimestamp(3, Timestamp.valueOf(session.getExpiresAt()));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        session.setId(generatedKeys.getLong(1));
                        logger.debug("Session created with ID: {}", session.getId());
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error creating session", e);
        }
        
        return session;
    }
    
    /**
     * Invalidates a session by setting is_active to false.
     * 
     * @param sessionToken Session token to invalidate
     */
    public void invalidateSession(String sessionToken) {
        String sql = "UPDATE team_comm.user_sessions SET is_active = false WHERE session_token = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sessionToken);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.debug("Session invalidated: {}", sessionToken);
            }
            
        } catch (SQLException e) {
            logger.error("Error invalidating session", e);
        }
    }
    
    /**
     * Invalidates all sessions for a user.
     * 
     * @param userId User ID
     */
    public void invalidateAllUserSessions(Long userId) {
        String sql = "UPDATE team_comm.user_sessions SET is_active = false WHERE user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            int rowsAffected = stmt.executeUpdate();
            
            logger.debug("Invalidated {} sessions for user ID: {}", rowsAffected, userId);
            
        } catch (SQLException e) {
            logger.error("Error invalidating all sessions for user ID: " + userId, e);
        }
    }
    
    /**
     * Cleans up expired sessions by setting is_active to false.
     * 
     * @return Number of sessions cleaned up
     */
    public int cleanupExpiredSessions() {
        String sql = """
            UPDATE team_comm.user_sessions 
            SET is_active = false 
            WHERE expires_at < CURRENT_TIMESTAMP AND is_active = true
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Cleaned up {} expired sessions", rowsAffected);
            }
            
            return rowsAffected;
            
        } catch (SQLException e) {
            logger.error("Error cleaning up expired sessions", e);
            return 0;
        }
    }
    
    private UserSession mapResultSetToUserSession(ResultSet rs) throws SQLException {
        UserSession session = new UserSession();
        session.setId(rs.getLong("id"));
        session.setUserId(rs.getLong("user_id"));
        session.setSessionToken(rs.getString("session_token"));
        session.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
        session.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        session.setIsActive(rs.getBoolean("is_active"));
        
        return session;
    }
}