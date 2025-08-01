package com.openteam.controller;

import com.openteam.model.Deployment;
import com.openteam.model.DeploymentStatus;
import com.openteam.model.Environment;
import com.openteam.model.User;
import com.openteam.service.DeploymentService;
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
    private final User currentUser;
    
    public DeploymentController() {
        this.deploymentService = new DeploymentService();
        this.deployments = FXCollections.observableArrayList();
        this.currentUser = createDefaultUser();
    }
    
    private User createDefaultUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setFullName("System Administrator");
        user.setEmail("admin@company.com");
        return user;
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
            TableRow<Deployment> row = new TableRow<>();
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
        environmentFilter.getItems().add(null);
        environmentFilter.getItems().addAll(Environment.values());
        
        environmentFilter.setButtonCell(new ListCell<Environment>() {
            @Override
            protected void updateItem(Environment item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "All Environments" : item.getDisplayName());
            }
        });
        
        environmentFilter.setCellFactory(listView -> new ListCell<Environment>() {
            @Override
            protected void updateItem(Environment item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "All Environments" : item.getDisplayName());
            }
        });
        
        environmentFilter.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldValue, newValue) -> filterByEnvironment(newValue)
        );
        
        // Status filter
        statusFilter.getItems().add(null);
        statusFilter.getItems().addAll(DeploymentStatus.values());
        
        statusFilter.setButtonCell(new ListCell<DeploymentStatus>() {
            @Override
            protected void updateItem(DeploymentStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "All Statuses" : item.getDisplayName());
            }
        });
        
        statusFilter.setCellFactory(listView -> new ListCell<DeploymentStatus>() {
            @Override
            protected void updateItem(DeploymentStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "All Statuses" : item.getDisplayName());
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
            releaseNameColumn.setPrefWidth(150);
            versionColumn.setPrefWidth(80);
            ticketNumberColumn.setPrefWidth(100);
            environmentColumn.setPrefWidth(100);
            statusColumn.setPrefWidth(100);
            deploymentDateColumn.setPrefWidth(130);
            driverColumn.setPrefWidth(120);
            documentationColumn.setPrefWidth(120);
            archivedColumn.setPrefWidth(80);
        } else {
            // Compressed width - shrink columns
            releaseNameColumn.setPrefWidth(120);
            versionColumn.setPrefWidth(70);
            ticketNumberColumn.setPrefWidth(80);
            environmentColumn.setPrefWidth(80);
            statusColumn.setPrefWidth(80);
            deploymentDateColumn.setPrefWidth(110);
            driverColumn.setPrefWidth(100);
            documentationColumn.setPrefWidth(100);
            archivedColumn.setPrefWidth(70);
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
            
            List<Deployment> deploymentList = deploymentService.searchDeployments(searchTerm, includeArchived);
            
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
                List<Deployment> filtered = deploymentService.getDeploymentsByEnvironment(environment);
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
                List<Deployment> filtered = deploymentService.getDeploymentsByStatus(status);
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
        Optional<Deployment> result = DialogUtils.showDeploymentDialog(null, currentUser);
        
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
                    currentUser
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
        
        Optional<Deployment> result = DialogUtils.showDeploymentDialog(selected, currentUser);
        
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
                    currentUser
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
}