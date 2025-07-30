package com.openteam.model;

/**
 * Deployment status enumeration for tracking deployment progress.
 */
public enum DeploymentStatus {
    PLANNED("Planned", "#2196F3"),
    IN_PROGRESS("In Progress", "#FF9800"),
    COMPLETED("Completed", "#4CAF50"),
    FAILED("Failed", "#F44336"),
    ROLLED_BACK("Rolled Back", "#9C27B0");
    
    private final String displayName;
    private final String colorCode;
    
    DeploymentStatus(String displayName, String colorCode) {
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