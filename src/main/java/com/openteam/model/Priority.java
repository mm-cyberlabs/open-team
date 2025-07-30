package com.openteam.model;

/**
 * Priority enumeration for announcements and other prioritized entities.
 */
public enum Priority {
    LOW("Low", "#4CAF50"),
    NORMAL("Normal", "#2196F3"),
    HIGH("High", "#FF9800"),
    URGENT("Urgent", "#F44336");
    
    private final String displayName;
    private final String colorCode;
    
    Priority(String displayName, String colorCode) {
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