package com.openteam.repository;

import com.openteam.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Workspace entities in the database.
 * Handles all CRUD operations for workspaces.
 */
public class WorkspaceRepository {
    private static final Logger logger = LoggerFactory.getLogger(WorkspaceRepository.class);
    
    private final DatabaseConnection dbConnection;
    
    public WorkspaceRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Finds all active workspaces ordered by name.
     * 
     * @return List of active workspaces
     */
    public List<Workspace> findAllActive() {
        String sql = """
            SELECT id, name, description, created_at, updated_at, is_active
            FROM team_comm.workspaces
            WHERE is_active = true
            ORDER BY name ASC
            """;
        
        List<Workspace> workspaces = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                workspaces.add(mapResultSetToWorkspace(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error finding all active workspaces", e);
        }
        
        return workspaces;
    }
    
    /**
     * Finds a workspace by ID.
     * 
     * @param id Workspace ID
     * @return Optional containing the workspace if found
     */
    public Optional<Workspace> findById(Long id) {
        String sql = """
            SELECT id, name, description, created_at, updated_at, is_active
            FROM team_comm.workspaces
            WHERE id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToWorkspace(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding workspace by ID: " + id, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Finds a workspace by name.
     * 
     * @param name Workspace name
     * @return Optional containing the workspace if found
     */
    public Optional<Workspace> findByName(String name) {
        String sql = """
            SELECT id, name, description, created_at, updated_at, is_active
            FROM team_comm.workspaces
            WHERE name = ? AND is_active = true
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToWorkspace(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding workspace by name: " + name, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Saves a workspace to the database.
     * 
     * @param workspace Workspace to save
     * @return Saved workspace with generated ID
     */
    public Workspace save(Workspace workspace) {
        if (workspace.getId() == null) {
            return insert(workspace);
        } else {
            return update(workspace);
        }
    }
    
    /**
     * Soft deletes a workspace by setting is_active to false.
     * 
     * @param id Workspace ID to delete
     */
    public void deleteById(Long id) {
        String sql = "UPDATE team_comm.workspaces SET is_active = false WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Workspace deleted (soft delete) with ID: {}", id);
            }
            
        } catch (SQLException e) {
            logger.error("Error deleting workspace with ID: " + id, e);
        }
    }
    
    private Workspace insert(Workspace workspace) {
        String sql = """
            INSERT INTO team_comm.workspaces (name, description)
            VALUES (?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, workspace.getName());
            stmt.setString(2, workspace.getDescription());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        workspace.setId(generatedKeys.getLong(1));
                        logger.info("Workspace created with ID: {}", workspace.getId());
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error inserting workspace", e);
        }
        
        return workspace;
    }
    
    private Workspace update(Workspace workspace) {
        String sql = """
            UPDATE team_comm.workspaces 
            SET name = ?, description = ?
            WHERE id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, workspace.getName());
            stmt.setString(2, workspace.getDescription());
            stmt.setLong(3, workspace.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Workspace updated with ID: {}", workspace.getId());
            }
            
        } catch (SQLException e) {
            logger.error("Error updating workspace", e);
        }
        
        return workspace;
    }
    
    private Workspace mapResultSetToWorkspace(ResultSet rs) throws SQLException {
        Workspace workspace = new Workspace();
        workspace.setId(rs.getLong("id"));
        workspace.setName(rs.getString("name"));
        workspace.setDescription(rs.getString("description"));
        workspace.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        workspace.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        workspace.setIsActive(rs.getBoolean("is_active"));
        
        return workspace;
    }
}