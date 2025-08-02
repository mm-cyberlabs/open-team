package com.openteam.repository;

import com.openteam.model.DeploymentComment;
import com.openteam.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing DeploymentComment entities in the database.
 * Handles all CRUD operations for deployment comments.
 */
public class DeploymentCommentRepository {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentCommentRepository.class);
    
    private final DatabaseConnection dbConnection;
    
    public DeploymentCommentRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Finds all comments for a specific deployment ordered by creation date (newest first).
     * 
     * @param deploymentId Deployment ID
     * @return List of comments for the deployment
     */
    public List<DeploymentComment> findByDeploymentId(Long deploymentId) {
        String sql = """
            SELECT dc.id, dc.deployment_id, dc.comment_text, dc.created_at,
                   u.id as user_id, u.username, u.full_name, u.email
            FROM team_comm.deployment_comments dc
            LEFT JOIN team_comm.users u ON dc.created_by = u.id
            WHERE dc.deployment_id = ?
            ORDER BY dc.created_at DESC
            """;
        
        List<DeploymentComment> comments = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, deploymentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding comments for deployment ID: " + deploymentId, e);
        }
        
        return comments;
    }
    
    /**
     * Finds a comment by ID.
     * 
     * @param id Comment ID
     * @return Optional containing the comment if found
     */
    public Optional<DeploymentComment> findById(Long id) {
        String sql = """
            SELECT dc.id, dc.deployment_id, dc.comment_text, dc.created_at,
                   u.id as user_id, u.username, u.full_name, u.email
            FROM team_comm.deployment_comments dc
            LEFT JOIN team_comm.users u ON dc.created_by = u.id
            WHERE dc.id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToComment(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding comment by ID: " + id, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Saves a comment to the database.
     * 
     * @param comment Comment to save
     * @return Saved comment with generated ID
     */
    public DeploymentComment save(DeploymentComment comment) {
        String sql = """
            INSERT INTO team_comm.deployment_comments (deployment_id, comment_text, created_by)
            VALUES (?, ?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, comment.getDeploymentId());
            stmt.setString(2, comment.getCommentText());
            stmt.setLong(3, comment.getCreatedBy().getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        comment.setId(generatedKeys.getLong(1));
                        logger.info("Comment created with ID: {}", comment.getId());
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error saving comment", e);
        }
        
        return comment;
    }
    
    /**
     * Deletes a comment by ID.
     * 
     * @param id Comment ID to delete
     */
    public void deleteById(Long id) {
        String sql = "DELETE FROM team_comm.deployment_comments WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Comment deleted with ID: {}", id);
            }
            
        } catch (SQLException e) {
            logger.error("Error deleting comment with ID: " + id, e);
        }
    }
    
    /**
     * Counts the number of comments for a specific deployment.
     * 
     * @param deploymentId Deployment ID
     * @return Number of comments
     */
    public int countByDeploymentId(Long deploymentId) {
        String sql = "SELECT COUNT(*) FROM team_comm.deployment_comments WHERE deployment_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, deploymentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error counting comments for deployment ID: " + deploymentId, e);
        }
        
        return 0;
    }
    
    /**
     * Maps a database result set row to a DeploymentComment object.
     */
    private DeploymentComment mapResultSetToComment(ResultSet rs) throws SQLException {
        DeploymentComment comment = new DeploymentComment();
        comment.setId(rs.getLong("id"));
        comment.setDeploymentId(rs.getLong("deployment_id"));
        comment.setCommentText(rs.getString("comment_text"));
        comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        // Map created by user
        User createdBy = new User();
        createdBy.setId(rs.getLong("user_id"));
        createdBy.setUsername(rs.getString("username"));
        createdBy.setFullName(rs.getString("full_name"));
        createdBy.setEmail(rs.getString("email"));
        comment.setCreatedBy(createdBy);
        
        return comment;
    }
}