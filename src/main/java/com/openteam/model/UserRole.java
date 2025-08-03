package com.openteam.model;

/**
 * Enumeration for user roles in the Open Team application.
 * Defines the permission levels for different types of users.
 */
public enum UserRole {
    /**
     * Super Administrator - Can manage all workspaces and system settings
     */
    SUPER_ADMIN,
    
    /**
     * Administrator - Can manage a specific workspace
     */
    ADMIN,
    
    /**
     * Regular User - Can work within a specific workspace
     */
    USER
}