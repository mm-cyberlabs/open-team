package com.openteam.service;

import com.openteam.model.User;
import com.openteam.model.UserRole;
import com.openteam.model.Workspace;
import com.openteam.repository.UserRepository;
import com.openteam.repository.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing users and user-related operations.
 * Handles user creation, updates, role management, and workspace assignments.
 */
public class UserManagementService {
    private static final Logger logger = LoggerFactory.getLogger(UserManagementService.class);
    
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AuthenticationService authService;
    
    public UserManagementService() {
        this.userRepository = new UserRepository();
        this.workspaceRepository = new WorkspaceRepository();
        this.authService = new AuthenticationService();
    }
    
    /**
     * Creates a new user.
     * Super admins can create any user type, workspace admins can create users in their workspace.
     * 
     * @param username Username for the new user
     * @param fullName Full name for the new user
     * @param email Email for the new user
     * @param password Initial password for the new user
     * @param role Role for the new user
     * @param workspaceId Workspace ID (required for non-super-admin users)
     * @param currentUser Current logged-in user creating this user
     * @return Created user, or null if operation failed
     */
    public User createUser(String username, String fullName, String email, String password, 
                          UserRole role, Long workspaceId, User currentUser) {
        
        // Check permissions
        if (!canCreateUser(role, workspaceId, currentUser)) {
            logger.warn("User {} attempted to create user with insufficient permissions", 
                currentUser.getUsername());
            return null;
        }
        
        // Check if username already exists
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            logger.warn("Username '{}' already exists", username);
            return null;
        }
        
        // Validate workspace for non-super-admin users
        Workspace workspace = null;
        if (role != UserRole.SUPER_ADMIN) {
            if (workspaceId == null) {
                logger.warn("Workspace ID required for non-super-admin user");
                return null;
            }
            
            Optional<Workspace> workspaceOpt = workspaceRepository.findById(workspaceId);
            if (workspaceOpt.isEmpty()) {
                logger.warn("Workspace not found: {}", workspaceId);
                return null;
            }
            workspace = workspaceOpt.get();
        }
        
