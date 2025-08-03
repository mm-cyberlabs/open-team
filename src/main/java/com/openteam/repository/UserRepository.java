package com.openteam.repository;

import com.openteam.model.User;
import com.openteam.model.UserRole;
import com.openteam.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing User entities in the database.
 * Handles all CRUD operations for users including authentication.
 */
public class UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    
    private final DatabaseConnection dbConnection;
    
    public UserRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Finds all active users.
     * 
     * @return List of active users
     */
    public List<User> findAllActive() {
        String sql = """
            SELECT u.id, u.username, u.full_name, u.email, u.password_hash, u.role, 
                   u.workspace_id, u.created_at, u.updated_at, u.is_active, u.last_login,
                   w.id as workspace_id, w.name as workspace_name, w.description as workspace_description
            FROM team_comm.users u
            LEFT JOIN team_comm.workspaces w ON u.workspace_id = w.id
            WHERE u.is_active = true
            ORDER BY u.username ASC
            """;
        
        List<User> users = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error finding all active users", e);
        }
        
        return users;
    }
    
    /**
     * Finds a user by username for authentication.
     * 
     * @param username Username to search for
     * @return Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        String sql = """
            SELECT u.id, u.username, u.full_name, u.email, u.password_hash, u.role, 
                   u.workspace_id, u.created_at, u.updated_at, u.is_active, u.last_login,
                   w.id as workspace_id, w.name as workspace_name, w.description as workspace_description
            FROM team_comm.users u
            LEFT JOIN team_comm.workspaces w ON u.workspace_id = w.id
            WHERE u.username = ? AND u.is_active = true
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding user by username: " + username, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Finds a user by ID.
     * 
     * @param id User ID
     * @return Optional containing the user if found
     */
    public Optional<User> findById(Long id) {
        String sql = """
            SELECT u.id, u.username, u.full_name, u.email, u.password_hash, u.role, 
                   u.workspace_id, u.created_at, u.updated_at, u.is_active, u.last_login,
                   w.id as workspace_id, w.name as workspace_name, w.description as workspace_description
            FROM team_comm.users u
            LEFT JOIN team_comm.workspaces w ON u.workspace_id = w.id
            WHERE u.id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding user by ID: " + id, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Finds all users in a specific workspace.
     * 
     * @param workspaceId Workspace ID
     * @return List of users in the workspace
     */
    public List<User> findByWorkspaceId(Long workspaceId) {
        String sql = """
            SELECT u.id, u.username, u.full_name, u.email, u.password_hash, u.role, 
                   u.workspace_id, u.created_at, u.updated_at, u.is_active, u.last_login,
                   w.id as workspace_id, w.name as workspace_name, w.description as workspace_description
            FROM team_comm.users u
            LEFT JOIN team_comm.workspaces w ON u.workspace_id = w.id
            WHERE u.workspace_id = ? AND u.is_active = true
            ORDER BY u.username ASC
            """;
        
        List<User> users = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, workspaceId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding users by workspace ID: " + workspaceId, e);
        }
        
        return users;
    }
    
    /**
     * Finds all super administrators.
     * 
     * @return List of super admin users
     */
    public List<User> findSuperAdmins() {
        String sql = """
            SELECT u.id, u.username, u.full_name, u.email, u.password_hash, u.role, 
                   u.workspace_id, u.created_at, u.updated_at, u.is_active, u.last_login,
                   w.id as workspace_id, w.name as workspace_name, w.description as workspace_description
            FROM team_comm.users u
            LEFT JOIN team_comm.workspaces w ON u.workspace_id = w.id
            WHERE u.role = 'SUPER_ADMIN' AND u.is_active = true
            ORDER BY u.username ASC
            """;
        
        List<User> users = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error finding super admin users", e);
        }
        
        return users;
    }
    
    /**
     * Updates the last login timestamp for a user.
     * 
     * @param userId User ID
     */
    public void updateLastLogin(Long userId) {
        String sql = "UPDATE team_comm.users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.executeUpdate();
            
            logger.debug("Updated last login for user ID: {}", userId);
            
        } catch (SQLException e) {
            logger.error("Error updating last login for user ID: " + userId, e);
        }
    }
    
    /**
     * Saves a user to the database.
     * 
     * @param user User to save
     * @return Saved user with generated ID
     */
    public User save(User user) {
        if (user.getId() == null) {
            return insert(user);
        } else {
            return update(user);
        }
    }
    
    /**
     * Soft deletes a user by setting is_active to false.
     * 
     * @param id User ID to delete
     */
    public void deleteById(Long id) {
        String sql = "UPDATE team_comm.users SET is_active = false WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("User deleted (soft delete) with ID: {}", id);
            }
            
        } catch (SQLException e) {
            logger.error("Error deleting user with ID: " + id, e);
        }
    }
    
    private User insert(User user) {
        String sql = """
            INSERT INTO team_comm.users (username, full_name, email, password_hash, role, workspace_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPasswordHash());
            stmt.setString(5, user.getRole().name());
            
            if (user.getWorkspace() != null) {
                stmt.setLong(6, user.getWorkspace().getId());
            } else {
                stmt.setNull(6, Types.BIGINT);
            }
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getLong(1));
                        logger.info("User created with ID: {}", user.getId());
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error inserting user", e);
        }
        
        return user;
    }
    
    private User update(User user) {
        String sql = """
            UPDATE team_comm.users 
            SET username = ?, full_name = ?, email = ?, password_hash = ?, role = ?, workspace_id = ?
            WHERE id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPasswordHash());
            stmt.setString(5, user.getRole().name());
            
            if (user.getWorkspace() != null) {
                stmt.setLong(6, user.getWorkspace().getId());
            } else {
                stmt.setNull(6, Types.BIGINT);
            }
            
            stmt.setLong(7, user.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("User updated with ID: {}", user.getId());
            }
            
        } catch (SQLException e) {
            logger.error("Error updating user", e);
        }
        
        return user;
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        user.setIsActive(rs.getBoolean("is_active"));
        
        // Handle last login (can be null)
        Timestamp lastLoginTimestamp = rs.getTimestamp("last_login");
        if (lastLoginTimestamp != null) {
            user.setLastLogin(lastLoginTimestamp.toLocalDateTime());
        }
        
        // Map workspace if present
        Long workspaceId = rs.getLong("workspace_id");
        if (!rs.wasNull()) {
            Workspace workspace = new Workspace();
            workspace.setId(workspaceId);
            workspace.setName(rs.getString("workspace_name"));
            workspace.setDescription(rs.getString("workspace_description"));
            user.setWorkspace(workspace);
        }
        
        return user;
    }
}