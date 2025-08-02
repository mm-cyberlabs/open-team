package com.openteam.util;

import com.openteam.model.*;
import com.openteam.service.DeploymentService;
import io.github.palexdev.materialfx.controls.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        
        DatePicker expirationDatePicker = new DatePicker();
        expirationDatePicker.setPromptText("Optional expiration date");
        expirationDatePicker.setPrefWidth(150);
        
        CheckBox neverExpiresCheckBox = new CheckBox("Never expires");
        neverExpiresCheckBox.setSelected(true);
        
        // When "Never expires" is checked, disable the date picker
        neverExpiresCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            expirationDatePicker.setDisable(newVal);
            if (newVal) {
                expirationDatePicker.setValue(null);
            }
        });
        
        // Initially disable the date picker since "Never expires" is checked
        expirationDatePicker.setDisable(true);
        
        // Populate fields if editing
        if (existing != null) {
            titleField.setText(existing.getTitle());
            contentArea.setText(existing.getContent());
            priorityCombo.setValue(existing.getPriority());
            
            // Handle expiration date
            if (existing.getExpirationDate() != null) {
                expirationDatePicker.setValue(existing.getExpirationDate().toLocalDate());
                neverExpiresCheckBox.setSelected(false);
                expirationDatePicker.setDisable(false);
            } else {
                neverExpiresCheckBox.setSelected(true);
                expirationDatePicker.setDisable(true);
            }
        }
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Priority:"), 0, 1);
        grid.add(priorityCombo, 1, 1);
        grid.add(new Label("Expires:"), 0, 2);
        grid.add(neverExpiresCheckBox, 1, 2);
        grid.add(new Label("Date:"), 0, 3);
        grid.add(expirationDatePicker, 1, 3);
        grid.add(new Label("Content:"), 0, 4);
        grid.add(contentArea, 1, 4);
        
        // Enable/disable save button based on input
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(existing == null); // For new records, start disabled; for editing, start enabled
        
        Runnable validateInput = () -> {
            boolean valid = !titleField.getText().trim().isEmpty() && !contentArea.getText().trim().isEmpty();
            saveButton.setDisable(!valid);
        };
        
        // Add listeners to all fields
        titleField.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        contentArea.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        priorityCombo.valueProperty().addListener((obs, old, newVal) -> validateInput.run());
        
        // Initial validation call
        validateInput.run();
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on title field
        titleField.requestFocus();
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Calculate expiration date
                LocalDateTime expirationDateTime = null;
                if (!neverExpiresCheckBox.isSelected() && expirationDatePicker.getValue() != null) {
                    // Set expiration to end of selected day (23:59:59)
                    expirationDateTime = expirationDatePicker.getValue().atTime(LocalTime.MAX);
                }
                
                if (existing == null) {
                    // Create new
                    Announcement newAnnouncement = new Announcement(
                        titleField.getText().trim(),
                        contentArea.getText().trim(),
                        priorityCombo.getValue(),
                        currentUser
                    );
                    newAnnouncement.setExpirationDate(expirationDateTime);
                    return newAnnouncement;
                } else {
                    // Update existing
                    existing.setTitle(titleField.getText().trim());
                    existing.setContent(contentArea.getText().trim());
                    existing.setPriority(priorityCombo.getValue());
                    existing.setExpirationDate(expirationDateTime);
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
        saveButton.setDisable(existing == null); // For new records, start disabled; for editing, start enabled
        
        Runnable validateInput = () -> {
            boolean valid = !titleField.getText().trim().isEmpty();
            saveButton.setDisable(!valid);
        };
        
        // Add listeners to all fields
        titleField.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        typeCombo.valueProperty().addListener((obs, old, newVal) -> validateInput.run());
        locationField.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        descriptionArea.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        dateTimePicker.getDatePicker().valueProperty().addListener((obs, old, newVal) -> validateInput.run());
        
        // Initial validation call
        validateInput.run();
        
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
        
        TextField ticketNumberField = new TextField();
        ticketNumberField.setPromptText("Ticket number (optional)");
        ticketNumberField.setPrefWidth(200);
        
        TextField documentationUrlField = new TextField();
        documentationUrlField.setPromptText("Documentation URL (optional)");
        documentationUrlField.setPrefWidth(300);
        
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
            ticketNumberField.setText(existing.getTicketNumber() != null ? existing.getTicketNumber() : "");
            documentationUrlField.setText(existing.getDocumentationUrl() != null ? existing.getDocumentationUrl() : "");
            releaseNotesArea.setText(existing.getReleaseNotes());
        }
        
        grid.add(new Label("Release Name:"), 0, 0);
        grid.add(releaseNameField, 1, 0);
        grid.add(new Label("Version:"), 0, 1);
        grid.add(versionField, 1, 1);
        grid.add(new Label("Ticket Number:"), 0, 2);
        grid.add(ticketNumberField, 1, 2);
        grid.add(new Label("Documentation URL:"), 0, 3);
        grid.add(documentationUrlField, 1, 3);
        grid.add(new Label("Deployment Date:"), 0, 4);
        grid.add(deploymentDatePicker, 1, 4);
        grid.add(new Label("Environment:"), 0, 5);
        grid.add(environmentCombo, 1, 5);
        grid.add(new Label("Status:"), 0, 6);
        grid.add(statusCombo, 1, 6);
        grid.add(new Label("Release Notes:"), 0, 7);
        grid.add(releaseNotesArea, 1, 7);
        
        // Enable/disable save button based on input
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(existing == null); // For new records, start disabled; for editing, start enabled
        
        Runnable validateInput = () -> {
            boolean valid = !releaseNameField.getText().trim().isEmpty() &&
                           !versionField.getText().trim().isEmpty() &&
                           deploymentDatePicker.isValid();
            saveButton.setDisable(!valid);
        };
        
        // Add listeners to all fields that should trigger validation
        releaseNameField.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        versionField.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        ticketNumberField.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        documentationUrlField.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        releaseNotesArea.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        environmentCombo.valueProperty().addListener((obs, old, newVal) -> validateInput.run());
        statusCombo.valueProperty().addListener((obs, old, newVal) -> validateInput.run());
        deploymentDatePicker.getDatePicker().valueProperty().addListener((obs, old, newVal) -> validateInput.run());
        
        // Initial validation call
        validateInput.run();
        
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
                            ticketNumberField.getText().trim().isEmpty() ? null : ticketNumberField.getText().trim(),
                            documentationUrlField.getText().trim().isEmpty() ? null : documentationUrlField.getText().trim(),
                            currentUser // created by
                        );
                    } else {
                        // Update existing
                        existing.setReleaseName(releaseNameField.getText().trim());
                        existing.setVersion(versionField.getText().trim());
                        existing.setDeploymentDateTime(deploymentDateTime);
                        existing.setReleaseNotes(releaseNotesArea.getText().trim().isEmpty() ? null : releaseNotesArea.getText().trim());
                        existing.setTicketNumber(ticketNumberField.getText().trim().isEmpty() ? null : ticketNumberField.getText().trim());
                        existing.setDocumentationUrl(documentationUrlField.getText().trim().isEmpty() ? null : documentationUrlField.getText().trim());
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
     * Shows dialog for creating/editing target dates.
     */
    public static Optional<TargetDate> showTargetDateDialog(TargetDate existing, User currentUser) {
        Dialog<TargetDate> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Create Target Date" : "Edit Target Date");
        dialog.setHeaderText(existing == null ? "Enter target date details:" : "Update target date details:");
        
        // Apply theme to dialog
        applyDialogTheme(dialog);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField projectNameField = new TextField();
        projectNameField.setPromptText("Project name");
        projectNameField.setPrefWidth(300);
        
        TextField taskNameField = new TextField();
        taskNameField.setPromptText("Task/milestone name");
        taskNameField.setPrefWidth(300);
        
        DateTimePicker targetDatePicker = new DateTimePicker();
        targetDatePicker.setPromptText("Select target/due date and time");
        
        ComboBox<TargetDateStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(TargetDateStatus.values());
        statusCombo.setValue(TargetDateStatus.PENDING);
        statusCombo.setPrefWidth(150);
        
        // For simplicity, we'll use the current user as driver. In a real app, you'd have a user selection
        TextField driverField = new TextField();
        driverField.setPromptText("Driver (person responsible)");
        driverField.setPrefWidth(200);
        driverField.setText(currentUser.getFullName());
        driverField.setEditable(false); // For now, just use current user
        
        TextField documentationUrlField = new TextField();
        documentationUrlField.setPromptText("Documentation URL (optional)");
        documentationUrlField.setPrefWidth(400);
        
        // Populate fields if editing
        if (existing != null) {
            projectNameField.setText(existing.getProjectName());
            taskNameField.setText(existing.getTaskName());
            if (existing.getTargetDate() != null) {
                targetDatePicker.setDateTime(existing.getTargetDate());
            }
            statusCombo.setValue(existing.getStatus());
            if (existing.getDriverUser() != null) {
                driverField.setText(existing.getDriverUser().getFullName());
            }
            documentationUrlField.setText(existing.getDocumentationUrl() != null ? existing.getDocumentationUrl() : "");
        }
        
        grid.add(new Label("Project Name:"), 0, 0);
        grid.add(projectNameField, 1, 0);
        grid.add(new Label("Task/Milestone:"), 0, 1);
        grid.add(taskNameField, 1, 1);
        grid.add(new Label("Target Date:"), 0, 2);
        grid.add(targetDatePicker, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusCombo, 1, 3);
        grid.add(new Label("Driver:"), 0, 4);
        grid.add(driverField, 1, 4);
        grid.add(new Label("Documentation URL:"), 0, 5);
        grid.add(documentationUrlField, 1, 5);
        
        // Enable/disable save button based on input
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(existing == null); // For new records, start disabled; for editing, start enabled
        
        Runnable validateInput = () -> {
            boolean valid = !projectNameField.getText().trim().isEmpty() &&
                           !taskNameField.getText().trim().isEmpty() &&
                           targetDatePicker.isValid();
            saveButton.setDisable(!valid);
        };
        
        // Add listeners to all fields that should trigger validation
        projectNameField.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        taskNameField.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        documentationUrlField.textProperty().addListener((obs, old, newVal) -> validateInput.run());
        statusCombo.valueProperty().addListener((obs, old, newVal) -> validateInput.run());
        targetDatePicker.getDatePicker().valueProperty().addListener((obs, old, newVal) -> validateInput.run());
        
        // Initial validation call
        validateInput.run();
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on project name field
        projectNameField.requestFocus();
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    LocalDateTime targetDateTime = targetDatePicker.getDateTime();
                    if (targetDateTime == null) {
                        UIUtils.showErrorDialog("Missing date", "Please select a target date and time");
                        return null;
                    }
                    
                    if (existing == null) {
                        // Create new
                        return new TargetDate(
                            projectNameField.getText().trim(),
                            taskNameField.getText().trim(),
                            targetDateTime,
                            currentUser, // Use current user as driver for now
                            documentationUrlField.getText().trim().isEmpty() ? null : documentationUrlField.getText().trim(),
                            statusCombo.getValue(),
                            currentUser // created by
                        );
                    } else {
                        // Update existing
                        existing.setProjectName(projectNameField.getText().trim());
                        existing.setTaskName(taskNameField.getText().trim());
                        existing.setTargetDate(targetDateTime);
                        existing.setDocumentationUrl(documentationUrlField.getText().trim().isEmpty() ? null : documentationUrlField.getText().trim());
                        existing.setStatus(statusCombo.getValue());
                        existing.setUpdatedBy(currentUser);
                        existing.setUpdatedAt(LocalDateTime.now());
                        return existing;
                    }
                } catch (Exception e) {
                    UIUtils.showErrorDialog("Invalid date", "Please select a valid target date and time");
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
    
    /**
     * Shows dialog for viewing and adding deployment comments using MaterialFX components.
     * 
     * @param deployment The deployment to show comments for
     * @param currentUser Current user for adding comments
     */
    public static void showDeploymentCommentsDialog(Deployment deployment, User currentUser) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Comments - " + deployment.getReleaseName() + " " + deployment.getVersion());
        dialog.setHeaderText("Comments for deployment:");
        
        // Apply theme to dialog
        applyDialogTheme(dialog);
        
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        
        // Main container
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(15));
        mainContainer.setPrefWidth(700);
        mainContainer.setPrefHeight(600);
        mainContainer.setStyle("-fx-background-color: #1a1a1a;");
        
        // Load comments first
        DeploymentService deploymentService = new DeploymentService();
        
        // Comments table using JavaFX TableView with MaterialFX styling
        TableView<DeploymentComment> commentsTable = new TableView<>();
        commentsTable.setPrefHeight(400);
        commentsTable.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #404040; -fx-border-width: 1; " +
                               "-fx-control-inner-background: #2d2d2d; -fx-table-cell-border-color: #404040;");
        
        // Create columns for table
        TableColumn<DeploymentComment, String> userColumn = new TableColumn<>("User");
        userColumn.setPrefWidth(120);
        userColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreatedBy().getFullName())
        );
        userColumn.setCellFactory(column -> new TableCell<DeploymentComment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #00ff85; -fx-font-weight: bold; -fx-background-color: transparent;");
                }
            }
        });
        
        TableColumn<DeploymentComment, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setPrefWidth(140);
        dateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(DateTimeUtil.formatForDisplay(cellData.getValue().getCreatedAt()))
        );
        dateColumn.setCellFactory(column -> new TableCell<DeploymentComment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #8b8b8b; -fx-background-color: transparent;");
                }
            }
        });
        
        TableColumn<DeploymentComment, String> commentColumn = new TableColumn<>("Comment");
        commentColumn.setPrefWidth(420);
        commentColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCommentText())
        );
        commentColumn.setCellFactory(column -> new TableCell<DeploymentComment, String>() {
            private final Text text = new Text();
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setPrefHeight(35);
                } else {
                    text.setText(item);
                    text.setWrappingWidth(400); // Set wrapping width
                    text.setStyle("-fx-fill: #ffffff; -fx-font-size: 12px;");
                    
                    // Force text bounds calculation
                    text.applyCss();
                    text.autosize();
                    
                    // Calculate and set the row height based on text content
                    double textHeight = text.getBoundsInLocal().getHeight();
                    double cellHeight = Math.max(35, textHeight + 20); // Minimum 35px, plus padding
                    setPrefHeight(cellHeight);
                    setMaxHeight(cellHeight);
                    
                    setGraphic(text);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5px;");
                }
            }
        });
        
        commentsTable.getColumns().addAll(userColumn, dateColumn, commentColumn);
        
        // Set table row factory for proper row height calculation
        commentsTable.setRowFactory(table -> {
            TableRow<DeploymentComment> row = new TableRow<DeploymentComment>() {
                @Override
                protected void updateItem(DeploymentComment comment, boolean empty) {
                    super.updateItem(comment, empty);
                    
                    if (empty || comment == null) {
                        setPrefHeight(35);
                        setStyle("-fx-background-color: transparent;");
                    } else {
                        // Calculate proper row height based on comment text
                        Text measureText = new Text(comment.getCommentText());
                        measureText.setWrappingWidth(400);
                        measureText.applyCss();
                        measureText.autosize();
                        
                        double textHeight = measureText.getBoundsInLocal().getHeight();
                        double rowHeight = Math.max(35, textHeight + 25);
                        setPrefHeight(rowHeight);
                        setMaxHeight(rowHeight);
                        
                        // Alternate row colors for better readability
                        if (getIndex() % 2 == 0) {
                            setStyle("-fx-background-color: #2d2d2d;");
                        } else {
                            setStyle("-fx-background-color: #333333;");
                        }
                    }
                }
            };
            return row;
        });
        
        // Load and display comments
        refreshCommentsTable(commentsTable, deployment.getId(), deploymentService);
        
        // Add comment section with MaterialFX components
        VBox addCommentSection = new VBox(10);
        addCommentSection.setPadding(new Insets(15, 0, 0, 0));
        addCommentSection.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #404040; -fx-border-width: 1 0 0 0;");
        
        Label addCommentLabel = new Label("Add Comment:");
        addCommentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-size: 14px;");
        
        TextArea newCommentArea = new TextArea();
        newCommentArea.setPromptText("Enter your comment here...");
        newCommentArea.setPrefRowCount(3);
        newCommentArea.setWrapText(true);
        newCommentArea.setStyle("-fx-control-inner-background: #404040; -fx-text-fill: #ffffff; " +
                               "-fx-border-color: #555555; -fx-background-color: #404040; " +
                               "-fx-prompt-text-fill: #8b8b8b;");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-right;");
        
        Button addCommentButton = new Button("Add Comment");
        addCommentButton.setStyle("-fx-background-color: #00ff85; -fx-text-fill: #000000; -fx-font-weight: bold; " +
                                 "-fx-background-radius: 4; -fx-border-radius: 4;");
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #404040; -fx-text-fill: #ffffff; " +
                           "-fx-background-radius: 4; -fx-border-radius: 4;");
        closeButton.setOnAction(e -> dialog.close());
        
        // Add comment button action
        addCommentButton.setOnAction(e -> {
            String commentText = newCommentArea.getText().trim();
            if (commentText.isEmpty()) {
                UIUtils.showWarningDialog("Empty Comment", "Please enter a comment before adding.");
                return;
            }
            
            try {
                deploymentService.addCommentToDeployment(deployment.getId(), commentText, currentUser);
                newCommentArea.clear();
                refreshCommentsTable(commentsTable, deployment.getId(), deploymentService);
                
                // Update dialog title with new count
                int newCount = deploymentService.getCommentCountForDeployment(deployment.getId());
                dialog.setTitle("Comments (" + newCount + ") - " + deployment.getReleaseName() + " " + deployment.getVersion());
            } catch (Exception ex) {
                UIUtils.showErrorDialog("Error Adding Comment", "Failed to add comment: " + ex.getMessage());
            }
        });
        
        // Enable/disable add button based on text
        newCommentArea.textProperty().addListener((obs, old, newVal) -> {
            addCommentButton.setDisable(newVal.trim().isEmpty());
        });
        addCommentButton.setDisable(true); // Initially disabled
        
        buttonBox.getChildren().addAll(addCommentButton, closeButton);
        addCommentSection.getChildren().addAll(addCommentLabel, newCommentArea, buttonBox);
        
        // Assemble main container
        int commentCount = deploymentService.getCommentCountForDeployment(deployment.getId());
        Label commentsLabel = new Label("Comments (" + commentCount + "):");
        commentsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #ffffff;");
        
        mainContainer.getChildren().addAll(commentsLabel, commentsTable, addCommentSection);
        
        // Set VBox grow priorities
        VBox.setVgrow(commentsTable, javafx.scene.layout.Priority.ALWAYS);
        
        dialog.getDialogPane().setContent(mainContainer);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1a1a;");
        
        dialog.setResultConverter(dialogButton -> null);
        
        dialog.showAndWait();
    }
    
    /**
     * Refreshes the comments table with latest data.
     */
    private static void refreshCommentsTable(TableView<DeploymentComment> commentsTable, Long deploymentId, DeploymentService deploymentService) {
        try {
            commentsTable.getItems().clear();
            commentsTable.getItems().addAll(deploymentService.getCommentsForDeployment(deploymentId));
            
            // Force table to recalculate row heights for text wrapping
            commentsTable.refresh();
        } catch (Exception e) {
            UIUtils.showErrorDialog("Error Loading Comments", "Failed to load comments: " + e.getMessage());
        }
    }
}