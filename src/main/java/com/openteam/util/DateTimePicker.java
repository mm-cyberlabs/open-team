package com.openteam.util;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Custom DateTimePicker component that combines DatePicker and time selection.
 * Provides a user-friendly calendar interface with hour and minute selection.
 */
public class DateTimePicker extends VBox {
    
    private final DatePicker datePicker;
    private final Spinner<Integer> hourSpinner;
    private final Spinner<Integer> minuteSpinner;
    private final Label timeLabel;
    
    public DateTimePicker() {
        super();
        setSpacing(10);
        setPadding(new Insets(5));
        
        // Initialize components
        datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.setPrefWidth(200);
        
        // Apply dark theme styling programmatically
        setupDatePickerStyling();
        
        // Hour spinner (0-23)
        hourSpinner = new Spinner<>(0, 23, LocalTime.now().getHour());
        hourSpinner.setEditable(true);
        hourSpinner.setPrefWidth(70);
        
        // Minute spinner (0-59)
        minuteSpinner = new Spinner<>(0, 59, LocalTime.now().getMinute());
        minuteSpinner.setEditable(true);
        minuteSpinner.setPrefWidth(70);
        
        // Time label
        timeLabel = new Label("Time:");
        
        // Layout for time components
        HBox timeBox = new HBox(10);
        timeBox.getChildren().addAll(
            timeLabel,
            new Label("Hour:"), hourSpinner,
            new Label("Min:"), minuteSpinner
        );
        
        // Add all components to the VBox
        getChildren().addAll(datePicker, timeBox);
        
        // Apply styling
        applyFuturisticStyling();
    }
    
    /**
     * Constructor with initial date/time value.
     */
    public DateTimePicker(LocalDateTime initialDateTime) {
        this();
        setDateTime(initialDateTime);
    }
    
    /**
     * Gets the selected date and time as LocalDateTime.
     */
    public LocalDateTime getDateTime() {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            return null;
        }
        
        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        
        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }
    
    /**
     * Sets the date and time value.
     */
    public void setDateTime(LocalDateTime dateTime) {
        if (dateTime != null) {
            datePicker.setValue(dateTime.toLocalDate());
            hourSpinner.getValueFactory().setValue(dateTime.getHour());
            minuteSpinner.getValueFactory().setValue(dateTime.getMinute());
        }
    }
    
    /**
     * Gets the DatePicker component for additional customization.
     */
    public DatePicker getDatePicker() {
        return datePicker;
    }
    
    /**
     * Gets the hour spinner component.
     */
    public Spinner<Integer> getHourSpinner() {
        return hourSpinner;
    }
    
    /**
     * Gets the minute spinner component.
     */
    public Spinner<Integer> getMinuteSpinner() {
        return minuteSpinner;
    }
    
    /**
     * Sets up DatePicker popup styling to match dark theme.
     */
    private void setupDatePickerStyling() {
        // Listen for when the popup is shown and apply styling
        datePicker.setOnShowing(event -> {
            try {
                // Use Platform.runLater to ensure popup is fully loaded
                javafx.application.Platform.runLater(() -> {
                    try {
                        // Try to find and style the popup
                        var popup = findDatePickerPopup();
                        if (popup != null) {
                            applyPopupStyling(popup);
                        }
                    } catch (Exception e) {
                        System.err.println("Could not apply DatePicker popup styling: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("Error setting up DatePicker popup styling: " + e.getMessage());
            }
        });
    }
    
    /**
     * Finds the DatePicker popup window.
     */
    private javafx.stage.PopupWindow findDatePickerPopup() {
        try {
            // Get all popup windows
            var windows = javafx.stage.Window.getWindows();
            for (var window : windows) {
                if (window instanceof javafx.stage.PopupWindow) {
                    var popup = (javafx.stage.PopupWindow) window;
                    if (popup.isShowing()) {
                        return popup;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding DatePicker popup: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Applies dark theme styling to the popup.
     */
    private void applyPopupStyling(javafx.stage.PopupWindow popup) {
        try {
            if (popup.getScene() != null && popup.getScene().getRoot() != null) {
                var root = popup.getScene().getRoot();
                
                // Apply dark theme styles
                root.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;");
                
                // Apply styling to all children recursively
                styleNodeRecursively(root);
            }
        } catch (Exception e) {
            System.err.println("Error applying popup styling: " + e.getMessage());
        }
    }
    
    /**
     * Recursively applies dark theme styling to all nodes.
     */
    private void styleNodeRecursively(javafx.scene.Node node) {
        try {
            // Apply basic dark theme styling
            node.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;");
            
            // If it's a Parent, style all children
            if (node instanceof javafx.scene.Parent) {
                var parent = (javafx.scene.Parent) node;
                for (var child : parent.getChildrenUnmodifiable()) {
                    styleNodeRecursively(child);
                }
            }
        } catch (Exception e) {
            System.err.println("Error styling node recursively: " + e.getMessage());
        }
    }
    
    /**
     * Applies the futuristic theme styling to match the application.
     */
    private void applyFuturisticStyling() {
        // Apply CSS classes that match the application theme
        datePicker.getStyleClass().add("date-time-picker");
        hourSpinner.getStyleClass().add("date-time-picker");
        minuteSpinner.getStyleClass().add("date-time-picker");
        timeLabel.getStyleClass().add("date-time-label");
        
        // Set background and text colors to match the dark theme
        setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: rgba(255,255,255,0.44);");
    }
    
    /**
     * Validates that a date/time is selected.
     */
    public boolean isValid() {
        return datePicker.getValue() != null;
    }
    
    /**
     * Clears the selection.
     */
    public void clear() {
        datePicker.setValue(null);
        hourSpinner.getValueFactory().setValue(0);
        minuteSpinner.getValueFactory().setValue(0);
    }
    
    /**
     * Sets the prompt text for the date picker.
     */
    public void setPromptText(String promptText) {
        datePicker.setPromptText(promptText);
    }
    
    /**
     * Disables or enables the component.
     */
    public void setComponentDisabled(boolean disable) {
        setDisable(disable);
        datePicker.setDisable(disable);
        hourSpinner.setDisable(disable);
        minuteSpinner.setDisable(disable);
    }
}