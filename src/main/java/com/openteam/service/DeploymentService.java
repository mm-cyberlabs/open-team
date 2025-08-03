package com.openteam.service;

import com.openteam.model.Deployment;
import com.openteam.model.DeploymentComment;
import com.openteam.model.DeploymentStatus;
import com.openteam.model.Environment;
import com.openteam.model.User;
import com.openteam.repository.DeploymentRepository;
import com.openteam.repository.DeploymentCommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing software deployments.
 * Provides business logic for deployment tracking operations.
 */
public class DeploymentService {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentService.class);
    
    private final DeploymentRepository deploymentRepository;
    private final DeploymentCommentRepository commentRepository;
    
    public DeploymentService() {
        this.deploymentRepository = new DeploymentRepository();
        this.commentRepository = new DeploymentCommentRepository();
    }
    
    /**
     * Retrieves all deployments.
     * 
     * @return List of all deployments ordered by deployment date
     */
    public List<Deployment> getAllDeployments() {
        logger.debug("Retrieving all deployments");
        return deploymentRepository.findAll();
    }
    
    /**
     * Retrieves non-archived deployments.
     * 
     * @return List of non-archived deployments ordered by deployment date
     */
    public List<Deployment> getNonArchivedDeployments() {
        logger.debug("Retrieving non-archived deployments");
        return deploymentRepository.findNonArchived();
    }
    
    /**
     * Retrieves a deployment by ID.
     * 
     * @param id Deployment ID
     * @return Optional containing the deployment if found
     */
    public Optional<Deployment> getDeploymentById(Long id) {
        logger.debug("Retrieving deployment with ID: {}", id);
        return deploymentRepository.findById(id);
    }
    
    /**
     * Creates a new deployment.
     * 
     * @param releaseName Name of the release
     * @param version Version number
     * @param deploymentDateTime When the deployment is scheduled/occurred
     * @param driverUser User responsible for the deployment
     * @param releaseNotes Notes about the release
     * @param environment Target environment
     * @param status Deployment status
     * @param createdBy User creating the deployment record
     * @return Created deployment
     */
    public Deployment createDeployment(String releaseName, String version, 
                                     LocalDateTime deploymentDateTime, User driverUser,
                                     String releaseNotes, Environment environment,
                                     DeploymentStatus status, User createdBy) {
        return createDeployment(releaseName, version, deploymentDateTime, driverUser,
                               releaseNotes, environment, status, null, null, createdBy);
    }
    
    /**
     * Creates a new deployment with ticket number and documentation URL.
     * 
     * @param releaseName Name of the release
     * @param version Version number
     * @param deploymentDateTime When the deployment is scheduled/occurred
     * @param driverUser User responsible for the deployment
     * @param releaseNotes Notes about the release
     * @param environment Target environment
     * @param status Deployment status
     * @param ticketNumber Ticket number for tracking
     * @param documentationUrl URL to documentation
     * @param createdBy User creating the deployment record
     * @return Created deployment
     */
    public Deployment createDeployment(String releaseName, String version, 
                                     LocalDateTime deploymentDateTime, User driverUser,
                                     String releaseNotes, Environment environment,
                                     DeploymentStatus status, String ticketNumber, 
                                     String documentationUrl, User createdBy) {
        logger.info("Creating new deployment: {} v{}", releaseName, version);
        
        validateDeploymentData(releaseName, version, deploymentDateTime, driverUser, createdBy);
        
        Deployment deployment = new Deployment(createdBy.getWorkspace(), releaseName, version, deploymentDateTime,
                                             driverUser, releaseNotes, environment, 
                                             status, ticketNumber, documentationUrl, createdBy);
        deployment.setCreatedAt(LocalDateTime.now());
        deployment.setUpdatedAt(LocalDateTime.now());
        
        return deploymentRepository.save(deployment);
    }
    
    /**
     * Updates an existing deployment.
     * 
     * @param id Deployment ID
     * @param releaseName New release name
     * @param version New version
     * @param deploymentDateTime New deployment date/time
     * @param driverUser New driver user
     * @param releaseNotes New release notes
     * @param environment New environment
     * @param status New status
     * @param updatedBy User updating the deployment
     * @return Updated deployment
     * @throws IllegalArgumentException if deployment not found
     */
    public Deployment updateDeployment(Long id, String releaseName, String version,
                                     LocalDateTime deploymentDateTime, User driverUser,
                                     String releaseNotes, Environment environment,
                                     DeploymentStatus status, User updatedBy) {
        return updateDeployment(id, releaseName, version, deploymentDateTime, driverUser,
                               releaseNotes, environment, status, null, null, updatedBy);
    }
    
    /**
     * Updates an existing deployment with ticket number and documentation URL.
     * 
     * @param id Deployment ID
     * @param releaseName New release name
     * @param version New version
     * @param deploymentDateTime New deployment date/time
     * @param driverUser New driver user
     * @param releaseNotes New release notes
     * @param environment New environment
     * @param status New status
     * @param ticketNumber New ticket number
     * @param documentationUrl New documentation URL
     * @param updatedBy User updating the deployment
     * @return Updated deployment
     * @throws IllegalArgumentException if deployment not found
     */
    public Deployment updateDeployment(Long id, String releaseName, String version,
                                     LocalDateTime deploymentDateTime, User driverUser,
                                     String releaseNotes, Environment environment,
                                     DeploymentStatus status, String ticketNumber,
                                     String documentationUrl, User updatedBy) {
        logger.info("Updating deployment with ID: {}", id);
        
        validateDeploymentData(releaseName, version, deploymentDateTime, driverUser, updatedBy);
        
        Optional<Deployment> existingOpt = deploymentRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Deployment not found with ID: " + id);
        }
        
        Deployment existing = existingOpt.get();
        existing.setReleaseName(releaseName);
        existing.setVersion(version);
        existing.setDeploymentDateTime(deploymentDateTime);
        existing.setDriverUser(driverUser);
        existing.setReleaseNotes(releaseNotes);
        existing.setTicketNumber(ticketNumber);
        existing.setDocumentationUrl(documentationUrl);
        existing.setEnvironment(environment);
        existing.setStatus(status);
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedAt(LocalDateTime.now());
        
        // Auto-archive deployment when status is changed to COMPLETED
        if (status == DeploymentStatus.COMPLETED && !Boolean.TRUE.equals(existing.getIsArchived())) {
            logger.info("Auto-archiving deployment {} as it was marked as COMPLETED", id);
            existing.setIsArchived(true);
        }
        
        return deploymentRepository.save(existing);
    }
    
    /**
     * Updates deployment status.
     * 
     * @param id Deployment ID
     * @param status New status
     * @param updatedBy User updating the status
     * @return Updated deployment
     * @throws IllegalArgumentException if deployment not found
     */
    public Deployment updateDeploymentStatus(Long id, DeploymentStatus status, User updatedBy) {
        logger.info("Updating deployment status for ID: {} to {}", id, status);
        
        Optional<Deployment> existingOpt = deploymentRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Deployment not found with ID: " + id);
        }
        
        Deployment existing = existingOpt.get();
        existing.setStatus(status);
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedAt(LocalDateTime.now());
        
        // Auto-archive deployment when status is changed to COMPLETED
        if (status == DeploymentStatus.COMPLETED && !Boolean.TRUE.equals(existing.getIsArchived())) {
            logger.info("Auto-archiving deployment {} as it was marked as COMPLETED", id);
            existing.setIsArchived(true);
        }
        
        return deploymentRepository.save(existing);
    }
    
    /**
     * Retrieves deployments by status.
     * 
     * @param status Deployment status to filter by
     * @return List of deployments with specified status
     */
    public List<Deployment> getDeploymentsByStatus(DeploymentStatus status) {
        logger.debug("Retrieving deployments with status: {}", status);
        return deploymentRepository.findByStatus(status);
    }
    
    /**
     * Retrieves deployments by environment.
     * 
     * @param environment Environment to filter by
     * @return List of deployments in specified environment
     */
    public List<Deployment> getDeploymentsByEnvironment(Environment environment) {
        logger.debug("Retrieving deployments for environment: {}", environment);
        return deploymentRepository.findByEnvironment(environment);
    }
    
    /**
     * Retrieves recent deployments (last 30 days).
     * 
     * @return List of recent deployments
     */
    public List<Deployment> getRecentDeployments() {
        logger.debug("Retrieving recent deployments");
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        return deploymentRepository.findAll().stream()
                .filter(deployment -> deployment.getDeploymentDateTime().isAfter(thirtyDaysAgo))
                .sorted((d1, d2) -> d2.getDeploymentDateTime().compareTo(d1.getDeploymentDateTime()))
                .toList();
    }
    
    /**
     * Retrieves production deployments.
     * 
     * @return List of production deployments
     */
    public List<Deployment> getProductionDeployments() {
        logger.debug("Retrieving production deployments");
        return deploymentRepository.findByEnvironment(Environment.PRODUCTION);
    }
    
    /**
     * Retrieves active deployments (IN_PROGRESS status).
     * 
     * @return List of active deployments
     */
    public List<Deployment> getActiveDeployments() {
        logger.debug("Retrieving active deployments");
        return deploymentRepository.findByStatus(DeploymentStatus.IN_PROGRESS);
    }
    
    /**
     * Validates deployment data before saving.
     * 
     * @param releaseName Release name
     * @param version Version string
     * @param deploymentDateTime Deployment date/time
     * @param driverUser Driver user
     * @param user User associated with the operation
     * @throws IllegalArgumentException if validation fails
     */
    private void validateDeploymentData(String releaseName, String version, 
                                      LocalDateTime deploymentDateTime, User driverUser, User user) {
        if (releaseName == null || releaseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Release name cannot be empty");
        }
        
        if (releaseName.length() > 100) {
            throw new IllegalArgumentException("Release name cannot exceed 100 characters");
        }
        
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version cannot be empty");
        }
        
        if (version.length() > 50) {
            throw new IllegalArgumentException("Version cannot exceed 50 characters");
        }
        
        if (deploymentDateTime == null) {
            throw new IllegalArgumentException("Deployment date/time is required");
        }
        
        if (driverUser == null || driverUser.getId() == null) {
            throw new IllegalArgumentException("Valid driver user is required");
        }
        
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Valid user is required for deployment operations");
        }
    }
    
    /**
     * Deletes a deployment (soft delete).
     * 
     * @param id Deployment ID to delete
     */
    public void deleteDeployment(Long id) {
        logger.info("Deleting deployment with ID: {}", id);
        
        Optional<Deployment> existingOpt = deploymentRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Deployment not found with ID: " + id);
        }
        
        deploymentRepository.deleteById(id);
    }
    
    /**
     * Searches deployments by release name, version, or release notes using local fuzzy search.
     * 
     * @param searchTerm Search term
     * @param includeArchived Whether to include archived deployments
     * @return List of matching deployments sorted by deployment date DESC
     */
    public List<Deployment> searchDeployments(String searchTerm, boolean includeArchived) {
        logger.debug("Searching deployments with term: {}, includeArchived: {}", searchTerm, includeArchived);
        
        // Get all deployments first (from memory/cache)
        List<Deployment> allDeployments = includeArchived ? getAllDeployments() : getNonArchivedDeployments();
        
        // If no search term, return all deployments
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return allDeployments;
        }
        
        // Perform local fuzzy search
        String searchTermLower = searchTerm.trim().toLowerCase();
        
        return allDeployments.stream()
                .filter(deployment -> fuzzyMatch(deployment, searchTermLower))
                .sorted((d1, d2) -> d2.getDeploymentDateTime().compareTo(d1.getDeploymentDateTime()))
                .toList();
    }
    
    /**
     * Performs fuzzy matching on a deployment against a search term.
     * Searches in release name, version, and release notes.
     * 
     * @param deployment Deployment to check
     * @param searchTermLower Search term in lowercase
     * @return true if deployment matches search term
     */
    private boolean fuzzyMatch(Deployment deployment, String searchTermLower) {
        // Check release name
        if (deployment.getReleaseName() != null && 
            deployment.getReleaseName().toLowerCase().contains(searchTermLower)) {
            return true;
        }
        
        // Check version
        if (deployment.getVersion() != null && 
            deployment.getVersion().toLowerCase().contains(searchTermLower)) {
            return true;
        }
        
        // Check release notes
        if (deployment.getReleaseNotes() != null && 
            deployment.getReleaseNotes().toLowerCase().contains(searchTermLower)) {
            return true;
        }
        
        // Check environment display name
        if (deployment.getEnvironment() != null && 
            deployment.getEnvironment().getDisplayName().toLowerCase().contains(searchTermLower)) {
            return true;
        }
        
        // Check status display name
        if (deployment.getStatus() != null && 
            deployment.getStatus().getDisplayName().toLowerCase().contains(searchTermLower)) {
            return true;
        }
        
        // Check driver user name
        if (deployment.getDriverUser() != null && deployment.getDriverUser().getFullName() != null &&
            deployment.getDriverUser().getFullName().toLowerCase().contains(searchTermLower)) {
            return true;
        }
        
        // Check ticket number
        if (deployment.getTicketNumber() != null && 
            deployment.getTicketNumber().toLowerCase().contains(searchTermLower)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Unarchives a deployment.
     * 
     * @param id Deployment ID to unarchive
     */
    public void unarchiveDeployment(Long id) {
        logger.info("Unarchiving deployment with ID: {}", id);
        
        Optional<Deployment> existingOpt = deploymentRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Deployment not found with ID: " + id);
        }
        
        deploymentRepository.unarchiveById(id);
    }
    
    /**
     * Retrieves all comments for a deployment in descending order by creation date.
     * 
     * @param deploymentId Deployment ID
     * @return List of comments for the deployment
     */
    public List<DeploymentComment> getCommentsForDeployment(Long deploymentId) {
        logger.debug("Retrieving comments for deployment ID: {}", deploymentId);
        return commentRepository.findByDeploymentId(deploymentId);
    }
    
    /**
     * Adds a comment to a deployment.
     * 
     * @param deploymentId Deployment ID
     * @param commentText Comment text
     * @param createdBy User creating the comment
     * @return Created comment
     * @throws IllegalArgumentException if deployment not found or validation fails
     */
    public DeploymentComment addCommentToDeployment(Long deploymentId, String commentText, User createdBy) {
        logger.info("Adding comment to deployment ID: {}", deploymentId);
        
        // Validate inputs
        if (commentText == null || commentText.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be empty");
        }
        
        if (commentText.length() > 2000) {
            throw new IllegalArgumentException("Comment text cannot exceed 2000 characters");
        }
        
        if (createdBy == null || createdBy.getId() == null) {
            throw new IllegalArgumentException("Valid user is required for creating comments");
        }
        
        // Verify deployment exists
        Optional<Deployment> deployment = deploymentRepository.findById(deploymentId);
        if (deployment.isEmpty()) {
            throw new IllegalArgumentException("Deployment not found with ID: " + deploymentId);
        }
        
        DeploymentComment comment = new DeploymentComment(deploymentId, commentText.trim(), createdBy);
        return commentRepository.save(comment);
    }
    
    /**
     * Deletes a comment by ID.
     * 
     * @param commentId Comment ID to delete
     * @throws IllegalArgumentException if comment not found
     */
    public void deleteComment(Long commentId) {
        logger.info("Deleting comment with ID: {}", commentId);
        
        Optional<DeploymentComment> comment = commentRepository.findById(commentId);
        if (comment.isEmpty()) {
            throw new IllegalArgumentException("Comment not found with ID: " + commentId);
        }
        
        commentRepository.deleteById(commentId);
    }
    
    /**
     * Gets the count of comments for a deployment.
     * 
     * @param deploymentId Deployment ID
     * @return Number of comments
     */
    public int getCommentCountForDeployment(Long deploymentId) {
        logger.debug("Getting comment count for deployment ID: {}", deploymentId);
        return commentRepository.countByDeploymentId(deploymentId);
    }
    
    /**
     * Searches deployments by workspace with optional search term and archive filter.
     * 
     * @param searchTerm Search term (can be null or empty for all results)
     * @param includeArchived Whether to include archived deployments
     * @param workspaceId Workspace ID to filter by
     * @return List of matching deployments for the workspace
     */
    public List<Deployment> searchDeploymentsByWorkspace(String searchTerm, boolean includeArchived, Long workspaceId) {
        logger.debug("Searching deployments for workspace: {}, searchTerm: '{}', includeArchived: {}", 
                    workspaceId, searchTerm, includeArchived);
        return deploymentRepository.searchByWorkspace(searchTerm, includeArchived, workspaceId);
    }
    
    /**
     * Retrieves deployments by environment and workspace.
     * 
     * @param environment Environment to filter by
     * @param workspaceId Workspace ID to filter by
     * @return List of deployments with specified environment for the workspace
     */
    public List<Deployment> getDeploymentsByEnvironmentAndWorkspace(Environment environment, Long workspaceId) {
        logger.debug("Retrieving deployments with environment: {} for workspace: {}", environment, workspaceId);
        return deploymentRepository.findByEnvironmentAndWorkspace(environment, workspaceId);
    }
    
    /**
     * Retrieves deployments by status and workspace.
     * 
     * @param status Deployment status to filter by
     * @param workspaceId Workspace ID to filter by
     * @return List of deployments with specified status for the workspace
     */
    public List<Deployment> getDeploymentsByStatusAndWorkspace(DeploymentStatus status, Long workspaceId) {
        logger.debug("Retrieving deployments with status: {} for workspace: {}", status, workspaceId);
        return deploymentRepository.findByStatusAndWorkspace(status, workspaceId);
    }
}