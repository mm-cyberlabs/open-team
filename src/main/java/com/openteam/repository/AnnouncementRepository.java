package com.openteam.repository;

import com.openteam.model.Announcement;
import com.openteam.model.Priority;
import com.openteam.model.User;
import com.openteam.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Announcement entities in the database.
 * Handles all CRUD operations for announcements with audit logging.
 */
public class AnnouncementRepository {
    private static final Logger logger = LoggerFactory.getLogger(AnnouncementRepository.class);
    
    private final DatabaseConnection dbConnection;
    
    public AnnouncementRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Finds all active announcements ordered by creation date.
     * 
     * @return List of active announcements
     */
    public List<Announcement> findAllActive() {
        String sql = """
            SELECT a.id, a.title, a.content, a.priority, a.created_at, a.updated_at, a.is_active, 
                   a.is_archived, a.expiration_date,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.announcements a
            LEFT JOIN team_comm.users cu ON a.created_by = cu.id
            LEFT JOIN team_comm.users uu ON a.updated_by = uu.id
            WHERE a.is_active = true
            ORDER BY a.created_at DESC
            """;
        
        List<Announcement> announcements = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                announcements.add(mapResultSetToAnnouncement(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error finding all active announcements", e);
        }
        
        return announcements;
    }
    
    /**
     * Finds an announcement by ID.
     * 
     * @param id Announcement ID
     * @return Optional containing the announcement if found
     */
    public Optional<Announcement> findById(Long id) {
        String sql = """
            SELECT a.id, a.title, a.content, a.priority, a.created_at, a.updated_at, a.is_active,
                   a.is_archived, a.expiration_date,
                   w.id as workspace_id, w.name as workspace_name, w.description as workspace_description,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.announcements a
            LEFT JOIN team_comm.workspaces w ON a.workspace_id = w.id
            LEFT JOIN team_comm.users cu ON a.created_by = cu.id
            LEFT JOIN team_comm.users uu ON a.updated_by = uu.id
            WHERE a.id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAnnouncement(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding announcement by ID: " + id, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Saves an announcement to the database.
     * 
     * @param announcement Announcement to save
     * @return Saved announcement with generated ID
     */
    public Announcement save(Announcement announcement) {
        if (announcement.getId() == null) {
            return insert(announcement);
        } else {
            return update(announcement);
        }
    }
    
    /**
     * Finds announcements by priority.
     * 
     * @param priority Priority to filter by
     * @return List of announcements with specified priority
     */
    public List<Announcement> findByPriority(Priority priority) {
        String sql = """
            SELECT a.id, a.title, a.content, a.priority, a.created_at, a.updated_at, a.is_active,
                   a.is_archived, a.expiration_date,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.announcements a
            LEFT JOIN team_comm.users cu ON a.created_by = cu.id
            LEFT JOIN team_comm.users uu ON a.updated_by = uu.id
            WHERE a.priority = ? AND a.is_active = true
            ORDER BY a.created_at DESC
            """;
        
        List<Announcement> announcements = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, priority.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    announcements.add(mapResultSetToAnnouncement(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding announcements by priority: " + priority, e);
        }
        
        return announcements;
    }
    
    /**
     * Soft deletes an announcement by setting is_active to false.
     * 
     * @param id Announcement ID to delete
     */
    public void deleteById(Long id) {
        String sql = "UPDATE team_comm.announcements SET is_active = false WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Announcement deleted (soft delete) with ID: {}", id);
            }
            
        } catch (SQLException e) {
            logger.error("Error deleting announcement with ID: " + id, e);
        }
    }
    
    private Announcement insert(Announcement announcement) {
        String sql = """
            INSERT INTO team_comm.announcements (workspace_id, title, content, priority, created_by, updated_by, expiration_date)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, announcement.getWorkspace().getId());
            stmt.setString(2, announcement.getTitle());
            stmt.setString(3, announcement.getContent());
            stmt.setString(4, announcement.getPriority().name());
            stmt.setLong(5, announcement.getCreatedBy().getId());
            stmt.setLong(6, announcement.getUpdatedBy().getId());
            
            // Handle expiration date (can be null)
            if (announcement.getExpirationDate() != null) {
                stmt.setTimestamp(7, Timestamp.valueOf(announcement.getExpirationDate()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        announcement.setId(generatedKeys.getLong(1));
                        logger.info("Announcement created with ID: {}", announcement.getId());
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error inserting announcement", e);
        }
        
        return announcement;
    }
    
    private Announcement update(Announcement announcement) {
        String sql = """
            UPDATE team_comm.announcements 
            SET workspace_id = ?, title = ?, content = ?, priority = ?, updated_by = ?, expiration_date = ?
            WHERE id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, announcement.getWorkspace().getId());
            stmt.setString(2, announcement.getTitle());
            stmt.setString(3, announcement.getContent());
            stmt.setString(4, announcement.getPriority().name());
            stmt.setLong(5, announcement.getUpdatedBy().getId());
            
            // Handle expiration date (can be null)
            if (announcement.getExpirationDate() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(announcement.getExpirationDate()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }
            
            stmt.setLong(7, announcement.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Announcement updated with ID: {}", announcement.getId());
            }
            
        } catch (SQLException e) {
            logger.error("Error updating announcement", e);
        }
        
        return announcement;
    }
    
    /**
     * Archives all expired announcements by setting is_archived = true.
     * @return Number of announcements archived
     */
    public int archiveExpiredAnnouncements() {
        String sql = """
            UPDATE team_comm.announcements 
            SET is_archived = true, updated_at = CURRENT_TIMESTAMP
            WHERE expiration_date IS NOT NULL 
              AND expiration_date < CURRENT_TIMESTAMP 
              AND is_archived = false 
              AND is_active = true
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Archived {} expired announcements", rowsAffected);
            }
            
            return rowsAffected;
            
        } catch (SQLException e) {
            logger.error("Error archiving expired announcements", e);
            return 0;
        }
    }
    
