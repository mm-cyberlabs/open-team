package com.openteam.util;

import com.openteam.repository.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility class for handling database schema migrations.
 */
public class DatabaseMigrationUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationUtil.class);
    private static final DatabaseConnection dbConnection = DatabaseConnection.getInstance();
    
    /**
     * Ensures all required columns exist in all tables.
     * This is a safe operation that adds columns only if they don't exist.
     */
    public static void ensureColumnsExist() {
        logger.info("Checking and adding required columns if needed...");
        
        try {
            // Archive columns
            addArchiveColumnIfNotExists("announcements");
            addArchiveColumnIfNotExists("activities");
            addArchiveColumnIfNotExists("deployments");
            
            // Deployment specific columns
            addDeploymentColumnsIfNotExists();
            
            logger.info("Columns migration completed successfully");
        } catch (Exception e) {
            logger.error("Error during columns migration", e);
        }
    }
    
    /**
     * Ensures the is_archived column exists in all tables.
     * This is a safe operation that adds the column only if it doesn't exist.
     */
    public static void ensureArchiveColumnsExist() {
        logger.info("Checking and adding archive columns if needed...");
        
        try {
            addArchiveColumnIfNotExists("announcements");
            addArchiveColumnIfNotExists("activities");
            addArchiveColumnIfNotExists("deployments");
            
            logger.info("Archive columns migration completed successfully");
        } catch (Exception e) {
            logger.error("Error during archive columns migration", e);
        }
    }
    
    /**
     * Adds ticket_number and documentation_url columns to deployments table if they don't exist.
     */
    private static void addDeploymentColumnsIfNotExists() {
        try (Connection conn = dbConnection.getConnection()) {
            // Add ticket_number column
            if (!columnExists(conn, "deployments", "ticket_number")) {
                logger.info("Adding ticket_number column to deployments table");
                
                String sql = "ALTER TABLE team_comm.deployments " + 
                           "ADD COLUMN ticket_number VARCHAR(50)";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate();
                    logger.info("Successfully added ticket_number column to deployments");
                }
            } else {
                logger.debug("Column ticket_number already exists in deployments table");
            }
            
            // Add documentation_url column
            if (!columnExists(conn, "deployments", "documentation_url")) {
                logger.info("Adding documentation_url column to deployments table");
                
                String sql = "ALTER TABLE team_comm.deployments " + 
                           "ADD COLUMN documentation_url VARCHAR(500)";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate();
                    logger.info("Successfully added documentation_url column to deployments");
                }
            } else {
                logger.debug("Column documentation_url already exists in deployments table");
            }
            
        } catch (SQLException e) {
            logger.error("Error adding deployment columns", e);
        }
    }
    
    /**
     * Adds is_archived column to a table if it doesn't exist.
     */
    private static void addArchiveColumnIfNotExists(String tableName) {
        try (Connection conn = dbConnection.getConnection()) {
            // Check if column exists
            if (!columnExists(conn, tableName, "is_archived")) {
                logger.info("Adding is_archived column to table: {}", tableName);
                
                String sql = "ALTER TABLE team_comm." + tableName + 
                           " ADD COLUMN is_archived BOOLEAN DEFAULT false";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate();
                    logger.info("Successfully added is_archived column to {}", tableName);
                }
                
                // Create index for better performance
                String indexSql = "CREATE INDEX IF NOT EXISTS idx_" + tableName + "_archived " +
                                "ON team_comm." + tableName + "(is_archived)";
                
                try (PreparedStatement stmt = conn.prepareStatement(indexSql)) {
                    stmt.executeUpdate();
                    logger.info("Created index for is_archived column on {}", tableName);
                }
                
            } else {
                logger.debug("Column is_archived already exists in table: {}", tableName);
            }
            
        } catch (SQLException e) {
            logger.error("Error adding archive column to table: " + tableName, e);
        }
    }
    
    /**
     * Checks if a column exists in a table.
     */
    private static boolean columnExists(Connection conn, String tableName, String columnName) {
        String sql = """
            SELECT 1 FROM information_schema.columns 
            WHERE table_schema = 'team_comm' 
            AND table_name = ? 
            AND column_name = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking if column exists: {} in table: {}", columnName, tableName, e);
            return false;
        }
    }
    
    /**
     * Migrates existing is_active values to is_archived (where is_archived = NOT is_active).
     * This handles tables that were created with is_active instead of is_archived.
     */
    public static void migrateActiveToArchived() {
        logger.info("Migrating is_active values to is_archived...");
        
        String[] tables = {"announcements", "activities"};
        
        for (String tableName : tables) {
            try (Connection conn = dbConnection.getConnection()) {
                
                // Check if both columns exist
                if (columnExists(conn, tableName, "is_active") && 
                    columnExists(conn, tableName, "is_archived")) {
                    
                    String sql = "UPDATE team_comm." + tableName + 
                               " SET is_archived = NOT is_active " +
                               " WHERE is_archived IS NULL OR is_archived = false";
                    
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        int updated = stmt.executeUpdate();
                        if (updated > 0) {
                            logger.info("Updated {} records in table: {}", updated, tableName);
                        }
                    }
                }
                
            } catch (SQLException e) {
                logger.error("Error migrating active to archived for table: " + tableName, e);
            }
        }
    }
}