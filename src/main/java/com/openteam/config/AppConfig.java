package com.openteam.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Application configuration class for Open Team application.
 * Contains application-specific settings loaded from YAML configuration.
 */
public class AppConfig {
    @JsonProperty("database")
    private DatabaseConfig database;
    
    @JsonProperty("application")
    private ApplicationSettings application;
    
    public AppConfig() {
        // Default constructor for Jackson
    }
    
    public DatabaseConfig getDatabase() {
        return database;
    }
    
    public void setDatabase(DatabaseConfig database) {
        this.database = database;
    }
    
    public ApplicationSettings getApplication() {
        return application;
    }
    
    public void setApplication(ApplicationSettings application) {
        this.application = application;
    }
    
    /**
     * Application-specific settings
     */
    public static class ApplicationSettings {
        @JsonProperty("refreshInterval")
        private int refreshInterval = 10; // Default 10 seconds
        
        public int getRefreshInterval() {
            return refreshInterval;
        }
        
        public void setRefreshInterval(int refreshInterval) {
            this.refreshInterval = refreshInterval;
        }
    }
}