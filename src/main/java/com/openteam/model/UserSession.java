package com.openteam.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * UserSession entity for tracking user authentication sessions.
 * Manages session tokens and expiration for security purposes.
 */
public class UserSession {
    private Long id;
    private Long userId;
    private String sessionToken;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Boolean isActive;
    
    public UserSession() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    public UserSession(Long userId, String sessionToken, LocalDateTime expiresAt) {
        this();
        this.userId = userId;
        this.sessionToken = sessionToken;
        this.expiresAt = expiresAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getSessionToken() {
        return sessionToken;
    }
    
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    /**
     * Check if the session is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSession that = (UserSession) o;
        return Objects.equals(id, that.id) && Objects.equals(sessionToken, that.sessionToken);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, sessionToken);
    }
    
    @Override
    public String toString() {
        return "UserSession{" +
                "id=" + id +
                ", userId=" + userId +
                ", expiresAt=" + expiresAt +
                ", isActive=" + isActive +
                '}';
    }
}