package com.openteam.model;

/**
 * Activity type enumeration for team activities.
 */
public enum ActivityType {
    MEETING("Meeting", "#2196F3"),
    TRAINING("Training", "#4CAF50"),
    EVENT("Event", "#FF9800"),
    GENERAL("General", "#9C27B0");
    
    private final String displayName;
    private final String colorCode;
    
    ActivityType(String displayName, String colorCode) {
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