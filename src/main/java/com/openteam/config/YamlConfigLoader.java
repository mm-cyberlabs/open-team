package com.openteam.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * YAML configuration loader for Open Team application.
 * Loads configuration from ~/.openteam/config.yml
 */
public class YamlConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(YamlConfigLoader.class);
    
    private static final String CONFIG_DIR = ".openteam";
    private static final String CONFIG_FILE = "config.yml";
    
    private static final ObjectMapper yamlMapper;
    
    static {
        yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Loads the complete application configuration from YAML file.
     * 
     * @return AppConfig object with all configuration settings
     * @throws ConfigurationException if configuration cannot be loaded
     */
    public static AppConfig loadConfig() throws ConfigurationException {
        Path configPath = getConfigPath();
        
        if (!Files.exists(configPath)) {
            createDefaultConfig(configPath);
        }
        
        try {
            AppConfig config = yamlMapper.readValue(configPath.toFile(), AppConfig.class);
            logger.info("Configuration loaded successfully from: {}", configPath);
            return config;
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load configuration from: " + configPath, e);
        }
    }
    
    /**
     * Gets the configuration file path based on the operating system.
     * 
     * @return Path to the configuration file
     */
    private static Path getConfigPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, CONFIG_DIR, CONFIG_FILE);
    }
    
    /**
     * Creates a default configuration file if none exists.
     * 
     * @param configPath Path where the configuration file should be created
     */
    private static void createDefaultConfig(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
            
            String defaultConfig = """
                database:
                  url: jdbc:postgresql://localhost:5432/openteam
                  username: openteam_user
                  password: your_secure_password
                  driver: org.postgresql.Driver
                
                application:
                  refreshInterval: 10
                """;
            
            Files.writeString(configPath, defaultConfig);
            logger.info("Default configuration file created at: {}", configPath);
            logger.warn("Please update the configuration file with your database credentials");
            
        } catch (IOException e) {
            logger.error("Failed to create default configuration file", e);
        }
    }
    
    /**
     * Custom exception for configuration-related errors.
     */
    public static class ConfigurationException extends Exception {
        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}