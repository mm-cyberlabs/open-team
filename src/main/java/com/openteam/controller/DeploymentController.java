package com.openteam.controller;

import com.openteam.model.Deployment;
import com.openteam.model.DeploymentStatus;
import com.openteam.model.Environment;
import com.openteam.model.User;
import com.openteam.service.DeploymentService;
import com.openteam.util.DateTimeUtil;
import com.openteam.util.DialogUtils;
import com.openteam.util.UIUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import io.github.palexdev.materialfx.controls.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the deployments view.
 * Manages display and interaction with software deployments.
 */
public class DeploymentController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentController.class);
    
    @FXML private VBox rootContainer;
    @FXML private TableView<Deployment> deploymentsTable;
    @FXML private TableColumn<Deployment, String> releaseNameColumn;
    @FXML private TableColumn<Deployment, String> versionColumn;
    @FXML private TableColumn<Deployment, String> ticketNumberColumn;
    @FXML private TableColumn<Deployment, String> environmentColumn;
    @FXML private TableColumn<Deployment, String> statusColumn;
    @FXML private TableColumn<Deployment, String> deploymentDateColumn;
    @FXML private TableColumn<Deployment, String> driverColumn;
    @FXML private TableColumn<Deployment, String> documentationColumn;
    @FXML private TableColumn<Deployment, String> commentsColumn;
    @FXML private TableColumn<Deployment, String> archivedColumn;
    @FXML private Label statusLabel;
    @FXML private TextArea releaseNotesTextArea;
    @FXML private ComboBox<Environment> environmentFilter;
    @FXML private ComboBox<DeploymentStatus> statusFilter;
    @FXML private CheckBox showArchivedCheckBox;
    @FXML private TextField searchField;
    @FXML private SplitPane mainSplitPane;
    
    private final DeploymentService deploymentService;
    private final ObservableList<Deployment> deployments;
    private User currentUser;
    
    public DeploymentController() {
        this.deploymentService = new DeploymentService();
        this.deployments = FXCollections.observableArrayList();
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
        logger.info("Initializing deployment controller");
        
        setupTable();
        setupFilters();
        setupEventHandlers();
        setupRightPanelToggle();
        setupSearch();
        loadDeployments();
    }
    
    private void setupTable() {
        releaseNameColumn.setCellValueFactory(new PropertyValueFactory<>("releaseName"));
        versionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
        ticketNumberColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTicketNumber() != null ? cellData.getValue().getTicketNumber() : ""
            )
        );
        environmentColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getEnvironment().getDisplayName()
            )
        );
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().getDisplayName()
            )
        );
        deploymentDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                DateTimeUtil.formatForDisplay(cellData.getValue().getDeploymentDateTime())
            )
        );
        driverColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDriverUser().getFullName()
            )
        );
        
        documentationColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDocumentationUrl() != null && !cellData.getValue().getDocumentationUrl().isEmpty() ? "View Docs" : ""
            )
        );
        
        commentsColumn.setCellValueFactory(cellData -> {
            Deployment deployment = cellData.getValue();
            int commentCount = deploymentService.getCommentCountForDeployment(deployment.getId());
            return new javafx.beans.property.SimpleStringProperty(commentCount > 0 ? "Comments (" + commentCount + ")" : "Add Comment");
        });
        
        archivedColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getIsArchived() != null && cellData.getValue().getIsArchived() ? "Yes" : "No"
            )
        );
        
        // Set custom cell factories for status and environment columns to show colors
        statusColumn.setCellFactory(column -> new TableCell<Deployment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Deployment deployment = getTableView().getItems().get(getIndex());
                    DeploymentStatus status = deployment.getStatus();
                    setStyle("-fx-text-fill: " + status.getColorCode() + ";");
                }
            }
        });
        
        environmentColumn.setCellFactory(column -> new TableCell<Deployment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Deployment deployment = getTableView().getItems().get(getIndex());
                    Environment environment = deployment.getEnvironment();
                    setStyle("-fx-text-fill: " + environment.getColorCode() + ";");
                }
            }
        });
        
        // Documentation column with clickable link
        documentationColumn.setCellFactory(column -> new TableCell<Deployment, String>() {
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
                    Deployment deployment = getTableView().getItems().get(getIndex());
                    String url = deployment.getDocumentationUrl();
                    
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
        
        // Comments column with clickable link
        commentsColumn.setCellFactory(column -> new TableCell<Deployment, String>() {
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
                    Deployment deployment = getTableView().getItems().get(getIndex());
                    
                    setStyle("-fx-text-fill: #0066cc; -fx-underline: true;");
                    setCursor(javafx.scene.Cursor.HAND);
                    setOnMouseClicked(event -> showCommentsDialog(deployment));
                }
            }
        });
        
        deploymentsTable.setItems(deployments);
        
        // Prevent extra empty columns from appearing
        deploymentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        deploymentsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    displayReleaseNotes(newSelection);
                    showRightPanel();
                } else {
                    hideRightPanel();
                }
            }
        );
        
        // Allow clicking on selected row to deselect
        deploymentsTable.setRowFactory(tv -> {
            TableRow<Deployment> row = new TableRow<Deployment>() {
                private Timeline blinkTimeline;
                
                @Override
                protected void updateItem(Deployment deployment, boolean empty) {
                    super.updateItem(deployment, empty);
                    
                    // Stop any existing animation
                    if (blinkTimeline != null) {
                        blinkTimeline.stop();
                        blinkTimeline = null;
                    }
                    
                    if (empty || deployment == null) {
                        setStyle("");
                        getStyleClass().removeAll("failed-row");
                    } else {
                        // Check if deployment failed
                        boolean isFailed = deployment.getStatus() == DeploymentStatus.FAILED;
                        
                        if (isFailed) {
                            getStyleClass().add("failed-row");
                            // Create blinking animation for failed deployments
                            blinkTimeline = new Timeline(
                                new KeyFrame(Duration.seconds(0), e -> setStyle("-fx-background-color: rgba(244, 67, 54, 0.3);")),
                                new KeyFrame(Duration.seconds(0.75), e -> setStyle("-fx-background-color: rgba(244, 67, 54, 0.8);")),
                                new KeyFrame(Duration.seconds(1.5), e -> setStyle("-fx-background-color: rgba(244, 67, 54, 0.3);"))
                            );
                            blinkTimeline.setCycleCount(Timeline.INDEFINITE);
                            blinkTimeline.play();
                        } else {
                            getStyleClass().removeAll("failed-row");
                            setStyle("");
                        }
                    }
                }
            };
            
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    if (row.isSelected()) {
                        deploymentsTable.getSelectionModel().clearSelection();
                    }
                }
            });
            return row;
        });
    }
    
    private void setupFilters() {
        // Environment filter
        logger.debug("Setting up environment filter with {} items", Environment.values().length);
        environmentFilter.getItems().add(null);
        environmentFilter.getItems().addAll(Environment.values());
        
        // Configure as simple dropdown
        environmentFilter.setEditable(false);
        logger.debug("Environment filter items: {}", environmentFilter.getItems());
        
        // Set prompt text instead of selecting an item to avoid green highlight
        environmentFilter.setPromptText("All");
        // Don't select any item initially
        
        environmentFilter.setConverter(new javafx.util.StringConverter<Environment>() {
            @Override
            public String toString(Environment item) {
                return item == null ? "All" : item.getDisplayName();
            }
            
            @Override
            public Environment fromString(String string) {
                return null;
            }
        });
        
        environmentFilter.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldValue, newValue) -> filterByEnvironment(newValue)
        );
        
        // Status filter
        statusFilter.getItems().add(null);
        statusFilter.getItems().addAll(DeploymentStatus.values());
        
        // Configure as simple dropdown
        statusFilter.setEditable(false);
        // Set prompt text instead of selecting an item to avoid green highlight
        statusFilter.setPromptText("All");
        // Don't select any item initially
        
        statusFilter.setConverter(new javafx.util.StringConverter<DeploymentStatus>() {
            @Override
            public String toString(DeploymentStatus item) {
                return item == null ? "All" : item.getDisplayName();
            }
            
            @Override
            public DeploymentStatus fromString(String string) {
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
        releaseNotesTextArea.clear();
    }
    
    private void adjustTableColumns(double dividerPosition) {
        // Calculate available width for table based on divider position
        // When divider is at 1.0 (right panel hidden), table gets full width
        // When divider is at 0.65 (right panel visible), table gets 65% width
        
        double tableWidthFactor = dividerPosition;
        double baseWidth = 1000; // Increased base width to accommodate new columns
        double availableTableWidth = baseWidth * tableWidthFactor;
        
        // Adjust column widths proportionally
        if (availableTableWidth > 800) {
            // Full width - expand columns
            releaseNameColumn.setPrefWidth(140);
            versionColumn.setPrefWidth(70);
            ticketNumberColumn.setPrefWidth(90);
            environmentColumn.setPrefWidth(90);
            statusColumn.setPrefWidth(90);
            deploymentDateColumn.setPrefWidth(120);
            driverColumn.setPrefWidth(110);
            documentationColumn.setPrefWidth(110);
            commentsColumn.setPrefWidth(100);
            archivedColumn.setPrefWidth(70);
        } else {
            // Compressed width - shrink columns
            releaseNameColumn.setPrefWidth(110);
            versionColumn.setPrefWidth(60);
            ticketNumberColumn.setPrefWidth(70);
            environmentColumn.setPrefWidth(70);
            statusColumn.setPrefWidth(70);
            deploymentDateColumn.setPrefWidth(100);
            driverColumn.setPrefWidth(90);
            documentationColumn.setPrefWidth(90);
            commentsColumn.setPrefWidth(80);
            archivedColumn.setPrefWidth(60);
        }
        
        // Force table to refresh layout
        deploymentsTable.refresh();
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
    
    private void loadDeployments() {
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
            statusLabel.setText("Searching deployments...");
            
            String searchTerm = searchField.getText();
            boolean includeArchived = showArchivedCheckBox.isSelected();
            
            User user = getCurrentUser();
            List<Deployment> deploymentList;
            
            if (user.isSuperAdmin()) {
                // SUPER_ADMIN can see all deployments across all workspaces
                deploymentList = deploymentService.searchDeployments(searchTerm, includeArchived);
            } else {
                // Regular users see only their workspace deployments
                deploymentList = deploymentService.searchDeploymentsByWorkspace(searchTerm, includeArchived, user.getWorkspace().getId());
            }
            
            deployments.clear();
            deployments.addAll(deploymentList);
            
            String searchInfo = searchTerm != null && !searchTerm.trim().isEmpty() ? 
                " matching '" + searchTerm + "'" : "";
            String archiveInfo = includeArchived ? " (including archived)" : "";
            
            statusLabel.setText("Found " + deploymentList.size() + " deployments" + searchInfo + archiveInfo);
            logger.info("Found {} deployments with search term: '{}', includeArchived: {}", 
                       deploymentList.size(), searchTerm, includeArchived);
            
        } catch (Exception e) {
            logger.error("Error searching deployments", e);
            statusLabel.setText("Error searching deployments");
            UIUtils.showDatabaseErrorDialog(e);
        }
    }
    
    public void refreshData() {
        logger.debug("Refreshing deployment data");
        loadDeployments();
    }
    
    private void filterByEnvironment(Environment environment) {
        try {
            if (environment == null) {
                loadDeployments();
            } else {
                User user = getCurrentUser();
                List<Deployment> filtered;
                
                if (user.isSuperAdmin()) {
                    // SUPER_ADMIN can see all deployments by environment across all workspaces
                    filtered = deploymentService.getDeploymentsByEnvironment(environment);
                } else {
                    // Regular users see only their workspace deployments by environment
                    filtered = deploymentService.getDeploymentsByEnvironmentAndWorkspace(environment, user.getWorkspace().getId());
                }
                
                deployments.clear();
                deployments.addAll(filtered);
                statusLabel.setText("Showing " + filtered.size() + " " + 
                                  environment.getDisplayName().toLowerCase() + " deployments");
            }
        } catch (Exception e) {
            logger.error("Error filtering deployments by environment", e);
            UIUtils.showDatabaseErrorDialog(e);
        }
    }
    
    private void filterByStatus(DeploymentStatus status) {
        try {
            if (status == null) {
                loadDeployments();
            } else {
                User user = getCurrentUser();
                List<Deployment> filtered;
                
                if (user.isSuperAdmin()) {
                    // SUPER_ADMIN can see all deployments by status across all workspaces
                    filtered = deploymentService.getDeploymentsByStatus(status);
                } else {
                    // Regular users see only their workspace deployments by status
                    filtered = deploymentService.getDeploymentsByStatusAndWorkspace(status, user.getWorkspace().getId());
                }
                
                deployments.clear();
                deployments.addAll(filtered);
                statusLabel.setText("Showing " + filtered.size() + " " + 
                                  status.getDisplayName().toLowerCase() + " deployments");
            }
        } catch (Exception e) {
            logger.error("Error filtering deployments by status", e);
            UIUtils.showDatabaseErrorDialog(e);
        }
    }
    
    private void displayReleaseNotes(Deployment deployment) {
        if (deployment != null) {
            StringBuilder notes = new StringBuilder();
            notes.append("Release: ").append(deployment.getReleaseName()).append(" v").append(deployment.getVersion()).append("\n\n");
            notes.append("Environment: ").append(deployment.getEnvironment().getDisplayName()).append("\n");
            notes.append("Status: ").append(deployment.getStatus().getDisplayName()).append("\n");
            notes.append("Deployment Date: ").append(DateTimeUtil.formatForDisplay(deployment.getDeploymentDateTime())).append("\n");
            notes.append("Driver: ").append(deployment.getDriverUser().getFullName()).append("\n");
            
            // Add ticket number if available
            if (deployment.getTicketNumber() != null && !deployment.getTicketNumber().trim().isEmpty()) {
                notes.append("Ticket Number: ").append(deployment.getTicketNumber()).append("\n");
            }
            
            // Add documentation link if available
            if (deployment.getDocumentationUrl() != null && !deployment.getDocumentationUrl().trim().isEmpty()) {
                notes.append("Documentation: ").append(deployment.getDocumentationUrl()).append("\n");
            }
            
            notes.append("Created by: ").append(deployment.getCreatedBy().getFullName()).append("\n");
            notes.append("Created: ").append(DateTimeUtil.formatForDisplay(deployment.getCreatedAt())).append("\n");
            
            if (!deployment.getCreatedAt().equals(deployment.getUpdatedAt())) {
                notes.append("Updated: ").append(DateTimeUtil.formatForDisplay(deployment.getUpdatedAt())).append("\n");
                notes.append("Updated by: ").append(deployment.getUpdatedBy().getFullName()).append("\n");
            }
            
            notes.append("\n--- Release Notes ---\n\n");
            notes.append(deployment.getReleaseNotes() != null ? deployment.getReleaseNotes() : "No release notes provided");
            
            releaseNotesTextArea.setText(notes.toString());
        } else {
            releaseNotesTextArea.clear();
        }
    }
    
    public Node getView() {
        return rootContainer;
    }
    
    @FXML
    private void clearSelection() {
        deploymentsTable.getSelectionModel().clearSelection();
        hideRightPanel();
    }
    
    @FXML
    private void unarchiveDeployment() {
        Deployment selected = deploymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showWarningDialog("No Selection", "Please select a deployment to unarchive.");
            return;
        }
        
        if (!selected.getIsArchived()) {
            UIUtils.showWarningDialog("Not Archived", "Selected deployment is not archived.");
            return;
        }
        
        try {
            deploymentService.unarchiveDeployment(selected.getId());
            performSearch(); // Refresh the list
            statusLabel.setText("Deployment unarchived successfully");
            logger.info("Unarchived deployment: {}", selected.getReleaseName());
            
        } catch (Exception e) {
            logger.error("Error unarchiving deployment", e);
            UIUtils.showErrorDialog("Error Unarchiving Deployment", 
                "Failed to unarchive deployment: " + e.getMessage());
        }
    }
    
    @FXML
    private void createDeployment() {
        User user = getCurrentUser();
        Optional<Deployment> result = DialogUtils.showDeploymentDialog(null, user);
        
        result.ifPresent(deployment -> {
            try {
                Deployment created = deploymentService.createDeployment(
                    deployment.getReleaseName(),
                    deployment.getVersion(),
                    deployment.getDeploymentDateTime(),
                    deployment.getDriverUser(),
                    deployment.getReleaseNotes(),
                    deployment.getEnvironment(),
                    deployment.getStatus(),
                    deployment.getTicketNumber(),
                    deployment.getDocumentationUrl(),
                    user
                );
                
                refreshData();
                statusLabel.setText("Deployment created successfully");
                logger.info("Created deployment: {}", created.getReleaseName());
                
            } catch (Exception e) {
                logger.error("Error creating deployment", e);
                UIUtils.showErrorDialog("Error Creating Deployment", 
                    "Failed to create deployment: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void editDeployment() {
        Deployment selected = deploymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showWarningDialog("No Selection", "Please select a deployment to edit.");
            return;
        }
        
        User user = getCurrentUser();
        Optional<Deployment> result = DialogUtils.showDeploymentDialog(selected, user);
        
        result.ifPresent(deployment -> {
            try {
                Deployment updated = deploymentService.updateDeployment(
                    deployment.getId(),
                    deployment.getReleaseName(),
                    deployment.getVersion(),
                    deployment.getDeploymentDateTime(),
                    deployment.getDriverUser(),
                    deployment.getReleaseNotes(),
                    deployment.getEnvironment(),
                    deployment.getStatus(),
                    deployment.getTicketNumber(),
                    deployment.getDocumentationUrl(),
                    user
                );
                
                refreshData();
                statusLabel.setText("Deployment updated successfully");
                logger.info("Updated deployment: {}", updated.getReleaseName());
                
            } catch (Exception e) {
                logger.error("Error updating deployment", e);
                UIUtils.showErrorDialog("Error Updating Deployment", 
                    "Failed to update deployment: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void deleteDeployment() {
        Deployment selected = deploymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showWarningDialog("No Selection", "Please select a deployment to delete.");
            return;
        }
        
        boolean confirmed = DialogUtils.showDeleteConfirmation("deployment", selected.getReleaseName());
        
        if (confirmed) {
            try {
                deploymentService.deleteDeployment(selected.getId());
                refreshData();
                statusLabel.setText("Deployment deleted (archived) successfully");
                logger.info("Deleted deployment: {}", selected.getReleaseName());
                
            } catch (Exception e) {
                logger.error("Error deleting deployment", e);
                UIUtils.showErrorDialog("Error Deleting Deployment", 
                    "Failed to delete deployment: " + e.getMessage());
            }
        }
    }
    
    /**
     * Shows the comments dialog for a deployment.
     * 
     * @param deployment The deployment to show comments for
     */
    private void showCommentsDialog(Deployment deployment) {
        if (deployment == null) {
            return;
        }
        
        logger.info("Showing comments dialog for deployment: {} v{}", deployment.getReleaseName(), deployment.getVersion());
        User user = getCurrentUser();
        DialogUtils.showDeploymentCommentsDialog(deployment, user);
        
        // Refresh the table to update comment counts
        performSearch();
    }
}