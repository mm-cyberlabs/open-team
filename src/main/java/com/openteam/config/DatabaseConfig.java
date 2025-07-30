package com.openteam.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Database configuration class for Open Team application.
 * Represents database connection settings loaded from YAML configuration.
 */
public class DatabaseConfig {
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("password")
    private String password;
    
    @JsonProperty("driver")
    private String driver;
    
    public DatabaseConfig() {
        // Default constructor for Jackson
    }
    
    public DatabaseConfig(String url, String username, String password, String driver) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driver = driver;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getDriver() {
        return driver;
    }
    
    public void setDriver(String driver) {
        this.driver = driver;
    }
    
    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", driver='" + driver + '\'' +
                '}';
    }
}