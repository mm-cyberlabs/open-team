package com.openteam.model;

/**
 * Environment enumeration for deployment environments.
 */
public enum Environment {
    DEV("Development", "#4CAF50"),
    STAGING("Staging", "#FF9800"),
    PRODUCTION("Production", "#F44336");
    
    private final String displayName;
    private final String colorCode;
    
    Environment(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColorCode() {
        return colorCode;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}