        try {
            String hashedPassword = authService.hashPassword(password);
            
            User user = new User(username, fullName, email, role, workspace);
            user.setPasswordHash(hashedPassword);
            
            user = userRepository.save(user);
            
            logger.info("User created: {} with role {} by user: {}", 
                username, role, currentUser.getUsername());
            return user;
            
        } catch (Exception e) {
            logger.error("Error creating user: " + username, e);
            return null;
        }
    }
    
    /**
     * Updates an existing user.
     * Super admins can update any user, workspace admins can update users in their workspace.
     * 
     * @param userId User ID to update
     * @param fullName New full name
     * @param email New email
     * @param role New role (optional, can be null to keep existing)
     * @param workspaceId New workspace ID (optional, can be null to keep existing)
     * @param currentUser Current logged-in user
     * @return Updated user, or null if operation failed
     */
    public User updateUser(Long userId, String fullName, String email, UserRole role, 
                          Long workspaceId, User currentUser) {
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            logger.warn("User not found for update: {}", userId);
            return null;
        }
        
        User user = userOpt.get();
        
        // Check permissions
        if (!canManageUser(user, currentUser)) {
            logger.warn("User {} attempted to update user {} without permission", 
                currentUser.getUsername(), userId);
            return null;
        }
        
        // Validate role and workspace changes
        if (role != null && role != user.getRole()) {
            if (!canAssignRole(role, currentUser)) {
                logger.warn("User {} attempted to assign role {} without permission", 
                    currentUser.getUsername(), role);
                return null;
            }
        }
        
        // Handle workspace assignment
        Workspace newWorkspace = null;
        if (workspaceId != null) {
            Optional<Workspace> workspaceOpt = workspaceRepository.findById(workspaceId);
            if (workspaceOpt.isEmpty()) {
                logger.warn("Workspace not found: {}", workspaceId);
                return null;
            }
            newWorkspace = workspaceOpt.get();
            
            // Check if current user can assign to this workspace
            if (!currentUser.isSuperAdmin()) {
                if (currentUser.getWorkspaceId() == null || 
                    !currentUser.getWorkspaceId().equals(workspaceId)) {
                    logger.warn("User {} attempted to assign user to workspace {} without permission", 
                        currentUser.getUsername(), workspaceId);
                    return null;
                }
            }
        }
        
        try {
            user.setFullName(fullName);
            user.setEmail(email);
            
            if (role != null) {
                user.setRole(role);
            }
            
            if (newWorkspace != null) {
                user.setWorkspace(newWorkspace);
            }
            
            user = userRepository.save(user);
            
            logger.info("User updated: {} by user: {}", user.getUsername(), currentUser.getUsername());
            return user;
            
        } catch (Exception e) {
            logger.error("Error updating user: " + userId, e);
            return null;
        }
    }
    
    /**
     * Deactivates a user.
     * Super admins can deactivate any user, workspace admins can deactivate users in their workspace.
     * 
     * @param userId User ID to deactivate
     * @param currentUser Current logged-in user
     * @return true if deactivation was successful
     */
    public boolean deactivateUser(Long userId, User currentUser) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            logger.warn("User not found for deactivation: {}", userId);
            return false;
        }
        
        User user = userOpt.get();
        
        // Check permissions
        if (!canManageUser(user, currentUser)) {
            logger.warn("User {} attempted to deactivate user {} without permission", 
                currentUser.getUsername(), userId);
            return false;
        }
        
        // Don't allow deactivating self
        if (user.getId().equals(currentUser.getId())) {
            logger.warn("User {} attempted to deactivate themselves", currentUser.getUsername());
            return false;
        }
        
        try {
            userRepository.deleteById(userId);
            
            logger.info("User deactivated: {} by user: {}", user.getUsername(), currentUser.getUsername());
            return true;
            
        } catch (Exception e) {
            logger.error("Error deactivating user: " + userId, e);
            return false;
        }
    }
    
    /**
     * Gets users that the current user can manage.
     * 
     * @param currentUser Current logged-in user
     * @return List of manageable users
     */
    public List<User> getManageableUsers(User currentUser) {
        if (currentUser.isSuperAdmin()) {
            return userRepository.findAllActive();
        } else if (currentUser.isAdmin() && currentUser.getWorkspaceId() != null) {
            return userRepository.findByWorkspaceId(currentUser.getWorkspaceId());
        } else {
            return List.of();
        }
    }
    
    /**
     * Resets a user's password.
     * Super admins can reset any password, workspace admins can reset passwords in their workspace.
     * 
     * @param userId User ID to reset password for
     * @param newPassword New password
     * @param currentUser Current logged-in user
     * @return true if password reset was successful
     */
    public boolean resetUserPassword(Long userId, String newPassword, User currentUser) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            logger.warn("User not found for password reset: {}", userId);
            return false;
        }
        
        User user = userOpt.get();
        
        // Check permissions
        if (!canManageUser(user, currentUser)) {
            logger.warn("User {} attempted to reset password for user {} without permission", 
                currentUser.getUsername(), userId);
            return false;
        }
        
        try {
            String hashedPassword = authService.hashPassword(newPassword);
            user.setPasswordHash(hashedPassword);
            userRepository.save(user);
            
            // Invalidate all sessions for the user to force re-login
            authService.logoutAllSessions(userId);
            
            logger.info("Password reset for user: {} by user: {}", user.getUsername(), currentUser.getUsername());
            return true;
            
        } catch (Exception e) {
            logger.error("Error resetting password for user: " + userId, e);
            return false;
        }
    }
    
    /**
     * Checks if current user can create a user with the specified role and workspace.
     * 
     * @param role Role to assign
     * @param workspaceId Workspace ID to assign
     * @param currentUser Current logged-in user
     * @return true if creation is allowed
     */
    private boolean canCreateUser(UserRole role, Long workspaceId, User currentUser) {
        if (currentUser.isSuperAdmin()) {
            return true;
        }
        
        if (currentUser.getRole() == UserRole.ADMIN) {
            // Workspace admins can only create USER role in their workspace
            return role == UserRole.USER && 
                   workspaceId != null && 
                   workspaceId.equals(currentUser.getWorkspaceId());
        }
        
        return false;
    }
    
    /**
     * Checks if current user can assign a specific role.
     * 
     * @param role Role to assign
     * @param currentUser Current logged-in user
     * @return true if role assignment is allowed
     */
    private boolean canAssignRole(UserRole role, User currentUser) {
        if (currentUser.isSuperAdmin()) {
            return true;
        }
        
        if (currentUser.getRole() == UserRole.ADMIN) {
            // Workspace admins can only assign USER role
            return role == UserRole.USER;
        }
        
        return false;
    }
    
    /**
     * Checks if current user can manage (edit/delete) a specific user.
     * 
     * @param user User to manage
     * @param currentUser Current logged-in user
     * @return true if management is allowed
     */
    private boolean canManageUser(User user, User currentUser) {
        if (currentUser.isSuperAdmin()) {
            return true;
        }
        
        if (currentUser.getRole() == UserRole.ADMIN) {
            // Workspace admins can manage users in their workspace (except other admins)
            return user.getRole() == UserRole.USER && 
                   currentUser.getWorkspaceId() != null &&
                   currentUser.getWorkspaceId().equals(user.getWorkspaceId());
        }
        
        return false;
    }
}