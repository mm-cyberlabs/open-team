package com.openteam.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * TargetDate entity representing important project milestones and target dates.
 * Contains project and task details with target dates and audit information.
 */
public class TargetDate {
    private Long id;
    private String projectName;
    private String taskName;
    private LocalDateTime targetDate;
    private User driverUser;
    private String documentationUrl;
    private TargetDateStatus status;
    private Boolean isArchived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private User createdBy;
    private User updatedBy;
    
    public TargetDate() {
        this.status = TargetDateStatus.PENDING;
        this.isArchived = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public TargetDate(String projectName, String taskName, LocalDateTime targetDate,
                     User driverUser, String documentationUrl, TargetDateStatus status, User createdBy) {
        this();
        this.projectName = projectName;
        this.taskName = taskName;
        this.targetDate = targetDate;
        this.driverUser = driverUser;
        this.documentationUrl = documentationUrl;
        this.status = status;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public LocalDateTime getTargetDate() {
        return targetDate;
    }
    
    public void setTargetDate(LocalDateTime targetDate) {
        this.targetDate = targetDate;
    }
    
    public User getDriverUser() {
        return driverUser;
    }
    
    public void setDriverUser(User driverUser) {
        this.driverUser = driverUser;
    }
    
    public String getDocumentationUrl() {
        return documentationUrl;
    }
    
    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }
    
    public TargetDateStatus getStatus() {
        return status;
    }
    
    public void setStatus(TargetDateStatus status) {
        this.status = status;
    }
    
    public Boolean getIsArchived() {
        return isArchived;
    }
    
    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public User getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetDate that = (TargetDate) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "TargetDate{" +
                "id=" + id +
                ", projectName='" + projectName + '\'' +
                ", taskName='" + taskName + '\'' +
                ", targetDate=" + targetDate +
                ", status=" + status +
                ", isArchived=" + isArchived +
                '}';
    }
}