package com.openteam.controller;

import com.openteam.util.UIUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

/**
 * Main controller for the Open Team application.
 * Manages navigation between different views and the main application state.
 */
public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    @FXML private StackPane contentArea;
    @FXML private TabPane mainTabPane;
    @FXML private Tab announcementsTab;
    @FXML private Tab targetDatesTab;
    @FXML private Tab deploymentsTab;
    @FXML private Label lastUpdateLabel;
    
    private AnnouncementController announcementController;
    private TargetDateController targetDateController;
    private DeploymentController deploymentController;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("Initializing main controller");
        
        setupTabNavigation();
        loadDefaultView();
        updateLastUpdateTime();
    }
    
    /**
     * Sets up tab navigation event handlers.
     */
    private void setupTabNavigation() {
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == announcementsTab) {
                showAnnouncementsView();
            } else if (newTab == targetDatesTab) {
                showTargetDatesView();
            } else if (newTab == deploymentsTab) {
                showDeploymentsView();
            }
        });
    }
    
    /**
     * Loads the default view (announcements).
     */
    private void loadDefaultView() {
        showAnnouncementsView();
    }
    
    /**
     * Shows the announcements view.
     */
    @FXML
    private void showAnnouncementsView() {
        logger.debug("Switching to announcements view");
        
        try {
            if (announcementController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/announcement-view.fxml"));
                contentArea.getChildren().clear();
                contentArea.getChildren().add(loader.load());
                announcementController = loader.getController();
            } else {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(announcementController.getView());
                announcementController.refreshData();
            }
            
            mainTabPane.getSelectionModel().select(announcementsTab);
            updateLastUpdateTime();
            
        } catch (IOException e) {
            logger.error("Error loading announcements view", e);
            UIUtils.showErrorDialog("View Error", "Failed to load announcements view", 
                                  "Error: " + e.getMessage());
        }
    }
    
    /**
     * Shows the target dates view.
     */
    @FXML
    private void showTargetDatesView() {
        logger.debug("Switching to target dates view");
        
        try {
            if (targetDateController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/target-date-view.fxml"));
                contentArea.getChildren().clear();
                contentArea.getChildren().add(loader.load());
                targetDateController = loader.getController();
            } else {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(targetDateController.getView());
                targetDateController.refreshData();
            }
            
            mainTabPane.getSelectionModel().select(targetDatesTab);
            updateLastUpdateTime();
            
        } catch (IOException e) {
            logger.error("Error loading target dates view", e);
            UIUtils.showErrorDialog("View Error", "Failed to load target dates view", 
                                  "Error: " + e.getMessage());
        }
    }
    
    /**
     * Shows the deployments view.
     */
    @FXML
    private void showDeploymentsView() {
        logger.debug("Switching to deployments view");
        
        try {
            if (deploymentController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/deployment-view.fxml"));
                contentArea.getChildren().clear();
                contentArea.getChildren().add(loader.load());
                deploymentController = loader.getController();
            } else {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(deploymentController.getView());
                deploymentController.refreshData();
            }
            
            mainTabPane.getSelectionModel().select(deploymentsTab);
            updateLastUpdateTime();
            
        } catch (IOException e) {
            logger.error("Error loading deployments view", e);
            UIUtils.showErrorDialog("View Error", "Failed to load deployments view", 
                                  "Error: " + e.getMessage());
        }
    }
    
    
    /**
     * Updates the last update time label.
     */
    private void updateLastUpdateTime() {
        LocalDateTime now = LocalDateTime.now();
        String timeString = now.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        lastUpdateLabel.setText("Last updated: " + timeString);
    }
    
    /**
     * Refreshes the current view.
     */
    @FXML
    private void refreshCurrentView() {
        logger.debug("Refreshing current view");
        
        try {
            if (contentArea.getChildren().isEmpty()) {
                return;
            }
            
            // Determine which view is currently active and refresh it
            Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
            if (selectedTab == announcementsTab && announcementController != null) {
                announcementController.refreshData();
            } else if (selectedTab == targetDatesTab && targetDateController != null) {
                targetDateController.refreshData();
            } else if (selectedTab == deploymentsTab && deploymentController != null) {
                deploymentController.refreshData();
            }
            
            updateLastUpdateTime();
            
        } catch (Exception e) {
            logger.error("Error refreshing current view", e);
            UIUtils.showErrorDialog("Refresh Error", "Failed to refresh view", 
                                  "Error: " + e.getMessage());
        }
    }
    
    /**
     * Shows application information dialog.
     */
    @FXML
    private void showAbout() {
        UIUtils.showInfoDialog(
            "About Open Team",
            "Open Team Communication App v1.0.0",
            "A JavaFX-based application designed for software teams to enhance communication " +
            "and software delivery transparency through a streamlined interface with no complex " +
            "web integrations or overwhelming screens.\n\n" +
            
            "Development Phases:\n" +
            "• Phase 1 [COMPLETED]: Core application with CRUD operations\n" +
            "• Phase 2 [IN PROGRESS]: Real-time notifications, GitHub integration for status updates, " +
            "and enhanced UI based on user feedback\n\n" +
            
            "Key Features:\n" +
            "• Team announcements management\n" +
            "• Important target dates and milestone tracking\n" +
            "• Software deployment monitoring\n" +
            "• Audit logging and archival system\n\n" +
            
            "Copyright © 2025 MM-CyberLabs, LLC\n" +
            "All rights reserved. This software is protected by copyright law."
        );
    }
}