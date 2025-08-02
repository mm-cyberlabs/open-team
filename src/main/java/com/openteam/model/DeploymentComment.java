package com.openteam.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DeploymentComment entity representing comments on deployments.
 * Contains comment details with audit fields.
 */
public class DeploymentComment {
    private Long id;
    private Long deploymentId;
    private String commentText;
    private LocalDateTime createdAt;
    private User createdBy;
    
    public DeploymentComment() {
        this.createdAt = LocalDateTime.now();
    }
    
    public DeploymentComment(Long deploymentId, String commentText, User createdBy) {
        this();
        this.deploymentId = deploymentId;
        this.commentText = commentText;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getDeploymentId() {
        return deploymentId;
    }
    
    public void setDeploymentId(Long deploymentId) {
        this.deploymentId = deploymentId;
    }
    
    public String getCommentText() {
        return commentText;
    }
    
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeploymentComment that = (DeploymentComment) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DeploymentComment{" +
                "id=" + id +
                ", deploymentId=" + deploymentId +
                ", commentText='" + commentText + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}