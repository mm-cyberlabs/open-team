package com.openteam.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Utility class for JavaFX UI operations.
 * Provides common UI helper methods and dialogs.
 */
public class UIUtils {
    private static final Logger logger = LoggerFactory.getLogger(UIUtils.class);
    
    private UIUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Shows an information dialog.
     * 
     * @param title Dialog title
     * @param header Dialog header text
     * @param content Dialog content text
     */
    public static void showInfoDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows an error dialog.
     * 
     * @param title Dialog title
     * @param header Dialog header text
     * @param content Dialog content text
     */
    public static void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
        
        logger.error("Error dialog shown - Title: {}, Header: {}, Content: {}", 
                    title, header, content);
    }
    
    /**
     * Shows a warning dialog.
     * 
     * @param title Dialog title
     * @param header Dialog header text
     * @param content Dialog content text
     */
    public static void showWarningDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
        
        logger.warn("Warning dialog shown - Title: {}, Header: {}, Content: {}", 
                   title, header, content);
    }
    
    /**
     * Shows a confirmation dialog.
     * 
     * @param title Dialog title
     * @param header Dialog header text
     * @param content Dialog content text
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Shows a database connection error dialog.
     * 
     * @param exception The exception that occurred
     */
    public static void showDatabaseErrorDialog(Exception exception) {
        showErrorDialog(
            "Database Connection Error",
            "Failed to connect to the database",
            "Please check your database configuration in ~/.openteam/config.yml\n\n" +
            "Error: " + exception.getMessage()
        );
    }
    
    /**
     * Shows a configuration error dialog.
     * 
     * @param exception The exception that occurred
     */
    public static void showConfigurationErrorDialog(Exception exception) {
        showErrorDialog(
            "Configuration Error",
            "Failed to load application configuration",
            "Please check your configuration file at ~/.openteam/config.yml\n\n" +
            "Error: " + exception.getMessage()
        );
    }
    
    /**
     * Creates a styled label with CSS classes.
     * 
     * @param text Label text
     * @param styleClasses CSS style classes to apply
     * @return Configured Label
     */
    public static Label createStyledLabel(String text, String... styleClasses) {
        Label label = new Label(text);
        if (styleClasses != null && styleClasses.length > 0) {
            label.getStyleClass().addAll(styleClasses);
        }
        return label;
    }
    
    /**
     * Sets the application icon for a stage.
     * 
     * @param stage Stage to set icon for
     * @param iconPath Path to the icon resource
     */
    public static void setApplicationIcon(Stage stage, String iconPath) {
        try {
            var iconStream = UIUtils.class.getResourceAsStream(iconPath);
            if (iconStream != null) {
                javafx.scene.image.Image icon = new javafx.scene.image.Image(iconStream);
                stage.getIcons().add(icon);
            } else {
                logger.warn("Could not load application icon from: {}", iconPath);
            }
        } catch (Exception e) {
            logger.error("Error setting application icon", e);
        }
    }
    
    /**
     * Truncates text to a maximum length and adds ellipsis if needed.
     * 
     * @param text Text to truncate
     * @param maxLength Maximum length
     * @return Truncated text with ellipsis if necessary
     */
    public static String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Capitalizes the first letter of each word in a string.
     * 
     * @param text Text to capitalize
     * @return Capitalized text
     */
    public static String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        
        return result.toString();
    }
}