package com.openteam.service;

import com.openteam.model.Announcement;
import com.openteam.model.Priority;
import com.openteam.model.User;
import com.openteam.repository.AnnouncementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing announcements.
 * Provides business logic for announcement operations.
 */
public class AnnouncementService {
    private static final Logger logger = LoggerFactory.getLogger(AnnouncementService.class);
    
    private final AnnouncementRepository announcementRepository;
    
    public AnnouncementService() {
        this.announcementRepository = new AnnouncementRepository();
    }
    
    /**
     * Retrieves all active announcements.
     * 
     * @return List of active announcements
     */
    public List<Announcement> getAllActiveAnnouncements() {
        logger.debug("Retrieving all active announcements");
        return announcementRepository.findAllActive();
    }
    
    /**
     * Retrieves an announcement by ID.
     * 
     * @param id Announcement ID
     * @return Optional containing the announcement if found
     */
    public Optional<Announcement> getAnnouncementById(Long id) {
        logger.debug("Retrieving announcement with ID: {}", id);
        return announcementRepository.findById(id);
    }
    
    /**
     * Creates a new announcement.
     * 
     * @param title Announcement title
     * @param content Announcement content
     * @param priority Announcement priority
     * @param createdBy User creating the announcement
     * @return Created announcement
     */
    public Announcement createAnnouncement(String title, String content, Priority priority, User createdBy) {
        logger.info("Creating new announcement: {}", title);
        
        validateAnnouncementData(title, content, createdBy);
        
        Announcement announcement = new Announcement(title, content, priority, createdBy);
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());
        
        return announcementRepository.save(announcement);
    }
    
    /**
     * Creates a new announcement with expiration date.
     * 
     * @param title Announcement title
     * @param content Announcement content
     * @param priority Announcement priority
     * @param expirationDate Expiration date (null for no expiration)
     * @param createdBy User creating the announcement
     * @return Created announcement
     */
    public Announcement createAnnouncement(String title, String content, Priority priority, 
                                         LocalDateTime expirationDate, User createdBy) {
        logger.info("Creating new announcement with expiration: {}", title);
        
        validateAnnouncementData(title, content, createdBy);
        
        Announcement announcement = new Announcement(title, content, priority, createdBy);
        announcement.setExpirationDate(expirationDate);
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());
        
        return announcementRepository.save(announcement);
    }
    
    /**
     * Updates an existing announcement.
     * 
     * @param id Announcement ID
     * @param title New title
     * @param content New content
     * @param priority New priority
     * @param updatedBy User updating the announcement
     * @return Updated announcement
     * @throws IllegalArgumentException if announcement not found
     */
    public Announcement updateAnnouncement(Long id, String title, String content, 
                                         Priority priority, User updatedBy) {
        logger.info("Updating announcement with ID: {}", id);
        
        validateAnnouncementData(title, content, updatedBy);
        
        Optional<Announcement> existingOpt = announcementRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Announcement not found with ID: " + id);
        }
        
        Announcement existing = existingOpt.get();
        existing.setTitle(title);
        existing.setContent(content);
        existing.setPriority(priority);
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedAt(LocalDateTime.now());
        
        return announcementRepository.save(existing);
    }
    
    /**
     * Updates an existing announcement with expiration date.
     * 
     * @param id Announcement ID
     * @param title New title
     * @param content New content
     * @param priority New priority
     * @param expirationDate New expiration date (null for no expiration)
     * @param updatedBy User updating the announcement
     * @return Updated announcement
     * @throws IllegalArgumentException if announcement not found
     */
    public Announcement updateAnnouncement(Long id, String title, String content, 
                                         Priority priority, LocalDateTime expirationDate, User updatedBy) {
        logger.info("Updating announcement with expiration, ID: {}", id);
        
        validateAnnouncementData(title, content, updatedBy);
        
        Optional<Announcement> existingOpt = announcementRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Announcement not found with ID: " + id);
        }
        
        Announcement existing = existingOpt.get();
        existing.setTitle(title);
        existing.setContent(content);
        existing.setPriority(priority);
        existing.setExpirationDate(expirationDate);
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedAt(LocalDateTime.now());
        
        return announcementRepository.save(existing);
    }
    
    /**
     * Deletes an announcement (soft delete).
     * 
     * @param id Announcement ID to delete
     */
    public void deleteAnnouncement(Long id) {
        logger.info("Deleting announcement with ID: {}", id);
        
        Optional<Announcement> existingOpt = announcementRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Announcement not found with ID: " + id);
        }
        
        announcementRepository.deleteById(id);
    }
    
    /**
     * Retrieves announcements by priority.
     * 
     * @param priority Priority to filter by
     * @return List of announcements with specified priority
     */
    public List<Announcement> getAnnouncementsByPriority(Priority priority) {
        logger.debug("Retrieving announcements with priority: {}", priority);
        return announcementRepository.findByPriority(priority);
    }
    
    /**
     * Retrieves high priority announcements (HIGH and URGENT).
     * 
     * @return List of high priority announcements
     */
    public List<Announcement> getHighPriorityAnnouncements() {
        logger.debug("Retrieving high priority announcements");
        List<Announcement> highPriority = announcementRepository.findByPriority(Priority.HIGH);
        List<Announcement> urgent = announcementRepository.findByPriority(Priority.URGENT);
        
        highPriority.addAll(urgent);
        return highPriority.stream()
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .toList();
    }
    
    /**
     * Archives all expired announcements.
     * This method should be called periodically to automatically archive announcements that have passed their expiration date.
     * 
     * @return Number of announcements that were archived
     */
    public int archiveExpiredAnnouncements() {
        logger.info("Archiving expired announcements");
        return announcementRepository.archiveExpiredAnnouncements();
    }
    
    /**
     * Validates announcement data before saving.
     * 
     * @param title Announcement title
     * @param content Announcement content
     * @param user User associated with the announcement
     * @throws IllegalArgumentException if validation fails
     */
    private void validateAnnouncementData(String title, String content, User user) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Announcement title cannot be empty");
        }
        
        if (title.length() > 200) {
            throw new IllegalArgumentException("Announcement title cannot exceed 200 characters");
        }
        
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Announcement content cannot be empty");
        }
        
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Valid user is required for announcement operations");
        }
    }
}