package com.openteam.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Deployment entity representing software deployment tracking.
 * Contains deployment details, version information, and audit fields.
 */
public class Deployment {
    private Long id;
    private String releaseName;
    private String version;
    private LocalDateTime deploymentDateTime;
    private User driverUser;
    private String releaseNotes;
    private Environment environment;
    private DeploymentStatus status;
    private String ticketNumber;
    private String documentationUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private User createdBy;
    private User updatedBy;
    private Boolean isArchived;
    
    public Deployment() {
        this.environment = Environment.PRODUCTION;
        this.status = DeploymentStatus.PLANNED;
        this.isArchived = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Deployment(String releaseName, String version, LocalDateTime deploymentDateTime,
                     User driverUser, String releaseNotes, Environment environment,
                     DeploymentStatus status, User createdBy) {
        this();
        this.releaseName = releaseName;
        this.version = version;
        this.deploymentDateTime = deploymentDateTime;
        this.driverUser = driverUser;
        this.releaseNotes = releaseNotes;
        this.environment = environment;
        this.status = status;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }
    
    public Deployment(String releaseName, String version, LocalDateTime deploymentDateTime,
                     User driverUser, String releaseNotes, Environment environment,
                     DeploymentStatus status, String ticketNumber, String documentationUrl, User createdBy) {
        this();
        this.releaseName = releaseName;
        this.version = version;
        this.deploymentDateTime = deploymentDateTime;
        this.driverUser = driverUser;
        this.releaseNotes = releaseNotes;
        this.environment = environment;
        this.status = status;
        this.ticketNumber = ticketNumber;
        this.documentationUrl = documentationUrl;
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
    
    public String getReleaseName() {
        return releaseName;
    }
    
    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public LocalDateTime getDeploymentDateTime() {
        return deploymentDateTime;
    }
    
    public void setDeploymentDateTime(LocalDateTime deploymentDateTime) {
        this.deploymentDateTime = deploymentDateTime;
    }
    
    public User getDriverUser() {
        return driverUser;
    }
    
    public void setDriverUser(User driverUser) {
        this.driverUser = driverUser;
    }
    
    public String getReleaseNotes() {
        return releaseNotes;
    }
    
    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }
    
    public Environment getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    
    public DeploymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(DeploymentStatus status) {
        this.status = status;
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
    
    public Boolean getIsArchived() {
        return isArchived;
    }
    
    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }
    
    public String getTicketNumber() {
        return ticketNumber;
    }
    
    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }
    
    public String getDocumentationUrl() {
        return documentationUrl;
    }
    
    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deployment that = (Deployment) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Deployment{" +
                "id=" + id +
                ", releaseName='" + releaseName + '\'' +
                ", version='" + version + '\'' +
                ", environment=" + environment +
                ", status=" + status +
                ", deploymentDateTime=" + deploymentDateTime +
                ", isArchived=" + isArchived +
                '}';
    }
}