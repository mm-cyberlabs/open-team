package com.openteam.repository;

import com.openteam.model.Activity;
import com.openteam.model.ActivityType;
import com.openteam.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Activity entities in the database.
 * Handles all CRUD operations for team activities.
 */
public class ActivityRepository {
    private static final Logger logger = LoggerFactory.getLogger(ActivityRepository.class);
    
    private final DatabaseConnection dbConnection;
    
    public ActivityRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Finds all active activities ordered by scheduled date.
     * 
     * @return List of active activities
     */
    public List<Activity> findAllActive() {
        String sql = """
            SELECT a.id, a.title, a.description, a.activity_type, a.scheduled_date, 
                   a.location, a.created_at, a.updated_at, a.is_active,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.activities a
            LEFT JOIN team_comm.users cu ON a.created_by = cu.id
            LEFT JOIN team_comm.users uu ON a.updated_by = uu.id
            WHERE a.is_active = true
            ORDER BY a.scheduled_date ASC
            """;
        
        List<Activity> activities = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                activities.add(mapResultSetToActivity(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error finding all active activities", e);
        }
        
        return activities;
    }
    
    /**
     * Finds an activity by ID.
     * 
     * @param id Activity ID
     * @return Optional containing the activity if found
     */
    public Optional<Activity> findById(Long id) {
        String sql = """
            SELECT a.id, a.title, a.description, a.activity_type, a.scheduled_date, 
                   a.location, a.created_at, a.updated_at, a.is_active,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.activities a
            LEFT JOIN team_comm.users cu ON a.created_by = cu.id
            LEFT JOIN team_comm.users uu ON a.updated_by = uu.id
            WHERE a.id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToActivity(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding activity by ID: " + id, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Saves an activity to the database.
     * 
     * @param activity Activity to save
     * @return Saved activity with generated ID
     */
    public Activity save(Activity activity) {
        if (activity.getId() == null) {
            return insert(activity);
        } else {
            return update(activity);
        }
    }
    
    /**
     * Finds activities by type.
     * 
     * @param activityType Activity type to filter by
     * @return List of activities with specified type
     */
    public List<Activity> findByType(ActivityType activityType) {
        String sql = """
            SELECT a.id, a.title, a.description, a.activity_type, a.scheduled_date, 
                   a.location, a.created_at, a.updated_at, a.is_active,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.activities a
            LEFT JOIN team_comm.users cu ON a.created_by = cu.id
            LEFT JOIN team_comm.users uu ON a.updated_by = uu.id
            WHERE a.activity_type = ? AND a.is_active = true
            ORDER BY a.scheduled_date ASC
            """;
        
        List<Activity> activities = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, activityType.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(mapResultSetToActivity(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding activities by type: " + activityType, e);
        }
        
        return activities;
    }
    
    /**
     * Soft deletes an activity by setting is_active to false.
     * 
     * @param id Activity ID to delete
     */
    public void deleteById(Long id) {
        String sql = "UPDATE team_comm.activities SET is_active = false WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Activity deleted (soft delete) with ID: {}", id);
            }
            
        } catch (SQLException e) {
            logger.error("Error deleting activity with ID: " + id, e);
        }
    }
    
    private Activity insert(Activity activity) {
        String sql = """
            INSERT INTO team_comm.activities (title, description, activity_type, scheduled_date, 
                                            location, created_by, updated_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, activity.getTitle());
            stmt.setString(2, activity.getDescription());
            stmt.setString(3, activity.getActivityType().name());
            stmt.setTimestamp(4, activity.getScheduledDate() != null ? 
                Timestamp.valueOf(activity.getScheduledDate()) : null);
            stmt.setString(5, activity.getLocation());
            stmt.setLong(6, activity.getCreatedBy().getId());
            stmt.setLong(7, activity.getUpdatedBy().getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        activity.setId(generatedKeys.getLong(1));
                        logger.info("Activity created with ID: {}", activity.getId());
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error inserting activity", e);
        }
        
        return activity;
    }
    
    private Activity update(Activity activity) {
        String sql = """
            UPDATE team_comm.activities 
            SET title = ?, description = ?, activity_type = ?, scheduled_date = ?, 
                location = ?, updated_by = ?
            WHERE id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, activity.getTitle());
            stmt.setString(2, activity.getDescription());
            stmt.setString(3, activity.getActivityType().name());
            stmt.setTimestamp(4, activity.getScheduledDate() != null ? 
                Timestamp.valueOf(activity.getScheduledDate()) : null);
            stmt.setString(5, activity.getLocation());
            stmt.setLong(6, activity.getUpdatedBy().getId());
            stmt.setLong(7, activity.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Activity updated with ID: {}", activity.getId());
            }
            
        } catch (SQLException e) {
            logger.error("Error updating activity", e);
        }
        
        return activity;
    }
    
    private Activity mapResultSetToActivity(ResultSet rs) throws SQLException {
        Activity activity = new Activity();
        activity.setId(rs.getLong("id"));
        activity.setTitle(rs.getString("title"));
        activity.setDescription(rs.getString("description"));
        activity.setActivityType(ActivityType.valueOf(rs.getString("activity_type")));
        
        Timestamp scheduledDate = rs.getTimestamp("scheduled_date");
        if (scheduledDate != null) {
            activity.setScheduledDate(scheduledDate.toLocalDateTime());
        }
        
        activity.setLocation(rs.getString("location"));
        activity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        activity.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        activity.setIsActive(rs.getBoolean("is_active"));
        
        // Map created by user
        User createdBy = new User();
        createdBy.setId(rs.getLong("created_user_id"));
        createdBy.setUsername(rs.getString("created_username"));
        createdBy.setFullName(rs.getString("created_full_name"));
        createdBy.setEmail(rs.getString("created_email"));
        activity.setCreatedBy(createdBy);
        
        // Map updated by user
        User updatedBy = new User();
        updatedBy.setId(rs.getLong("updated_user_id"));
        updatedBy.setUsername(rs.getString("updated_username"));
        updatedBy.setFullName(rs.getString("updated_full_name"));
        updatedBy.setEmail(rs.getString("updated_email"));
        activity.setUpdatedBy(updatedBy);
        
        return activity;
    }
}