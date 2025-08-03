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
 * Service for managing workspaces and workspace-related operations.
 * Handles workspace creation, management, and user assignments.
 */
public class WorkspaceService {
    private static final Logger logger = LoggerFactory.getLogger(WorkspaceService.class);
    
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    
    public WorkspaceService() {
        this.workspaceRepository = new WorkspaceRepository();
        this.userRepository = new UserRepository();
    }
    
    /**
     * Gets all active workspaces.
     * Super admins can see all workspaces, admins see only their workspace.
     * 
     * @param currentUser Current logged-in user
     * @return List of workspaces the user can access
     */
    public List<Workspace> getAccessibleWorkspaces(User currentUser) {
        if (currentUser.isSuperAdmin()) {
            return workspaceRepository.findAllActive();
        } else if (currentUser.isAdmin() && currentUser.getWorkspace() != null) {
            // Admin can only see their own workspace
            return List.of(currentUser.getWorkspace());
        } else {
            // Regular users don't manage workspaces
            return List.of();
        }
    }
    
    /**
     * Creates a new workspace.
     * Only super admins can create workspaces.
     * 
     * @param name Workspace name
     * @param description Workspace description
     * @param currentUser Current logged-in user
     * @return Created workspace, or null if operation failed
     */
    public Workspace createWorkspace(String name, String description, User currentUser) {
        if (!currentUser.isSuperAdmin()) {
            logger.warn("Non-super-admin user attempted to create workspace: {}", currentUser.getUsername());
            return null;
        }
        
        // Check if workspace name already exists
        Optional<Workspace> existing = workspaceRepository.findByName(name);
        if (existing.isPresent()) {
            logger.warn("Workspace with name '{}' already exists", name);
            return null;
        }
        
        try {
            Workspace workspace = new Workspace(name, description);
            workspace = workspaceRepository.save(workspace);
            
            logger.info("Workspace created: {} by user: {}", name, currentUser.getUsername());
            return workspace;
            
        } catch (Exception e) {
            logger.error("Error creating workspace: " + name, e);
            return null;
        }
    }
    
    /**
     * Updates an existing workspace.
     * Super admins can update any workspace, workspace admins can update their own.
     * 
     * @param workspaceId Workspace ID to update
     * @param name New workspace name
     * @param description New workspace description
     * @param currentUser Current logged-in user
     * @return Updated workspace, or null if operation failed
     */
    public Workspace updateWorkspace(Long workspaceId, String name, String description, User currentUser) {
        Optional<Workspace> workspaceOpt = workspaceRepository.findById(workspaceId);
        
        if (workspaceOpt.isEmpty()) {
            logger.warn("Workspace not found for update: {}", workspaceId);
            return null;
        }
        
        Workspace workspace = workspaceOpt.get();
        
        // Check permissions
        if (!canManageWorkspace(workspace, currentUser)) {
            logger.warn("User {} attempted to update workspace {} without permission", 
                currentUser.getUsername(), workspaceId);
            return null;
        }
        
        // Check if new name conflicts with existing workspace (if name changed)
        if (!workspace.getName().equals(name)) {
            Optional<Workspace> existing = workspaceRepository.findByName(name);
            if (existing.isPresent() && !existing.get().getId().equals(workspaceId)) {
                logger.warn("Workspace name '{}' already exists", name);
                return null;
            }
        }
        
        try {
            workspace.setName(name);
            workspace.setDescription(description);
            workspace = workspaceRepository.save(workspace);
            
            logger.info("Workspace updated: {} by user: {}", name, currentUser.getUsername());
            return workspace;
            
        } catch (Exception e) {
            logger.error("Error updating workspace: " + workspaceId, e);
            return null;
        }
    }
    
    /**
     * Deletes (deactivates) a workspace.
     * Only super admins can delete workspaces.
     * 
     * @param workspaceId Workspace ID to delete
     * @param currentUser Current logged-in user
     * @return true if deletion was successful
     */
    public boolean deleteWorkspace(Long workspaceId, User currentUser) {
        if (!currentUser.isSuperAdmin()) {
            logger.warn("Non-super-admin user attempted to delete workspace: {}", currentUser.getUsername());
            return false;
        }
        
        Optional<Workspace> workspaceOpt = workspaceRepository.findById(workspaceId);
        
        if (workspaceOpt.isEmpty()) {
            logger.warn("Workspace not found for deletion: {}", workspaceId);
            return false;
        }
        
        try {
            // Check if there are any users in this workspace
            List<User> workspaceUsers = userRepository.findByWorkspaceId(workspaceId);
            if (!workspaceUsers.isEmpty()) {
                logger.warn("Cannot delete workspace {} - contains {} users", workspaceId, workspaceUsers.size());
                return false;
            }
            
            workspaceRepository.deleteById(workspaceId);
            
            logger.info("Workspace deleted: {} by user: {}", workspaceId, currentUser.getUsername());
            return true;
            
        } catch (Exception e) {
            logger.error("Error deleting workspace: " + workspaceId, e);
            return false;
        }
    }
    
    /**
     * Gets a workspace by ID.
     * 
     * @param workspaceId Workspace ID
     * @param currentUser Current logged-in user
     * @return Workspace if found and accessible, null otherwise
     */
    public Workspace getWorkspace(Long workspaceId, User currentUser) {
        Optional<Workspace> workspaceOpt = workspaceRepository.findById(workspaceId);
        
        if (workspaceOpt.isEmpty()) {
            return null;
        }
        
        Workspace workspace = workspaceOpt.get();
        
        // Check access permissions
        if (!canAccessWorkspace(workspace, currentUser)) {
            logger.warn("User {} attempted to access workspace {} without permission", 
                currentUser.getUsername(), workspaceId);
            return null;
        }
        
        return workspace;
    }
    
    /**
     * Gets all users in a workspace.
     * 
     * @param workspaceId Workspace ID
     * @param currentUser Current logged-in user
     * @return List of users in the workspace
     */
    public List<User> getWorkspaceUsers(Long workspaceId, User currentUser) {
        Optional<Workspace> workspaceOpt = workspaceRepository.findById(workspaceId);
        
        if (workspaceOpt.isEmpty()) {
            return List.of();
        }
        
        Workspace workspace = workspaceOpt.get();
        
        // Check permissions
        if (!canManageWorkspace(workspace, currentUser)) {
            logger.warn("User {} attempted to view users in workspace {} without permission", 
                currentUser.getUsername(), workspaceId);
            return List.of();
        }
        
        return userRepository.findByWorkspaceId(workspaceId);
    }
    
    /**
     * Checks if a user can access a workspace.
     * 
     * @param workspace Workspace to check
     * @param user User to check permissions for
     * @return true if user can access the workspace
     */
    private boolean canAccessWorkspace(Workspace workspace, User user) {
        if (user.isSuperAdmin()) {
            return true;
        }
        
        if (user.getWorkspace() != null) {
            return user.getWorkspace().getId().equals(workspace.getId());
        }
        
        return false;
    }
    
    /**
     * Checks if a user can manage (edit/delete) a workspace.
     * 
     * @param workspace Workspace to check
     * @param user User to check permissions for
     * @return true if user can manage the workspace
     */
    private boolean canManageWorkspace(Workspace workspace, User user) {
        if (user.isSuperAdmin()) {
            return true;
        }
        
        if (user.getRole() == UserRole.ADMIN && user.getWorkspace() != null) {
            return user.getWorkspace().getId().equals(workspace.getId());
        }
        
        return false;
    }
}