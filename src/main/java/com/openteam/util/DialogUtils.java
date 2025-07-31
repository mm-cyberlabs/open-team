package com.openteam.util;

import com.openteam.model.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Utility class for creating dialog forms for CRUD operations.
 */
public class DialogUtils {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    /**
     * Applies the application theme to dialog windows.
     */
    private static void applyDialogTheme(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
            DialogUtils.class.getResource("/css/futuristic-theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");
    }
    
    /**
     * Shows dialog for creating/editing announcements.
     */
    public static Optional<Announcement> showAnnouncementDialog(Announcement existing, User currentUser) {
        Dialog<Announcement> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Create Announcement" : "Edit Announcement");
        dialog.setHeaderText(existing == null ? "Enter announcement details:" : "Update announcement details:");
        
        // Apply theme to dialog
        applyDialogTheme(dialog);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Announcement title");
        titleField.setPrefWidth(300);
        
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Announcement content");
        contentArea.setPrefRowCount(5);
        contentArea.setPrefWidth(300);
        contentArea.setWrapText(true);
        
        ComboBox<Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(Priority.values());
        priorityCombo.setValue(Priority.NORMAL);
        priorityCombo.setPrefWidth(150);
        
        // Populate fields if editing
        if (existing != null) {
            titleField.setText(existing.getTitle());
            contentArea.setText(existing.getContent());
            priorityCombo.setValue(existing.getPriority());
        }
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Priority:"), 0, 1);
        grid.add(priorityCombo, 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(contentArea, 1, 2);
        
        // Enable/disable save button based on input
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() || contentArea.getText().trim().isEmpty());
        });
        
        contentArea.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(titleField.getText().trim().isEmpty() || newValue.trim().isEmpty());
        });
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on title field
        titleField.requestFocus();
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (existing == null) {
                    // Create new
                    return new Announcement(
                        titleField.getText().trim(),
                        contentArea.getText().trim(),
                        priorityCombo.getValue(),
                        currentUser
                    );
                } else {
                    // Update existing
                    existing.setTitle(titleField.getText().trim());
                    existing.setContent(contentArea.getText().trim());
                    existing.setPriority(priorityCombo.getValue());
                    existing.setUpdatedBy(currentUser);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return existing;
                }
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    /**
     * Shows dialog for creating/editing activities.
     */
    public static Optional<Activity> showActivityDialog(Activity existing, User currentUser) {
        Dialog<Activity> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Create Activity" : "Edit Activity");
        dialog.setHeaderText(existing == null ? "Enter activity details:" : "Update activity details:");
        
        // Apply theme to dialog
        applyDialogTheme(dialog);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Activity title");
        titleField.setPrefWidth(300);
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Activity description");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setPrefWidth(300);
        descriptionArea.setWrapText(true);
        
        ComboBox<ActivityType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(ActivityType.values());
        typeCombo.setValue(ActivityType.GENERAL);
        typeCombo.setPrefWidth(150);
        
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setPromptText("Select date and time");
        
        TextField locationField = new TextField();
        locationField.setPromptText("Location (optional)");
        locationField.setPrefWidth(300);
        
        // Populate fields if editing
        if (existing != null) {
            titleField.setText(existing.getTitle());
            descriptionArea.setText(existing.getDescription());
            typeCombo.setValue(existing.getActivityType());
            if (existing.getScheduledDate() != null) {
                dateTimePicker.setDateTime(existing.getScheduledDate());
            }
            locationField.setText(existing.getLocation());
        }
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Scheduled Date:"), 0, 2);
        grid.add(dateTimePicker, 1, 2);
        grid.add(new Label("Location:"), 0, 3);
        grid.add(locationField, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descriptionArea, 1, 4);
        
        // Enable/disable save button based on input
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on title field
        titleField.requestFocus();
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    LocalDateTime scheduledDate = dateTimePicker.getDateTime();
                    
                    if (existing == null) {
                        // Create new
                        return new Activity(
                            titleField.getText().trim(),
                            descriptionArea.getText().trim(),
                            typeCombo.getValue(),
                            scheduledDate,
                            locationField.getText().trim().isEmpty() ? null : locationField.getText().trim(),
                            currentUser
                        );
                    } else {
                        // Update existing
                        existing.setTitle(titleField.getText().trim());
                        existing.setDescription(descriptionArea.getText().trim());
                        existing.setActivityType(typeCombo.getValue());
                        existing.setScheduledDate(scheduledDate);
                        existing.setLocation(locationField.getText().trim().isEmpty() ? null : locationField.getText().trim());
                        existing.setUpdatedBy(currentUser);
                        existing.setUpdatedAt(LocalDateTime.now());
                        return existing;
                    }
                } catch (Exception e) {
                    UIUtils.showErrorDialog("Invalid date", "Please select a valid date and time");
                    return null;
                }
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    /**
     * Shows dialog for creating/editing deployments.
     */
    public static Optional<Deployment> showDeploymentDialog(Deployment existing, User currentUser) {
        Dialog<Deployment> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Create Deployment" : "Edit Deployment");
        dialog.setHeaderText(existing == null ? "Enter deployment details:" : "Update deployment details:");
        
        // Apply theme to dialog
        applyDialogTheme(dialog);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField releaseNameField = new TextField();
        releaseNameField.setPromptText("Release name");
        releaseNameField.setPrefWidth(200);
        
        TextField versionField = new TextField();
        versionField.setPromptText("Version (e.g., v1.2.0)");
        versionField.setPrefWidth(150);
        
        DateTimePicker deploymentDatePicker = new DateTimePicker();
        deploymentDatePicker.setPromptText("Select deployment date and time");
        
        ComboBox<Environment> environmentCombo = new ComboBox<>();
        environmentCombo.getItems().addAll(Environment.values());
        environmentCombo.setValue(Environment.PRODUCTION);
        environmentCombo.setPrefWidth(150);
        
        ComboBox<DeploymentStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(DeploymentStatus.values());
        statusCombo.setValue(DeploymentStatus.PLANNED);
        statusCombo.setPrefWidth(150);
        
        TextArea releaseNotesArea = new TextArea();
        releaseNotesArea.setPromptText("Release notes");
        releaseNotesArea.setPrefRowCount(4);
        releaseNotesArea.setPrefWidth(300);
        releaseNotesArea.setWrapText(true);
        
        // Populate fields if editing
        if (existing != null) {
            releaseNameField.setText(existing.getReleaseName());
            versionField.setText(existing.getVersion());
            if (existing.getDeploymentDateTime() != null) {
                deploymentDatePicker.setDateTime(existing.getDeploymentDateTime());
            }
            environmentCombo.setValue(existing.getEnvironment());
            statusCombo.setValue(existing.getStatus());
            releaseNotesArea.setText(existing.getReleaseNotes());
        }
        
        grid.add(new Label("Release Name:"), 0, 0);
        grid.add(releaseNameField, 1, 0);
        grid.add(new Label("Version:"), 0, 1);
        grid.add(versionField, 1, 1);
        grid.add(new Label("Deployment Date:"), 0, 2);
        grid.add(deploymentDatePicker, 1, 2);
        grid.add(new Label("Environment:"), 0, 3);
        grid.add(environmentCombo, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusCombo, 1, 4);
        grid.add(new Label("Release Notes:"), 0, 5);
        grid.add(releaseNotesArea, 1, 5);
        
        // Enable/disable save button based on input
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        Runnable validateInput = () -> {
            boolean valid = !releaseNameField.getText().trim().isEmpty() &&
                           !versionField.getText().trim().isEmpty() &&
                           deploymentDatePicker.isValid();
            saveButton.setDisable(!valid);
        };
        
        releaseNameField.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        versionField.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        deploymentDatePicker.getDatePicker().valueProperty().addListener((obs, old, newVal) -> validateInput.run());
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on release name field
        releaseNameField.requestFocus();
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    LocalDateTime deploymentDateTime = deploymentDatePicker.getDateTime();
                    if (deploymentDateTime == null) {
                        UIUtils.showErrorDialog("Missing date", "Please select a deployment date and time");
                        return null;
                    }
                    
                    if (existing == null) {
                        // Create new
                        return new Deployment(
                            releaseNameField.getText().trim(),
                            versionField.getText().trim(),
                            deploymentDateTime,
                            currentUser, // driver user
                            releaseNotesArea.getText().trim().isEmpty() ? null : releaseNotesArea.getText().trim(),
                            environmentCombo.getValue(),
                            statusCombo.getValue(),
                            currentUser // created by
                        );
                    } else {
                        // Update existing
                        existing.setReleaseName(releaseNameField.getText().trim());
                        existing.setVersion(versionField.getText().trim());
                        existing.setDeploymentDateTime(deploymentDateTime);
                        existing.setReleaseNotes(releaseNotesArea.getText().trim().isEmpty() ? null : releaseNotesArea.getText().trim());
                        existing.setEnvironment(environmentCombo.getValue());
                        existing.setStatus(statusCombo.getValue());
                        existing.setUpdatedBy(currentUser);
                        existing.setUpdatedAt(LocalDateTime.now());
                        return existing;
                    }
                } catch (Exception e) {
                    UIUtils.showErrorDialog("Invalid date", "Please select a valid deployment date and time");
                    return null;
                }
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    /**
     * Shows confirmation dialog for deleting records.
     */
    public static boolean showDeleteConfirmation(String itemType, String itemName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete " + itemType + "?");
        alert.setContentText("Are you sure you want to delete \"" + itemName + "\"?\n\n" +
                           "This action will archive the record and it can be restored later.");
        
        // Apply theme to alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            DialogUtils.class.getResource("/css/futuristic-theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}