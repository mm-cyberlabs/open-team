package com.openteam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Service for integrating with macOS Keychain to store and retrieve user credentials.
 * Provides secure storage of username and full name for auto-fill functionality.
 */
public class KeychainService {
    private static final Logger logger = LoggerFactory.getLogger(KeychainService.class);
    
    private static final String SERVICE_NAME = "OpenTeamApp";
    private static final String USERNAME_ACCOUNT = "current_username";
    private static final String FULLNAME_ACCOUNT = "current_fullname";
    private static final int COMMAND_TIMEOUT_SECONDS = 10;
    
    /**
     * Stores user credentials in the keychain.
     * 
     * @param username Username to store
     * @param fullName Full name to store
     */
    public void storeUserCredentials(String username, String fullName) {
        try {
            // Store username
            storeKeychainItem(USERNAME_ACCOUNT, username);
            
            // Store full name
            storeKeychainItem(FULLNAME_ACCOUNT, fullName);
            
            logger.debug("User credentials stored in keychain successfully");
            
        } catch (Exception e) {
            logger.warn("Failed to store credentials in keychain: {}", e.getMessage());
        }
    }
    
    /**
     * Retrieves stored user credentials from the keychain.
     * 
     * @return KeychainCredentials containing username and full name
     */
    public AuthenticationService.KeychainCredentials getStoredCredentials() {
        try {
            String username = getKeychainItem(USERNAME_ACCOUNT);
            String fullName = getKeychainItem(FULLNAME_ACCOUNT);
            
            return new AuthenticationService.KeychainCredentials(username, fullName);
            
        } catch (Exception e) {
            logger.debug("No credentials found in keychain or error occurred: {}", e.getMessage());
            return new AuthenticationService.KeychainCredentials(null, null);
        }
    }
    
    /**
     * Clears stored credentials from the keychain.
     */
    public void clearStoredCredentials() {
        try {
            deleteKeychainItem(USERNAME_ACCOUNT);
            deleteKeychainItem(FULLNAME_ACCOUNT);
            
            logger.debug("Credentials cleared from keychain");
            
        } catch (Exception e) {
            logger.warn("Failed to clear credentials from keychain: {}", e.getMessage());
        }
    }
    
    /**
     * Stores an item in the macOS keychain.
     * 
     * @param account Account name for the keychain item
     * @param value Value to store
     * @throws IOException If the keychain operation fails
     */
    private void storeKeychainItem(String account, String value) throws IOException {
        if (!isMacOS()) {
            logger.debug("Keychain storage skipped - not running on macOS");
            return;
        }
        
        // First try to delete existing item (ignore errors)
        try {
            deleteKeychainItem(account);
        } catch (Exception ignored) {
            // Ignore errors when deleting (item might not exist)
        }
        
        // Add new item
        String[] command = {
            "security", "add-generic-password",
            "-s", SERVICE_NAME,
            "-a", account,
            "-w", value,
            "-U"  // Update existing item if it exists
        };
        
        Process process = new ProcessBuilder(command)
            .redirectErrorStream(true)
            .start();
        
        try {
            boolean finished = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IOException("Keychain command timed out");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String output = readProcessOutput(process);
                throw new IOException("Keychain command failed with exit code " + exitCode + ": " + output);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Keychain command interrupted", e);
        }
    }
    
    /**
     * Retrieves an item from the macOS keychain.
     * 
     * @param account Account name for the keychain item
     * @return The stored value, or null if not found
     * @throws IOException If the keychain operation fails
     */
    private String getKeychainItem(String account) throws IOException {
        if (!isMacOS()) {
            logger.debug("Keychain retrieval skipped - not running on macOS");
            return null;
        }
        
        String[] command = {
            "security", "find-generic-password",
            "-s", SERVICE_NAME,
            "-a", account,
            "-w"  // Output only the password
        };
        
        Process process = new ProcessBuilder(command)
            .redirectErrorStream(true)
            .start();
        
        try {
            boolean finished = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IOException("Keychain command timed out");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                // Item not found or other error
                return null;
            }
            
            String output = readProcessOutput(process);
            return output.trim();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Keychain command interrupted", e);
        }
    }
    
    /**
     * Deletes an item from the macOS keychain.
     * 
     * @param account Account name for the keychain item
     * @throws IOException If the keychain operation fails
     */
    private void deleteKeychainItem(String account) throws IOException {
        if (!isMacOS()) {
            logger.debug("Keychain deletion skipped - not running on macOS");
            return;
        }
        
        String[] command = {
            "security", "delete-generic-password",
            "-s", SERVICE_NAME,
            "-a", account
        };
        
        Process process = new ProcessBuilder(command)
            .redirectErrorStream(true)
            .start();
        
        try {
            boolean finished = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IOException("Keychain command timed out");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String output = readProcessOutput(process);
                // Only throw if it's not a "item not found" error
                if (!output.contains("could not be found")) {
                    throw new IOException("Keychain delete command failed with exit code " + exitCode + ": " + output);
                }
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Keychain command interrupted", e);
        }
    }
    
    /**
     * Reads all output from a process.
     * 
     * @param process Process to read from
     * @return Combined stdout and stderr output
     * @throws IOException If reading fails
     */
    private String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        return output.toString();
    }
    
    /**
     * Checks if the application is running on macOS.
     * 
     * @return true if running on macOS
     */
    private boolean isMacOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("mac") || osName.contains("darwin");
    }
}