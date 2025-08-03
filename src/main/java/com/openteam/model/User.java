package com.openteam.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User entity representing team members in the Open Team application.
 * Contains user information, authentication data, and audit fields.
 */
public class User {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String passwordHash;
    private UserRole role;
    private Workspace workspace;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    private Boolean isActive;
    
    public User() {
        this.isActive = true;
        this.role = UserRole.USER;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public User(String username, String fullName, String email) {
        this();
        this.username = username;
        this.fullName = fullName;
        this.email = email;
    }
    
    public User(String username, String fullName, String email, UserRole role, Workspace workspace) {
        this();
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.workspace = workspace;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public Workspace getWorkspace() {
        return workspace;
    }
    
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
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
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    /**
     * Checks if user is a super administrator
     */
    public boolean isSuperAdmin() {
        return role == UserRole.SUPER_ADMIN;
    }
    
    /**
     * Checks if user is an administrator (either super admin or workspace admin)
     */
    public boolean isAdmin() {
        return role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN;
    }
    
    /**
     * Gets the workspace ID, null for super admins
     */
    public Long getWorkspaceId() {
        return workspace != null ? workspace.getId() : null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", workspace=" + (workspace != null ? workspace.getName() : "null") +
                ", isActive=" + isActive +
                '}';
    }
}