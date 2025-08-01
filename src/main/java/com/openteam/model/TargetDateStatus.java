package com.openteam.model;

/**
 * Enumeration of possible target date statuses.
 */
public enum TargetDateStatus {
    PENDING("Pending", "#2196F3"),
    IN_PROGRESS("In Progress", "#FF9800"),
    COMPLETED("Completed", "#4CAF50"),
    CANCELLED("Cancelled", "#F44336");
    
    private final String displayName;
    private final String colorCode;
    
    TargetDateStatus(String displayName, String colorCode) {
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