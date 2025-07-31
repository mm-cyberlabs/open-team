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
     * Applies the futuristic theme styling to match the application.
     */
    private void applyFuturisticStyling() {
        // Apply CSS classes that match the application theme
        datePicker.getStyleClass().add("date-time-picker");
        hourSpinner.getStyleClass().add("date-time-picker");
        minuteSpinner.getStyleClass().add("date-time-picker");
        timeLabel.getStyleClass().add("date-time-label");
        
        // Set background and text colors to match the dark theme
        setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;");
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