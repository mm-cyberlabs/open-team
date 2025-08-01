package com.openteam.repository;

import com.openteam.model.TargetDate;
import com.openteam.model.TargetDateStatus;
import com.openteam.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing TargetDate entities in the database.
 * Handles all CRUD operations for important target dates and project milestones.
 */
public class TargetDateRepository {
    private static final Logger logger = LoggerFactory.getLogger(TargetDateRepository.class);
    
    private final DatabaseConnection dbConnection;
    
    public TargetDateRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Finds all target dates ordered by target date.
     * 
     * @return List of all target dates
     */
    public List<TargetDate> findAll() {
        String sql = """
            SELECT t.id, t.project_name, t.task_name, t.target_date, t.documentation_url,
                   t.status, t.is_archived, t.created_at, t.updated_at,
                   du.id as driver_user_id, du.username as driver_username,
                   du.full_name as driver_full_name, du.email as driver_email,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.target_dates t
            LEFT JOIN team_comm.users du ON t.driver_user_id = du.id
            LEFT JOIN team_comm.users cu ON t.created_by = cu.id
            LEFT JOIN team_comm.users uu ON t.updated_by = uu.id
            ORDER BY t.target_date ASC
            """;
        
        List<TargetDate> targetDates = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                targetDates.add(mapResultSetToTargetDate(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error finding all target dates", e);
        }
        
        return targetDates;
    }
    
    /**
     * Finds non-archived target dates ordered by target date.
     * 
     * @return List of non-archived target dates
     */
    public List<TargetDate> findNonArchived() {
        String sql = """
            SELECT t.id, t.project_name, t.task_name, t.target_date, t.documentation_url,
                   t.status, t.is_archived, t.created_at, t.updated_at,
                   du.id as driver_user_id, du.username as driver_username,
                   du.full_name as driver_full_name, du.email as driver_email,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.target_dates t
            LEFT JOIN team_comm.users du ON t.driver_user_id = du.id
            LEFT JOIN team_comm.users cu ON t.created_by = cu.id
            LEFT JOIN team_comm.users uu ON t.updated_by = uu.id
            WHERE COALESCE(t.is_archived, false) = false
            ORDER BY t.target_date ASC
            """;
        
        List<TargetDate> targetDates = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                targetDates.add(mapResultSetToTargetDate(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error finding non-archived target dates", e);
        }
        
        return targetDates;
    }
    
    /**
     * Finds a target date by ID.
     * 
     * @param id Target date ID
     * @return Optional containing the target date if found
     */
    public Optional<TargetDate> findById(Long id) {
        String sql = """
            SELECT t.id, t.project_name, t.task_name, t.target_date, t.documentation_url,
                   t.status, t.is_archived, t.created_at, t.updated_at,
                   du.id as driver_user_id, du.username as driver_username,
                   du.full_name as driver_full_name, du.email as driver_email,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.target_dates t
            LEFT JOIN team_comm.users du ON t.driver_user_id = du.id
            LEFT JOIN team_comm.users cu ON t.created_by = cu.id
            LEFT JOIN team_comm.users uu ON t.updated_by = uu.id
            WHERE t.id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTargetDate(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding target date by ID: " + id, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Saves a target date to the database.
     * 
     * @param targetDate Target date to save
     * @return Saved target date with generated ID
     */
    public TargetDate save(TargetDate targetDate) {
        if (targetDate.getId() == null) {
            return insert(targetDate);
        } else {
            return update(targetDate);
        }
    }
    
    /**
     * Finds target dates by status.
     * 
     * @param status Target date status to filter by
     * @return List of target dates with specified status
     */
    public List<TargetDate> findByStatus(TargetDateStatus status) {
        String sql = """
            SELECT t.id, t.project_name, t.task_name, t.target_date, t.documentation_url,
                   t.status, t.is_archived, t.created_at, t.updated_at,
                   du.id as driver_user_id, du.username as driver_username,
                   du.full_name as driver_full_name, du.email as driver_email,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.target_dates t
            LEFT JOIN team_comm.users du ON t.driver_user_id = du.id
            LEFT JOIN team_comm.users cu ON t.created_by = cu.id
            LEFT JOIN team_comm.users uu ON t.updated_by = uu.id
            WHERE t.status = ?
            ORDER BY t.target_date ASC
            """;
        
        List<TargetDate> targetDates = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    targetDates.add(mapResultSetToTargetDate(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding target dates by status: " + status, e);
        }
        
        return targetDates;
    }
    
    /**
     * Soft deletes a target date by setting is_archived to true.
     * 
     * @param id Target date ID to delete
     */
    public void deleteById(Long id) {
        String sql = "UPDATE team_comm.target_dates SET is_archived = true WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Target date deleted (soft delete) with ID: {}", id);
            }
            
        } catch (SQLException e) {
            logger.error("Error deleting target date with ID: " + id, e);
        }
    }
    
    /**
     * Unarchives a target date by setting is_archived to false.
     * 
     * @param id Target date ID to unarchive
     */
    public void unarchiveById(Long id) {
        String sql = "UPDATE team_comm.target_dates SET is_archived = false WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Target date unarchived with ID: {}", id);
            }
            
        } catch (SQLException e) {
            logger.error("Error unarchiving target date with ID: " + id, e);
            throw new RuntimeException("Unable to unarchive target date: " + e.getMessage(), e);
        }
    }
    
    private TargetDate insert(TargetDate targetDate) {
        String sql = """
            INSERT INTO team_comm.target_dates (project_name, task_name, target_date, driver_user_id,
                                              documentation_url, status, created_by, updated_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, targetDate.getProjectName());
            stmt.setString(2, targetDate.getTaskName());
            stmt.setTimestamp(3, Timestamp.valueOf(targetDate.getTargetDate()));
            stmt.setLong(4, targetDate.getDriverUser().getId());
            stmt.setString(5, targetDate.getDocumentationUrl());
            stmt.setString(6, targetDate.getStatus().name());
            stmt.setLong(7, targetDate.getCreatedBy().getId());
            stmt.setLong(8, targetDate.getUpdatedBy().getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        targetDate.setId(generatedKeys.getLong(1));
                        logger.info("Target date created with ID: {}", targetDate.getId());
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error inserting target date", e);
        }
        
        return targetDate;
    }
    
    private TargetDate update(TargetDate targetDate) {
        String sql = """
            UPDATE team_comm.target_dates 
            SET project_name = ?, task_name = ?, target_date = ?, driver_user_id = ?,
                documentation_url = ?, status = ?, is_archived = ?, updated_by = ?
            WHERE id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, targetDate.getProjectName());
            stmt.setString(2, targetDate.getTaskName());
            stmt.setTimestamp(3, Timestamp.valueOf(targetDate.getTargetDate()));
            stmt.setLong(4, targetDate.getDriverUser().getId());
            stmt.setString(5, targetDate.getDocumentationUrl());
            stmt.setString(6, targetDate.getStatus().name());
            stmt.setBoolean(7, targetDate.getIsArchived() != null ? targetDate.getIsArchived() : false);
            stmt.setLong(8, targetDate.getUpdatedBy().getId());
            stmt.setLong(9, targetDate.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Target date updated with ID: {}", targetDate.getId());
            }
            
        } catch (SQLException e) {
            logger.error("Error updating target date", e);
        }
        
        return targetDate;
    }
    
    private TargetDate mapResultSetToTargetDate(ResultSet rs) throws SQLException {
        TargetDate targetDate = new TargetDate();
        targetDate.setId(rs.getLong("id"));
        targetDate.setProjectName(rs.getString("project_name"));
        targetDate.setTaskName(rs.getString("task_name"));
        targetDate.setTargetDate(rs.getTimestamp("target_date").toLocalDateTime());
        targetDate.setDocumentationUrl(rs.getString("documentation_url"));
        targetDate.setStatus(TargetDateStatus.valueOf(rs.getString("status")));
        targetDate.setIsArchived(rs.getBoolean("is_archived"));
        targetDate.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        targetDate.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        // Map driver user
        User driverUser = new User();
        driverUser.setId(rs.getLong("driver_user_id"));
        driverUser.setUsername(rs.getString("driver_username"));
        driverUser.setFullName(rs.getString("driver_full_name"));
        driverUser.setEmail(rs.getString("driver_email"));
        targetDate.setDriverUser(driverUser);
        
        // Map created by user
        User createdBy = new User();
        createdBy.setId(rs.getLong("created_user_id"));
        createdBy.setUsername(rs.getString("created_username"));
        createdBy.setFullName(rs.getString("created_full_name"));
        createdBy.setEmail(rs.getString("created_email"));
        targetDate.setCreatedBy(createdBy);
        
        // Map updated by user
        User updatedBy = new User();
        updatedBy.setId(rs.getLong("updated_user_id"));
        updatedBy.setUsername(rs.getString("updated_username"));
        updatedBy.setFullName(rs.getString("updated_full_name"));
        updatedBy.setEmail(rs.getString("updated_email"));
        targetDate.setUpdatedBy(updatedBy);
        
        return targetDate;
    }
}