    private Announcement mapResultSetToAnnouncement(ResultSet rs) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setId(rs.getLong("id"));
        announcement.setTitle(rs.getString("title"));
        announcement.setContent(rs.getString("content"));
        announcement.setPriority(Priority.valueOf(rs.getString("priority")));
        announcement.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        announcement.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        announcement.setIsActive(rs.getBoolean("is_active"));
        announcement.setIsArchived(rs.getBoolean("is_archived"));
        
        // Handle expiration date (can be null)
        Timestamp expirationTimestamp = rs.getTimestamp("expiration_date");
        if (expirationTimestamp != null) {
            announcement.setExpirationDate(expirationTimestamp.toLocalDateTime());
        }
        
        // Map workspace (if available in result set)
        try {
            Long workspaceId = rs.getLong("workspace_id");
            if (!rs.wasNull()) {
                Workspace workspace = new Workspace();
                workspace.setId(workspaceId);
                workspace.setName(rs.getString("workspace_name"));
                workspace.setDescription(rs.getString("workspace_description"));
                announcement.setWorkspace(workspace);
            }
        } catch (SQLException e) {
            // workspace columns not in this query - this is okay for some queries
        }
        
        // Map created by user
        User createdBy = new User();
        createdBy.setId(rs.getLong("created_user_id"));
        createdBy.setUsername(rs.getString("created_username"));
        createdBy.setFullName(rs.getString("created_full_name"));
        createdBy.setEmail(rs.getString("created_email"));
        announcement.setCreatedBy(createdBy);
        
        // Map updated by user
        User updatedBy = new User();
        updatedBy.setId(rs.getLong("updated_user_id"));
        updatedBy.setUsername(rs.getString("updated_username"));
        updatedBy.setFullName(rs.getString("updated_full_name"));
        updatedBy.setEmail(rs.getString("updated_email"));
        announcement.setUpdatedBy(updatedBy);
        
        return announcement;
    }
    
    /**
     * Finds announcements by workspace with optional archive filter.
     * 
     * @param workspaceId Workspace ID to filter by
     * @param activeOnly Whether to only include active (non-archived) announcements
     * @return List of announcements for the workspace
     */
    public List<Announcement> findByWorkspace(Long workspaceId, boolean activeOnly) {
        String sql = "SELECT a.*, " +
                    "cu.id as created_user_id, cu.username as created_username, cu.full_name as created_full_name, cu.email as created_email, " +
                    "uu.id as updated_user_id, uu.username as updated_username, uu.full_name as updated_full_name, uu.email as updated_email " +
                    "FROM team_comm.announcements a " +
                    "LEFT JOIN team_comm.users cu ON a.created_by = cu.id " +
                    "LEFT JOIN team_comm.users uu ON a.updated_by = uu.id " +
                    "WHERE a.workspace_id = ? " +
                    (activeOnly ? "AND (a.is_archived IS NULL OR a.is_archived = false) " : "") +
                    "ORDER BY a.created_at DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, workspaceId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Announcement> announcements = new ArrayList<>();
                while (rs.next()) {
                    announcements.add(mapResultSetToAnnouncement(rs));
                }
                return announcements;
            }
        } catch (SQLException e) {
            logger.error("Error finding announcements by workspace: {}", workspaceId, e);
            throw new RuntimeException("Database error while retrieving announcements by workspace", e);
        }
    }
    
    /**
     * Finds announcements by priority and workspace.
     * 
     * @param priority Priority to filter by
     * @param workspaceId Workspace ID to filter by
     * @return List of announcements with specified priority for the workspace
     */
    public List<Announcement> findByPriorityAndWorkspace(Priority priority, Long workspaceId) {
        String sql = "SELECT a.*, " +
                    "cu.username as created_username, cu.full_name as created_full_name, cu.email as created_email, " +
                    "uu.username as updated_username, uu.full_name as updated_full_name, uu.email as updated_email " +
                    "FROM team_comm.announcements a " +
                    "JOIN team_comm.users cu ON a.created_by = cu.id " +
                    "JOIN team_comm.users uu ON a.updated_by = uu.id " +
                    "WHERE a.priority = ? AND a.workspace_id = ? " +
                    "AND (a.is_archived IS NULL OR a.is_archived = false) " +
                    "ORDER BY a.created_at DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, priority.name());
            stmt.setLong(2, workspaceId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Announcement> announcements = new ArrayList<>();
                while (rs.next()) {
                    announcements.add(mapResultSetToAnnouncement(rs));
                }
                return announcements;
            }
        } catch (SQLException e) {
            logger.error("Error finding announcements by priority and workspace: {} {}", priority, workspaceId, e);
            throw new RuntimeException("Database error while retrieving announcements by priority and workspace", e);
        }
    }
}