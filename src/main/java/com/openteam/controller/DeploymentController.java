package com.openteam.controller;

import com.openteam.model.Deployment;
import com.openteam.model.DeploymentStatus;
import com.openteam.model.Environment;
import com.openteam.service.DeploymentService;
import com.openteam.util.DateTimeUtil;
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
    @FXML private Button updateButton;
    @FXML private Label statusLabel;
    @FXML private TextArea releaseNotesTextArea;
    @FXML private ComboBox<Environment> environmentFilter;
    @FXML private ComboBox<DeploymentStatus> statusFilter;
    
    private final DeploymentService deploymentService;
    private final ObservableList<Deployment> deployments;
    
    public DeploymentController() {
        this.deploymentService = new DeploymentService();
        this.deployments = FXCollections.observableArrayList();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("Initializing deployment controller");
        
        setupTable();
        setupFilters();
        setupEventHandlers();
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
        
        deploymentsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    displayReleaseNotes(newSelection);
                }
            }
        );
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
    }
    
    private void loadDeployments() {
        try {
            updateButton.setDisable(true);
            statusLabel.setText("Loading deployments...");
            
            List<Deployment> deploymentList = deploymentService.getAllDeployments();
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
        releaseNotesTextArea.clear();
    }
}