package com.openteam.repository;

import com.openteam.model.Deployment;
import com.openteam.model.DeploymentStatus;
import com.openteam.model.Environment;
import com.openteam.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Deployment entities in the database.
 * Handles all CRUD operations for software deployments.
 */
public class DeploymentRepository {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentRepository.class);
    
    private final DatabaseConnection dbConnection;
    
    public DeploymentRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Finds all deployments ordered by deployment date.
     * 
     * @return List of all deployments
     */
    public List<Deployment> findAll() {
        String sql = """
            SELECT d.id, d.release_name, d.version, d.deployment_datetime, d.release_notes,
                   d.environment, d.status, d.created_at, d.updated_at,
                   du.id as driver_user_id, du.username as driver_username,
                   du.full_name as driver_full_name, du.email as driver_email,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.deployments d
            LEFT JOIN team_comm.users du ON d.driver_user_id = du.id
            LEFT JOIN team_comm.users cu ON d.created_by = cu.id
            LEFT JOIN team_comm.users uu ON d.updated_by = uu.id
            ORDER BY d.deployment_datetime DESC
            """;
        
        List<Deployment> deployments = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                deployments.add(mapResultSetToDeployment(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error finding all deployments", e);
        }
        
        return deployments;
    }
    
    /**
     * Finds a deployment by ID.
     * 
     * @param id Deployment ID
     * @return Optional containing the deployment if found
     */
    public Optional<Deployment> findById(Long id) {
        String sql = """
            SELECT d.id, d.release_name, d.version, d.deployment_datetime, d.release_notes,
                   d.environment, d.status, d.created_at, d.updated_at,
                   du.id as driver_user_id, du.username as driver_username,
                   du.full_name as driver_full_name, du.email as driver_email,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.deployments d
            LEFT JOIN team_comm.users du ON d.driver_user_id = du.id
            LEFT JOIN team_comm.users cu ON d.created_by = cu.id
            LEFT JOIN team_comm.users uu ON d.updated_by = uu.id
            WHERE d.id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDeployment(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding deployment by ID: " + id, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Saves a deployment to the database.
     * 
     * @param deployment Deployment to save
     * @return Saved deployment with generated ID
     */
    public Deployment save(Deployment deployment) {
        if (deployment.getId() == null) {
            return insert(deployment);
        } else {
            return update(deployment);
        }
    }
    
    /**
     * Finds deployments by status.
     * 
     * @param status Deployment status to filter by
     * @return List of deployments with specified status
     */
    public List<Deployment> findByStatus(DeploymentStatus status) {
        String sql = """
            SELECT d.id, d.release_name, d.version, d.deployment_datetime, d.release_notes,
                   d.environment, d.status, d.created_at, d.updated_at,
                   du.id as driver_user_id, du.username as driver_username,
                   du.full_name as driver_full_name, du.email as driver_email,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.deployments d
            LEFT JOIN team_comm.users du ON d.driver_user_id = du.id
            LEFT JOIN team_comm.users cu ON d.created_by = cu.id
            LEFT JOIN team_comm.users uu ON d.updated_by = uu.id
            WHERE d.status = ?
            ORDER BY d.deployment_datetime DESC
            """;
        
        List<Deployment> deployments = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    deployments.add(mapResultSetToDeployment(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding deployments by status: " + status, e);
        }
        
        return deployments;
    }
    
    /**
     * Finds deployments by environment.
     * 
     * @param environment Environment to filter by
     * @return List of deployments in specified environment
     */
    public List<Deployment> findByEnvironment(Environment environment) {
        String sql = """
            SELECT d.id, d.release_name, d.version, d.deployment_datetime, d.release_notes,
                   d.environment, d.status, d.created_at, d.updated_at,
                   du.id as driver_user_id, du.username as driver_username,
                   du.full_name as driver_full_name, du.email as driver_email,
                   cu.id as created_user_id, cu.username as created_username, 
                   cu.full_name as created_full_name, cu.email as created_email,
                   uu.id as updated_user_id, uu.username as updated_username,
                   uu.full_name as updated_full_name, uu.email as updated_email
            FROM team_comm.deployments d
            LEFT JOIN team_comm.users du ON d.driver_user_id = du.id
            LEFT JOIN team_comm.users cu ON d.created_by = cu.id
            LEFT JOIN team_comm.users uu ON d.updated_by = uu.id
            WHERE d.environment = ?
            ORDER BY d.deployment_datetime DESC
            """;
        
        List<Deployment> deployments = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, environment.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    deployments.add(mapResultSetToDeployment(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding deployments by environment: " + environment, e);
        }
        
        return deployments;
    }
    
    private Deployment insert(Deployment deployment) {
        String sql = """
            INSERT INTO team_comm.deployments (release_name, version, deployment_datetime,
                                             driver_user_id, release_notes, environment, 
                                             status, created_by, updated_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, deployment.getReleaseName());
            stmt.setString(2, deployment.getVersion());
            stmt.setTimestamp(3, Timestamp.valueOf(deployment.getDeploymentDateTime()));
            stmt.setLong(4, deployment.getDriverUser().getId());
            stmt.setString(5, deployment.getReleaseNotes());
            stmt.setString(6, deployment.getEnvironment().name());
            stmt.setString(7, deployment.getStatus().name());
            stmt.setLong(8, deployment.getCreatedBy().getId());
            stmt.setLong(9, deployment.getUpdatedBy().getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        deployment.setId(generatedKeys.getLong(1));
                        logger.info("Deployment created with ID: {}", deployment.getId());
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error inserting deployment", e);
        }
        
        return deployment;
    }
    
    private Deployment update(Deployment deployment) {
        String sql = """
            UPDATE team_comm.deployments 
            SET release_name = ?, version = ?, deployment_datetime = ?, driver_user_id = ?,
                release_notes = ?, environment = ?, status = ?, updated_by = ?
            WHERE id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, deployment.getReleaseName());
            stmt.setString(2, deployment.getVersion());
            stmt.setTimestamp(3, Timestamp.valueOf(deployment.getDeploymentDateTime()));
            stmt.setLong(4, deployment.getDriverUser().getId());
            stmt.setString(5, deployment.getReleaseNotes());
            stmt.setString(6, deployment.getEnvironment().name());
            stmt.setString(7, deployment.getStatus().name());
            stmt.setLong(8, deployment.getUpdatedBy().getId());
            stmt.setLong(9, deployment.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Deployment updated with ID: {}", deployment.getId());
            }
            
        } catch (SQLException e) {
            logger.error("Error updating deployment", e);
        }
        
        return deployment;
    }
    
    private Deployment mapResultSetToDeployment(ResultSet rs) throws SQLException {
        Deployment deployment = new Deployment();
        deployment.setId(rs.getLong("id"));
        deployment.setReleaseName(rs.getString("release_name"));
        deployment.setVersion(rs.getString("version"));
        deployment.setDeploymentDateTime(rs.getTimestamp("deployment_datetime").toLocalDateTime());
        deployment.setReleaseNotes(rs.getString("release_notes"));
        deployment.setEnvironment(Environment.valueOf(rs.getString("environment")));
        deployment.setStatus(DeploymentStatus.valueOf(rs.getString("status")));
        deployment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        deployment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        // Map driver user
        User driverUser = new User();
        driverUser.setId(rs.getLong("driver_user_id"));
        driverUser.setUsername(rs.getString("driver_username"));
        driverUser.setFullName(rs.getString("driver_full_name"));
        driverUser.setEmail(rs.getString("driver_email"));
        deployment.setDriverUser(driverUser);
        
        // Map created by user
        User createdBy = new User();
        createdBy.setId(rs.getLong("created_user_id"));
        createdBy.setUsername(rs.getString("created_username"));
        createdBy.setFullName(rs.getString("created_full_name"));
        createdBy.setEmail(rs.getString("created_email"));
        deployment.setCreatedBy(createdBy);
        
        // Map updated by user
        User updatedBy = new User();
        updatedBy.setId(rs.getLong("updated_user_id"));
        updatedBy.setUsername(rs.getString("updated_username"));
        updatedBy.setFullName(rs.getString("updated_full_name"));
        updatedBy.setEmail(rs.getString("updated_email"));
        deployment.setUpdatedBy(updatedBy);
        
        return deployment;
    }
}