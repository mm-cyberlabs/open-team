package com.openteam.controller;

import com.openteam.model.TargetDate;
import com.openteam.model.TargetDateStatus;
import com.openteam.model.User;
import com.openteam.service.TargetDateService;
import com.openteam.util.DateTimeUtil;
import com.openteam.util.DialogUtils;
import com.openteam.util.UIUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import io.github.palexdev.materialfx.controls.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the target dates view.
 * Manages display and interaction with important target dates and project milestones.
 */
public class TargetDateController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(TargetDateController.class);
    
    @FXML private VBox rootContainer;
    @FXML private TableView<TargetDate> targetDatesTable;
    @FXML private TableColumn<TargetDate, String> projectNameColumn;
    @FXML private TableColumn<TargetDate, String> taskNameColumn;
    @FXML private TableColumn<TargetDate, String> targetDateColumn;
    @FXML private TableColumn<TargetDate, String> driverColumn;
    @FXML private TableColumn<TargetDate, String> statusColumn;
    @FXML private TableColumn<TargetDate, String> documentationColumn;
    @FXML private TableColumn<TargetDate, String> archivedColumn;
    @FXML private Label statusLabel;
    @FXML private TextArea detailsTextArea;
    @FXML private ComboBox<TargetDateStatus> statusFilter;
    @FXML private CheckBox showArchivedCheckBox;
    @FXML private TextField searchField;
    @FXML private SplitPane mainSplitPane;
    
    private final TargetDateService targetDateService;
    private final ObservableList<TargetDate> targetDates;
    private User currentUser;
    
    public TargetDateController() {
        this.targetDateService = new TargetDateService();
        this.targetDates = FXCollections.observableArrayList();
    }
    
    /**
     * Sets the current user for this controller.
     * This should be called after login to establish the user context.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Gets the current user, falling back to session if not set.
     */
    private User getCurrentUser() {
        if (currentUser != null) {
            return currentUser;
        }
        
        // Try to get from session
        if (LoginController.UserSession.isLoggedIn()) {
            return LoginController.UserSession.getCurrentUser();
        }
        
        throw new IllegalStateException("No current user available. User must be logged in.");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("Initializing target date controller");
        
        setupTable();
        setupFilters();
        setupEventHandlers();
        setupRightPanelToggle();
        setupSearch();
        loadTargetDates();
    }
    
    private void setupTable() {
        projectNameColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        taskNameColumn.setCellValueFactory(new PropertyValueFactory<>("taskName"));
        targetDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                DateTimeUtil.formatForDisplay(cellData.getValue().getTargetDate())
            )
        );
        driverColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDriverUser().getFullName()
            )
        );
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().getDisplayName()
            )
        );
        
        documentationColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDocumentationUrl() != null && !cellData.getValue().getDocumentationUrl().isEmpty() ? "View Docs" : ""
            )
        );
        
        archivedColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getIsArchived() != null && cellData.getValue().getIsArchived() ? "Yes" : "No"
            )
        );
        
        // Set custom cell factories for status column to show colors
        statusColumn.setCellFactory(column -> new TableCell<TargetDate, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    TargetDate targetDate = getTableView().getItems().get(getIndex());
                    TargetDateStatus status = targetDate.getStatus();
                    setStyle("-fx-text-fill: " + status.getColorCode() + ";");
                }
            }
        });
        
        // Documentation column with clickable link
        documentationColumn.setCellFactory(column -> new TableCell<TargetDate, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                    setStyle("");
                    setOnMouseClicked(null);
                    setCursor(null);
                } else {
                    setText(item);
                    TargetDate targetDate = getTableView().getItems().get(getIndex());
                    String url = targetDate.getDocumentationUrl();
                    
                    if (url != null && !url.isEmpty()) {
                        setStyle("-fx-text-fill: #00ff85; -fx-underline: true;");
                        setCursor(javafx.scene.Cursor.HAND);
                        setOnMouseClicked(event -> openUrl(url));
                    } else {
                        setStyle("");
                        setOnMouseClicked(null);
                        setCursor(null);
                    }
                }
            }
        });
        
        targetDatesTable.setItems(targetDates);
        
        // Prevent extra empty columns from appearing
        targetDatesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        targetDatesTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    displayTargetDateDetails(newSelection);
                    showRightPanel();
                } else {
                    hideRightPanel();
                }
            }
        );
        
        // Allow clicking on selected row to deselect and handle overdue styling
        targetDatesTable.setRowFactory(tv -> {
            TableRow<TargetDate> row = new TableRow<TargetDate>() {
                private Timeline blinkTimeline;
                
                @Override
                protected void updateItem(TargetDate targetDate, boolean empty) {
                    super.updateItem(targetDate, empty);
                    
                    // Stop any existing animation
                    if (blinkTimeline != null) {
                        blinkTimeline.stop();
                        blinkTimeline = null;
                    }
                    
                    if (empty || targetDate == null) {
                        setStyle("");
                        getStyleClass().removeAll("overdue-row");
                    } else {
                        // Check if target date is overdue and not completed
                        boolean isOverdue = targetDate.getTargetDate() != null &&
                            targetDate.getTargetDate().isBefore(java.time.LocalDateTime.now()) &&
                            targetDate.getStatus() != TargetDateStatus.COMPLETED &&
                            targetDate.getStatus() != TargetDateStatus.CANCELLED;
                        
                        if (isOverdue) {
                            getStyleClass().add("overdue-row");
                            // Create blinking animation
                            blinkTimeline = new Timeline(
                                new KeyFrame(Duration.seconds(0), e -> setStyle("-fx-background-color: rgba(244, 67, 54, 0.3);")),
                                new KeyFrame(Duration.seconds(1), e -> setStyle("-fx-background-color: rgba(244, 67, 54, 0.8);")),
                                new KeyFrame(Duration.seconds(2), e -> setStyle("-fx-background-color: rgba(244, 67, 54, 0.3);"))
                            );
                            blinkTimeline.setCycleCount(Timeline.INDEFINITE);
                            blinkTimeline.play();
                        } else {
                            getStyleClass().removeAll("overdue-row");
                            setStyle("");
                        }
                    }
                }
            };
            
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    if (row.isSelected()) {
                        targetDatesTable.getSelectionModel().clearSelection();
                    }
                }
            });
            return row;
        });
    }
    
    private void setupFilters() {
        // Status filter
        statusFilter.getItems().add(null);
        statusFilter.getItems().addAll(TargetDateStatus.values());
        
        // Disable search functionality - make it a simple dropdown
        statusFilter.setEditable(false);
        // Set prompt text instead of selecting an item to avoid green highlight
        statusFilter.setPromptText("All");
        // Don't select any item initially
        
        statusFilter.setConverter(new javafx.util.StringConverter<TargetDateStatus>() {
            @Override
            public String toString(TargetDateStatus item) {
                return item == null ? "All Statuses" : item.getDisplayName();
            }
            
            @Override
            public TargetDateStatus fromString(String string) {
                return null;
            }
        });
        
        statusFilter.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldValue, newValue) -> filterByStatus(newValue)
        );
    }
    
    private void setupEventHandlers() {
        // Archive filter checkbox
        showArchivedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            performSearch();
        });
    }
    
    private void setupRightPanelToggle() {
        // Initially hide the right panel
        hideRightPanel();
        
        // Add listener for divider position changes to make table responsive
        mainSplitPane.getDividers().get(0).positionProperty().addListener((obs, oldPos, newPos) -> {
            adjustTableColumns(newPos.doubleValue());
        });
    }
    
    private void showRightPanel() {
        if (mainSplitPane.getDividers().size() > 0) {
            mainSplitPane.setDividerPositions(0.65);
            mainSplitPane.getItems().get(1).setVisible(true);
            mainSplitPane.getItems().get(1).setManaged(true);
        }
    }
    
    private void hideRightPanel() {
        if (mainSplitPane.getDividers().size() > 0) {
            mainSplitPane.setDividerPositions(1.0);
            mainSplitPane.getItems().get(1).setVisible(false);
            mainSplitPane.getItems().get(1).setManaged(false);
        }
        detailsTextArea.clear();
    }
    
    private void adjustTableColumns(double dividerPosition) {
        // Calculate available width for table based on divider position
        double tableWidthFactor = dividerPosition;
        double baseWidth = 1000;
        double availableTableWidth = baseWidth * tableWidthFactor;
        
        // Adjust column widths proportionally
        if (availableTableWidth > 800) {
            // Full width - expand columns
            projectNameColumn.setPrefWidth(200);
            taskNameColumn.setPrefWidth(200);
            targetDateColumn.setPrefWidth(130);
            driverColumn.setPrefWidth(120);
            statusColumn.setPrefWidth(100);
            documentationColumn.setPrefWidth(120);
            archivedColumn.setPrefWidth(80);
        } else {
            // Compressed width - shrink columns
            projectNameColumn.setPrefWidth(150);
            taskNameColumn.setPrefWidth(150);
            targetDateColumn.setPrefWidth(110);
            driverColumn.setPrefWidth(100);
            statusColumn.setPrefWidth(80);
            documentationColumn.setPrefWidth(100);
            archivedColumn.setPrefWidth(70);
        }
        
        // Force table to refresh layout
        targetDatesTable.refresh();
    }
    
    private void openUrl(String url) {
        try {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.net.URI uri = new java.net.URI(url);
                desktop.browse(uri);
                logger.info("Opened documentation URL: {}", url);
            } else {
                logger.warn("Desktop browsing not supported");
                UIUtils.showWarningDialog("Browser Not Supported", "Cannot open URL: " + url);
            }
        } catch (Exception e) {
            logger.error("Error opening URL: " + url, e);
            UIUtils.showErrorDialog("Error Opening URL", "Failed to open documentation: " + e.getMessage());
        }
    }
    
    private void loadTargetDates() {
        performSearch();
    }
    
    private void setupSearch() {
        // Setup search field with real-time search
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            performSearch();
        });
    }
    
    private void performSearch() {
        try {
            statusLabel.setText("Searching target dates...");
            
            String searchTerm = searchField.getText();
            boolean includeArchived = showArchivedCheckBox.isSelected();
            
            User user = getCurrentUser();
            List<TargetDate> targetDateList;
            
            if (user.isSuperAdmin()) {
                // SUPER_ADMIN can see all target dates across all workspaces
                targetDateList = targetDateService.searchTargetDates(searchTerm, includeArchived);
            } else {
                // Regular users see only their workspace target dates
                targetDateList = targetDateService.searchTargetDatesByWorkspace(searchTerm, includeArchived, user.getWorkspace().getId());
            }
            
            targetDates.clear();
            targetDates.addAll(targetDateList);
            
            String searchInfo = searchTerm != null && !searchTerm.trim().isEmpty() ? 
                " matching '" + searchTerm + "'" : "";
            String archiveInfo = includeArchived ? " (including archived)" : "";
            
            statusLabel.setText("Found " + targetDateList.size() + " target dates" + searchInfo + archiveInfo);
            logger.info("Found {} target dates with search term: '{}', includeArchived: {}", 
                       targetDateList.size(), searchTerm, includeArchived);
            
        } catch (Exception e) {
            logger.error("Error searching target dates", e);
            statusLabel.setText("Error searching target dates");
            UIUtils.showDatabaseErrorDialog(e);
        }
    }
    
    public void refreshData() {
        logger.debug("Refreshing target date data");
        loadTargetDates();
    }
    
    private void filterByStatus(TargetDateStatus status) {
        try {
            if (status == null) {
                loadTargetDates();
            } else {
                User user = getCurrentUser();
                List<TargetDate> filtered;
                
                if (user.isSuperAdmin()) {
                    // SUPER_ADMIN can see all target dates by status across all workspaces
                    filtered = targetDateService.getTargetDatesByStatus(status);
                } else {
                    // Regular users see only their workspace target dates by status
                    filtered = targetDateService.getTargetDatesByStatusAndWorkspace(status, user.getWorkspace().getId());
                }
                
                targetDates.clear();
                targetDates.addAll(filtered);
                statusLabel.setText("Showing " + filtered.size() + " " + 
                                  status.getDisplayName().toLowerCase() + " target dates");
            }
        } catch (Exception e) {
            logger.error("Error filtering target dates by status", e);
            UIUtils.showDatabaseErrorDialog(e);
        }
    }
    
    private void displayTargetDateDetails(TargetDate targetDate) {
        if (targetDate != null) {
            StringBuilder details = new StringBuilder();
            details.append("Project: ").append(targetDate.getProjectName()).append("\n");
            details.append("Task: ").append(targetDate.getTaskName()).append("\n\n");
            details.append("Target Date: ").append(DateTimeUtil.formatForDisplay(targetDate.getTargetDate())).append("\n");
            details.append("Status: ").append(targetDate.getStatus().getDisplayName()).append("\n");
            details.append("Driver: ").append(targetDate.getDriverUser().getFullName()).append("\n");
            
            if (targetDate.getDocumentationUrl() != null && !targetDate.getDocumentationUrl().trim().isEmpty()) {
                details.append("Documentation: ").append(targetDate.getDocumentationUrl()).append("\n");
            }
            
            details.append("Created by: ").append(targetDate.getCreatedBy().getFullName()).append("\n");
            details.append("Created: ").append(DateTimeUtil.formatForDisplay(targetDate.getCreatedAt())).append("\n");
            
            if (!targetDate.getCreatedAt().equals(targetDate.getUpdatedAt())) {
                details.append("Updated: ").append(DateTimeUtil.formatForDisplay(targetDate.getUpdatedAt())).append("\n");
                details.append("Updated by: ").append(targetDate.getUpdatedBy().getFullName()).append("\n");
            }
            
            detailsTextArea.setText(details.toString());
        } else {
            detailsTextArea.clear();
        }
    }
    
    public Node getView() {
        return rootContainer;
    }
    
    @FXML
    private void clearSelection() {
        targetDatesTable.getSelectionModel().clearSelection();
        hideRightPanel();
    }
    
    @FXML
    private void unarchiveTargetDate() {
        TargetDate selected = targetDatesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showWarningDialog("No Selection", "Please select a target date to unarchive.");
            return;
        }
        
        if (!selected.getIsArchived()) {
            UIUtils.showWarningDialog("Not Archived", "Selected target date is not archived.");
            return;
        }
        
        try {
            targetDateService.unarchiveTargetDate(selected.getId());
            performSearch(); // Refresh the list
            statusLabel.setText("Target date unarchived successfully");
            logger.info("Unarchived target date: {} - {}", selected.getProjectName(), selected.getTaskName());
            
        } catch (Exception e) {
            logger.error("Error unarchiving target date", e);
            UIUtils.showErrorDialog("Error Unarchiving Target Date", 
                "Failed to unarchive target date: " + e.getMessage());
        }
    }
    
    @FXML
    private void createTargetDate() {
        User user = getCurrentUser();
        Optional<TargetDate> result = DialogUtils.showTargetDateDialog(null, user);
        
        result.ifPresent(targetDate -> {
            try {
                TargetDate created = targetDateService.createTargetDate(
                    targetDate.getProjectName(),
                    targetDate.getTaskName(),
                    targetDate.getTargetDate(),
                    targetDate.getDriverUser(),
                    targetDate.getDocumentationUrl(),
                    targetDate.getStatus(),
                    user
                );
                
                refreshData();
                statusLabel.setText("Target date created successfully");
                logger.info("Created target date: {} - {}", created.getProjectName(), created.getTaskName());
                
            } catch (Exception e) {
                logger.error("Error creating target date", e);
                UIUtils.showErrorDialog("Error Creating Target Date", 
                    "Failed to create target date: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void editTargetDate() {
        TargetDate selected = targetDatesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showWarningDialog("No Selection", "Please select a target date to edit.");
            return;
        }
        
        User user = getCurrentUser();
        Optional<TargetDate> result = DialogUtils.showTargetDateDialog(selected, user);
        
        result.ifPresent(targetDate -> {
            try {
                TargetDate updated = targetDateService.updateTargetDate(
                    targetDate.getId(),
                    targetDate.getProjectName(),
                    targetDate.getTaskName(),
                    targetDate.getTargetDate(),
                    targetDate.getDriverUser(),
                    targetDate.getDocumentationUrl(),
                    targetDate.getStatus(),
                    user
                );
                
                refreshData();
                statusLabel.setText("Target date updated successfully");
                logger.info("Updated target date: {} - {}", updated.getProjectName(), updated.getTaskName());
                
            } catch (Exception e) {
                logger.error("Error updating target date", e);
                UIUtils.showErrorDialog("Error Updating Target Date", 
                    "Failed to update target date: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void deleteTargetDate() {
        TargetDate selected = targetDatesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showWarningDialog("No Selection", "Please select a target date to delete.");
            return;
        }
        
        boolean confirmed = DialogUtils.showDeleteConfirmation("target date", 
            selected.getProjectName() + " - " + selected.getTaskName());
        
        if (confirmed) {
            try {
                targetDateService.deleteTargetDate(selected.getId());
                refreshData();
                statusLabel.setText("Target date deleted (archived) successfully");
                logger.info("Deleted target date: {} - {}", selected.getProjectName(), selected.getTaskName());
                
            } catch (Exception e) {
                logger.error("Error deleting target date", e);
                UIUtils.showErrorDialog("Error Deleting Target Date", 
                    "Failed to delete target date: " + e.getMessage());
            }
        }
    }
}