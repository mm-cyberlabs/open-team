package com.openteam.repository;

import com.openteam.config.AppConfig;
import com.openteam.config.DatabaseConfig;
import com.openteam.config.YamlConfigLoader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection manager using HikariCP connection pooling.
 * Provides centralized database connection management for the application.
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    
    private static DatabaseConnection instance;
    private HikariDataSource dataSource;
    
    private DatabaseConnection() {
        initialize();
    }
    
    /**
     * Gets the singleton instance of DatabaseConnection.
     * 
     * @return DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Initializes the database connection pool.
     */
    private void initialize() {
        try {
            AppConfig config = YamlConfigLoader.loadConfig();
            DatabaseConfig dbConfig = config.getDatabase();
            
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(dbConfig.getUrl());
            hikariConfig.setUsername(dbConfig.getUsername());
            hikariConfig.setPassword(dbConfig.getPassword());
            hikariConfig.setDriverClassName(dbConfig.getDriver());
            
            // Connection pool settings
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setConnectionTimeout(30000);
            hikariConfig.setIdleTimeout(300000);
            hikariConfig.setMaxLifetime(900000);
            hikariConfig.setLeakDetectionThreshold(60000);
            
            // Connection test query
            hikariConfig.setConnectionTestQuery("SELECT 1");
            
            this.dataSource = new HikariDataSource(hikariConfig);
            logger.info("Database connection pool initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize database connection", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Gets a connection from the connection pool.
     * 
     * @return Database connection
     * @throws SQLException if connection cannot be obtained
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database connection not initialized");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Gets the DataSource for direct access.
     * 
     * @return HikariDataSource instance
     */
    public DataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * Tests the database connection.
     * 
     * @return true if connection is successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
    
    /**
     * Closes the database connection pool.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }
}