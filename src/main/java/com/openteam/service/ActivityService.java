package com.openteam.service;

import com.openteam.model.Activity;
import com.openteam.model.ActivityType;
import com.openteam.model.User;
import com.openteam.repository.ActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing team activities.
 * Provides business logic for activity operations.
 */
public class ActivityService {
    private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);
    
    private final ActivityRepository activityRepository;
    
    public ActivityService() {
        this.activityRepository = new ActivityRepository();
    }
    
    /**
     * Retrieves all active activities.
     * 
     * @return List of active activities ordered by scheduled date
     */
    public List<Activity> getAllActiveActivities() {
        logger.debug("Retrieving all active activities");
        return activityRepository.findAllActive();
    }
    
    /**
     * Retrieves an activity by ID.
     * 
     * @param id Activity ID
     * @return Optional containing the activity if found
     */
    public Optional<Activity> getActivityById(Long id) {
        logger.debug("Retrieving activity with ID: {}", id);
        return activityRepository.findById(id);
    }
    
    /**
     * Creates a new activity.
     * 
     * @param title Activity title
     * @param description Activity description
     * @param activityType Type of activity
     * @param scheduledDate When the activity is scheduled
     * @param location Where the activity takes place
     * @param createdBy User creating the activity
     * @return Created activity
     */
    public Activity createActivity(String title, String description, ActivityType activityType,
                                 LocalDateTime scheduledDate, String location, User createdBy) {
        logger.info("Creating new activity: {}", title);
        
        validateActivityData(title, createdBy);
        
        Activity activity = new Activity(title, description, activityType, 
                                       scheduledDate, location, createdBy);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        
        return activityRepository.save(activity);
    }
    
    /**
     * Updates an existing activity.
     * 
     * @param id Activity ID
     * @param title New title
     * @param description New description
     * @param activityType New activity type
     * @param scheduledDate New scheduled date
     * @param location New location
     * @param updatedBy User updating the activity
     * @return Updated activity
     * @throws IllegalArgumentException if activity not found
     */
    public Activity updateActivity(Long id, String title, String description, 
                                 ActivityType activityType, LocalDateTime scheduledDate, 
                                 String location, User updatedBy) {
        logger.info("Updating activity with ID: {}", id);
        
        validateActivityData(title, updatedBy);
        
        Optional<Activity> existingOpt = activityRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Activity not found with ID: " + id);
        }
        
        Activity existing = existingOpt.get();
        existing.setTitle(title);
        existing.setDescription(description);
        existing.setActivityType(activityType);
        existing.setScheduledDate(scheduledDate);
        existing.setLocation(location);
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedAt(LocalDateTime.now());
        
        return activityRepository.save(existing);
    }
    
    /**
     * Deletes an activity (soft delete).
     * 
     * @param id Activity ID to delete
     */
    public void deleteActivity(Long id) {
        logger.info("Deleting activity with ID: {}", id);
        
        Optional<Activity> existingOpt = activityRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Activity not found with ID: " + id);
        }
        
        activityRepository.deleteById(id);
    }
    
    /**
     * Retrieves activities by type.
     * 
     * @param activityType Activity type to filter by
     * @return List of activities with specified type
     */
    public List<Activity> getActivitiesByType(ActivityType activityType) {
        logger.debug("Retrieving activities with type: {}", activityType);
        return activityRepository.findByType(activityType);
    }
    
    /**
     * Retrieves upcoming activities (scheduled for future dates).
     * 
     * @return List of upcoming activities
     */
    public List<Activity> getUpcomingActivities() {
        logger.debug("Retrieving upcoming activities");
        return activityRepository.findAllActive().stream()
                .filter(activity -> activity.getScheduledDate() != null && 
                        activity.getScheduledDate().isAfter(LocalDateTime.now()))
                .sorted((a1, a2) -> a1.getScheduledDate().compareTo(a2.getScheduledDate()))
                .toList();
    }
    
    /**
     * Retrieves activities scheduled for today.
     * 
     * @return List of today's activities
     */
    public List<Activity> getTodaysActivities() {
        logger.debug("Retrieving today's activities");
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        
        return activityRepository.findAllActive().stream()
                .filter(activity -> activity.getScheduledDate() != null &&
                        activity.getScheduledDate().isAfter(startOfDay) &&
                        activity.getScheduledDate().isBefore(endOfDay))
                .sorted((a1, a2) -> a1.getScheduledDate().compareTo(a2.getScheduledDate()))
                .toList();
    }
    
    /**
     * Validates activity data before saving.
     * 
     * @param title Activity title
     * @param user User associated with the activity
     * @throws IllegalArgumentException if validation fails
     */
    private void validateActivityData(String title, User user) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Activity title cannot be empty");
        }
        
        if (title.length() > 200) {
            throw new IllegalArgumentException("Activity title cannot exceed 200 characters");
        }
        
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Valid user is required for activity operations");
        }
    }
}