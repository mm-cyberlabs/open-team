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
    @FXML private TableColumn<Deployment, String> environmentColumn;
    @FXML private TableColumn<Deployment, String> statusColumn;
    @FXML private TableColumn<Deployment, String> deploymentDateColumn;
    @FXML private TableColumn<Deployment, String> driverColumn;
    @FXML private TableColumn<Deployment, String> archivedColumn;
    @FXML private Button updateButton;
    @FXML private Label statusLabel;
    @FXML private TextArea releaseNotesTextArea;
    @FXML private ComboBox<Environment> environmentFilter;
    @FXML private ComboBox<DeploymentStatus> statusFilter;
    @FXML private CheckBox showArchivedCheckBox;
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
        loadDeployments();
    }
    
    private void setupTable() {
        releaseNameColumn.setCellValueFactory(new PropertyValueFactory<>("releaseName"));
        versionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
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
        updateButton.setOnAction(event -> refreshData());
        
        // Archive filter checkbox
        showArchivedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            filterByArchiveStatus();
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
        double baseWidth = 800; // Assume base container width of 800px
        double availableTableWidth = baseWidth * tableWidthFactor;
        
        // Adjust column widths proportionally
        if (availableTableWidth > 600) {
            // Full width - expand columns
            releaseNameColumn.setPrefWidth(200);
            versionColumn.setPrefWidth(100);
            environmentColumn.setPrefWidth(120);
            statusColumn.setPrefWidth(120);
            deploymentDateColumn.setPrefWidth(150);
            driverColumn.setPrefWidth(140);
            archivedColumn.setPrefWidth(90);
        } else {
            // Compressed width - shrink columns
            releaseNameColumn.setPrefWidth(150);
            versionColumn.setPrefWidth(80);
            environmentColumn.setPrefWidth(90);
            statusColumn.setPrefWidth(90);
            deploymentDateColumn.setPrefWidth(130);
            driverColumn.setPrefWidth(120);
            archivedColumn.setPrefWidth(80);
        }
        
        // Force table to refresh layout
        deploymentsTable.refresh();
    }
    
    private void loadDeployments() {
        try {
            updateButton.setDisable(true);
            statusLabel.setText("Loading deployments...");
            
            List<Deployment> deploymentList;
            if (showArchivedCheckBox.isSelected()) {
                deploymentList = deploymentService.getAllDeployments();
            } else {
                deploymentList = deploymentService.getNonArchivedDeployments();
            }
            
            deployments.clear();
            deployments.addAll(deploymentList);
            
            statusLabel.setText("Loaded " + deploymentList.size() + " deployments");
            logger.info("Loaded {} deployments", deploymentList.size());
            
        } catch (Exception e) {
            logger.error("Error loading deployments", e);
            statusLabel.setText("Error loading deployments");
            UIUtils.showDatabaseErrorDialog(e);
        } finally {
            updateButton.setDisable(false);
        }
    }
    
    private void filterByArchiveStatus() {
        loadDeployments();
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
    private void showProduction() {
        try {
            List<Deployment> production = deploymentService.getProductionDeployments();
            deployments.clear();
            deployments.addAll(production);
            statusLabel.setText("Showing " + production.size() + " production deployments");
            environmentFilter.getSelectionModel().clearSelection();
            statusFilter.getSelectionModel().clearSelection();
        } catch (Exception e) {
            logger.error("Error loading production deployments", e);
            UIUtils.showDatabaseErrorDialog(e);
        }
    }
    
    @FXML
    private void showRecent() {
        try {
            List<Deployment> recent = deploymentService.getRecentDeployments();
            deployments.clear();
            deployments.addAll(recent);
            statusLabel.setText("Showing " + recent.size() + " recent deployments (last 30 days)");
            environmentFilter.getSelectionModel().clearSelection();
            statusFilter.getSelectionModel().clearSelection();
        } catch (Exception e) {
            logger.error("Error loading recent deployments", e);
            UIUtils.showDatabaseErrorDialog(e);
        }
    }
    
    @FXML
    private void showActive() {
        try {
            List<Deployment> active = deploymentService.getActiveDeployments();
            deployments.clear();
            deployments.addAll(active);
            statusLabel.setText("Showing " + active.size() + " active deployments");
            environmentFilter.getSelectionModel().clearSelection();
            statusFilter.getSelectionModel().clearSelection();
        } catch (Exception e) {
            logger.error("Error loading active deployments", e);
            UIUtils.showDatabaseErrorDialog(e);
        }
    }
    
    @FXML
    private void clearSelection() {
        deploymentsTable.getSelectionModel().clearSelection();
        hideRightPanel();
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