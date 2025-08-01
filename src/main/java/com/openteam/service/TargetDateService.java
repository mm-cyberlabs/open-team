package com.openteam.service;

import com.openteam.model.TargetDate;
import com.openteam.model.TargetDateStatus;
import com.openteam.model.User;
import com.openteam.repository.TargetDateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing important target dates and project milestones.
 * Provides business logic for target date operations with auto-archiving.
 */
public class TargetDateService {
    private static final Logger logger = LoggerFactory.getLogger(TargetDateService.class);
    
    private final TargetDateRepository targetDateRepository;
    
    public TargetDateService() {
        this.targetDateRepository = new TargetDateRepository();
    }
    
    /**
     * Retrieves all target dates.
     * 
     * @return List of all target dates ordered by target date
     */
    public List<TargetDate> getAllTargetDates() {
        logger.debug("Retrieving all target dates");
        return targetDateRepository.findAll();
    }
    
    /**
     * Retrieves non-archived target dates.
     * 
     * @return List of non-archived target dates ordered by target date
     */
    public List<TargetDate> getNonArchivedTargetDates() {
        logger.debug("Retrieving non-archived target dates");
        return targetDateRepository.findNonArchived();
    }
    
    /**
     * Retrieves a target date by ID.
     * 
     * @param id Target date ID
     * @return Optional containing the target date if found
     */
    public Optional<TargetDate> getTargetDateById(Long id) {
        logger.debug("Retrieving target date with ID: {}", id);
        return targetDateRepository.findById(id);
    }
    
    /**
     * Creates a new target date.
     * 
     * @param projectName Name of the project
     * @param taskName Name of the task/milestone
     * @param targetDate Target/due date
     * @param driverUser User responsible for the task
     * @param documentationUrl URL to documentation
     * @param status Initial status
     * @param createdBy User creating the target date record
     * @return Created target date
     */
    public TargetDate createTargetDate(String projectName, String taskName, LocalDateTime targetDate,
                                     User driverUser, String documentationUrl, TargetDateStatus status,
                                     User createdBy) {
        logger.info("Creating new target date: {} - {}", projectName, taskName);
        
        validateTargetDateData(projectName, taskName, targetDate, driverUser, createdBy);
        
        TargetDate targetDateEntity = new TargetDate(projectName, taskName, targetDate,
                                                   driverUser, documentationUrl, status, createdBy);
        targetDateEntity.setCreatedAt(LocalDateTime.now());
        targetDateEntity.setUpdatedAt(LocalDateTime.now());
        
        return targetDateRepository.save(targetDateEntity);
    }
    
    /**
     * Updates an existing target date.
     * 
     * @param id Target date ID
     * @param projectName New project name
     * @param taskName New task name
     * @param targetDate New target date
     * @param driverUser New driver user
     * @param documentationUrl New documentation URL
     * @param status New status
     * @param updatedBy User updating the target date
     * @return Updated target date
     * @throws IllegalArgumentException if target date not found
     */
    public TargetDate updateTargetDate(Long id, String projectName, String taskName,
                                     LocalDateTime targetDate, User driverUser, String documentationUrl,
                                     TargetDateStatus status, User updatedBy) {
        logger.info("Updating target date with ID: {}", id);
        
        validateTargetDateData(projectName, taskName, targetDate, driverUser, updatedBy);
        
        Optional<TargetDate> existingOpt = targetDateRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Target date not found with ID: " + id);
        }
        
        TargetDate existing = existingOpt.get();
        existing.setProjectName(projectName);
        existing.setTaskName(taskName);
        existing.setTargetDate(targetDate);
        existing.setDriverUser(driverUser);
        existing.setDocumentationUrl(documentationUrl);
        existing.setStatus(status);
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedAt(LocalDateTime.now());
        
        // Auto-archive target date when status is changed to COMPLETED
        if (status == TargetDateStatus.COMPLETED && !Boolean.TRUE.equals(existing.getIsArchived())) {
            logger.info("Auto-archiving target date {} as it was marked as COMPLETED", id);
            existing.setIsArchived(true);
        }
        
