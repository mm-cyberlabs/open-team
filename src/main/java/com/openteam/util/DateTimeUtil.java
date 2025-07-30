package com.openteam.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date and time operations.
 * Provides common date/time formatting and calculation methods.
 */
public class DateTimeUtil {
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private static final DateTimeFormatter TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private static final DateTimeFormatter DISPLAY_FORMATTER = 
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    private DateTimeUtil() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Formats a LocalDateTime using the default format (yyyy-MM-dd HH:mm:ss).
     * 
     * @param dateTime LocalDateTime to format
     * @return Formatted date string, or empty string if null
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }
    
    /**
     * Formats a LocalDateTime using the display format (MMM dd, yyyy HH:mm).
     * 
     * @param dateTime LocalDateTime to format
     * @return Formatted date string for display
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DISPLAY_FORMATTER);
    }
    
    /**
     * Formats only the date part of a LocalDateTime.
     * 
     * @param dateTime LocalDateTime to format
     * @return Formatted date string (yyyy-MM-dd)
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_FORMATTER);
    }
    
    /**
     * Formats only the time part of a LocalDateTime.
     * 
     * @param dateTime LocalDateTime to format
     * @return Formatted time string (HH:mm:ss)
     */
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(TIME_FORMATTER);
    }
    
    /**
     * Returns a human-readable relative time string.
     * 
     * @param dateTime LocalDateTime to compare
     * @return Relative time string (e.g., "2 hours ago", "in 30 minutes")
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        if (dateTime.isBefore(now)) {
            return getTimeAgo(dateTime, now);
        } else {
            return getTimeFromNow(dateTime, now);
        }
    }
    
    /**
     * Checks if a LocalDateTime is today.
     * 
     * @param dateTime LocalDateTime to check
     * @return true if the date is today
     */
    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }
    
    /**
     * Checks if a LocalDateTime is in the future.
     * 
     * @param dateTime LocalDateTime to check
     * @return true if the date is in the future
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(LocalDateTime.now());
    }
    
    /**
     * Checks if a LocalDateTime is in the past.
     * 
     * @param dateTime LocalDateTime to check
     * @return true if the date is in the past
     */
    public static boolean isPast(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isBefore(LocalDateTime.now());
    }
    
    private static String getTimeAgo(LocalDateTime dateTime, LocalDateTime now) {
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes < 1) {
            return "just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        }
        
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        }
        
        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        }
        
        long weeks = days / 7;
        if (weeks < 4) {
            return weeks + " week" + (weeks == 1 ? "" : "s") + " ago";
        }
        
        long months = ChronoUnit.MONTHS.between(dateTime, now);
        if (months < 12) {
            return months + " month" + (months == 1 ? "" : "s") + " ago";
        }
        
        long years = ChronoUnit.YEARS.between(dateTime, now);
        return years + " year" + (years == 1 ? "" : "s") + " ago";
    }
    
    private static String getTimeFromNow(LocalDateTime dateTime, LocalDateTime now) {
        long minutes = ChronoUnit.MINUTES.between(now, dateTime);
        if (minutes < 1) {
            return "in a moment";
        } else if (minutes < 60) {
            return "in " + minutes + " minute" + (minutes == 1 ? "" : "s");
        }
        
        long hours = ChronoUnit.HOURS.between(now, dateTime);
        if (hours < 24) {
            return "in " + hours + " hour" + (hours == 1 ? "" : "s");
        }
        
        long days = ChronoUnit.DAYS.between(now, dateTime);
        if (days < 7) {
            return "in " + days + " day" + (days == 1 ? "" : "s");
        }
        
        long weeks = days / 7;
        if (weeks < 4) {
            return "in " + weeks + " week" + (weeks == 1 ? "" : "s");
        }
        
        long months = ChronoUnit.MONTHS.between(now, dateTime);
        if (months < 12) {
            return "in " + months + " month" + (months == 1 ? "" : "s");
        }
        
        long years = ChronoUnit.YEARS.between(now, dateTime);
        return "in " + years + " year" + (years == 1 ? "" : "s");
    }
}