        return targetDateRepository.save(existing);
    }
    
    /**
     * Updates target date status.
     * 
     * @param id Target date ID
     * @param status New status
     * @param updatedBy User updating the status
     * @return Updated target date
     * @throws IllegalArgumentException if target date not found
     */
    public TargetDate updateTargetDateStatus(Long id, TargetDateStatus status, User updatedBy) {
        logger.info("Updating target date status for ID: {} to {}", id, status);
        
        Optional<TargetDate> existingOpt = targetDateRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Target date not found with ID: " + id);
        }
        
        TargetDate existing = existingOpt.get();
        existing.setStatus(status);
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedAt(LocalDateTime.now());
        
        // Auto-archive target date when status is changed to COMPLETED
        if (status == TargetDateStatus.COMPLETED && !Boolean.TRUE.equals(existing.getIsArchived())) {
            logger.info("Auto-archiving target date {} as it was marked as COMPLETED", id);
            existing.setIsArchived(true);
        }
        
        return targetDateRepository.save(existing);
    }
    
    /**
     * Deletes a target date (soft delete).
     * 
     * @param id Target date ID to delete
     */
    public void deleteTargetDate(Long id) {
        logger.info("Deleting target date with ID: {}", id);
        
        Optional<TargetDate> existingOpt = targetDateRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Target date not found with ID: " + id);
        }
        
        targetDateRepository.deleteById(id);
    }
    
    /**
     * Unarchives a target date.
     * 
     * @param id Target date ID to unarchive
     */
    public void unarchiveTargetDate(Long id) {
        logger.info("Unarchiving target date with ID: {}", id);
        
        Optional<TargetDate> existingOpt = targetDateRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Target date not found with ID: " + id);
        }
        
        targetDateRepository.unarchiveById(id);
    }
    
    /**
     * Retrieves target dates by status.
     * 
     * @param status Target date status to filter by
     * @return List of target dates with specified status
     */
    public List<TargetDate> getTargetDatesByStatus(TargetDateStatus status) {
        logger.debug("Retrieving target dates with status: {}", status);
        return targetDateRepository.findByStatus(status);
    }
    
    /**
     * Retrieves upcoming target dates (due in the future).
     * 
     * @return List of upcoming target dates
     */
    public List<TargetDate> getUpcomingTargetDates() {
        logger.debug("Retrieving upcoming target dates");
        return targetDateRepository.findNonArchived().stream()
                .filter(targetDate -> targetDate.getTargetDate().isAfter(LocalDateTime.now()))
                .sorted((t1, t2) -> t1.getTargetDate().compareTo(t2.getTargetDate()))
                .toList();
    }
    
    /**
     * Retrieves overdue target dates (past due and not completed).
     * 
     * @return List of overdue target dates
     */
    public List<TargetDate> getOverdueTargetDates() {
        logger.debug("Retrieving overdue target dates");
        return targetDateRepository.findNonArchived().stream()
                .filter(targetDate -> targetDate.getTargetDate().isBefore(LocalDateTime.now()) &&
                        targetDate.getStatus() != TargetDateStatus.COMPLETED)
                .sorted((t1, t2) -> t1.getTargetDate().compareTo(t2.getTargetDate()))
                .toList();
    }
    
    /**
     * Retrieves target dates due within the next specified days.
     * 
     * @param days Number of days to look ahead
     * @return List of target dates due soon
     */
    public List<TargetDate> getTargetDatesDueSoon(int days) {
        logger.debug("Retrieving target dates due within {} days", days);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(days);
        
        return targetDateRepository.findNonArchived().stream()
                .filter(targetDate -> targetDate.getTargetDate().isAfter(now) &&
                        targetDate.getTargetDate().isBefore(futureDate) &&
                        targetDate.getStatus() != TargetDateStatus.COMPLETED)
                .sorted((t1, t2) -> t1.getTargetDate().compareTo(t2.getTargetDate()))
                .toList();
    }
    
    /**
     * Searches target dates by project name, task name, or driver name using local search.
     * 
     * @param searchTerm Search term
     * @param includeArchived Whether to include archived target dates
     * @return List of matching target dates sorted by target date ASC
     */
    public List<TargetDate> searchTargetDates(String searchTerm, boolean includeArchived) {
        logger.debug("Searching target dates with term: {}, includeArchived: {}", searchTerm, includeArchived);
        
        // Get all target dates first
        List<TargetDate> allTargetDates = includeArchived ? getAllTargetDates() : getNonArchivedTargetDates();
        
        // If no search term, return all target dates
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return allTargetDates;
        }
        
        // Perform local search
        String searchTermLower = searchTerm.trim().toLowerCase();
        
        return allTargetDates.stream()
                .filter(targetDate -> fuzzyMatch(targetDate, searchTermLower))
                .sorted((t1, t2) -> t1.getTargetDate().compareTo(t2.getTargetDate()))
                .toList();
    }
    
    /**
     * Performs fuzzy matching on a target date against a search term.
     * Searches in project name, task name, and driver name.
     * 
     * @param targetDate Target date to check
     * @param searchTermLower Search term in lowercase
     * @return true if target date matches search term
     */
    private boolean fuzzyMatch(TargetDate targetDate, String searchTermLower) {
        // Check project name
        if (targetDate.getProjectName() != null && 
            targetDate.getProjectName().toLowerCase().contains(searchTermLower)) {
            return true;
        }
        
        // Check task name
        if (targetDate.getTaskName() != null && 
            targetDate.getTaskName().toLowerCase().contains(searchTermLower)) {
            return true;
        }
        
        // Check driver user name
        if (targetDate.getDriverUser() != null && targetDate.getDriverUser().getFullName() != null &&
            targetDate.getDriverUser().getFullName().toLowerCase().contains(searchTermLower)) {
            return true;
        }
        
        // Check status display name
        if (targetDate.getStatus() != null && 
            targetDate.getStatus().getDisplayName().toLowerCase().contains(searchTermLower)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Validates target date data before saving.
     * 
     * @param projectName Project name
     * @param taskName Task name
     * @param targetDate Target date
     * @param driverUser Driver user
     * @param user User associated with the operation
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTargetDateData(String projectName, String taskName, LocalDateTime targetDate,
                                      User driverUser, User user) {
        if (projectName == null || projectName.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }
        
        if (projectName.length() > 200) {
            throw new IllegalArgumentException("Project name cannot exceed 200 characters");
        }
        
        if (taskName == null || taskName.trim().isEmpty()) {
            throw new IllegalArgumentException("Task name cannot be empty");
        }
        
        if (taskName.length() > 200) {
            throw new IllegalArgumentException("Task name cannot exceed 200 characters");
        }
        
        if (targetDate == null) {
            throw new IllegalArgumentException("Target date is required");
        }
        
        if (driverUser == null || driverUser.getId() == null) {
            throw new IllegalArgumentException("Valid driver user is required");
        }
        
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Valid user is required for target date operations");
        }
    }
}