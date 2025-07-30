package com.openteam.controller;

import com.openteam.util.UIUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    
    @FXML private VBox navigationMenu;
    @FXML private StackPane contentArea;
    @FXML private Button announcementsButton;
    @FXML private Button activitiesButton;
    @FXML private Button deploymentsButton;
    @FXML private Label lastUpdateLabel;
    
    private AnnouncementController announcementController;
    private ActivityController activityController;
    private DeploymentController deploymentController;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("Initializing main controller");
        
        setupNavigationButtons();
        loadDefaultView();
        updateLastUpdateTime();
    }
    
    /**
     * Sets up navigation button event handlers.
     */
    private void setupNavigationButtons() {
        announcementsButton.setOnAction(event -> showAnnouncementsView());
        activitiesButton.setOnAction(event -> showActivitiesView());
        deploymentsButton.setOnAction(event -> showDeploymentsView());
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
            
            setActiveNavigationButton(announcementsButton);
            updateLastUpdateTime();
            
        } catch (IOException e) {
            logger.error("Error loading announcements view", e);
            UIUtils.showErrorDialog("View Error", "Failed to load announcements view", 
                                  "Error: " + e.getMessage());
        }
    }
    
    /**
     * Shows the activities view.
     */
    @FXML
    private void showActivitiesView() {
        logger.debug("Switching to activities view");
        
        try {
            if (activityController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/activity-view.fxml"));
                contentArea.getChildren().clear();
                contentArea.getChildren().add(loader.load());
                activityController = loader.getController();
            } else {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(activityController.getView());
                activityController.refreshData();
            }
            
            setActiveNavigationButton(activitiesButton);
            updateLastUpdateTime();
            
        } catch (IOException e) {
            logger.error("Error loading activities view", e);
            UIUtils.showErrorDialog("View Error", "Failed to load activities view", 
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
            
            setActiveNavigationButton(deploymentsButton);
            updateLastUpdateTime();
            
        } catch (IOException e) {
            logger.error("Error loading deployments view", e);
            UIUtils.showErrorDialog("View Error", "Failed to load deployments view", 
                                  "Error: " + e.getMessage());
        }
    }
    
    /**
     * Sets the active navigation button style.
     * 
     * @param activeButton Button to mark as active
     */
    private void setActiveNavigationButton(Button activeButton) {
        // Remove active class from all buttons
        announcementsButton.getStyleClass().removeAll("nav-button-active");
        activitiesButton.getStyleClass().removeAll("nav-button-active");
        deploymentsButton.getStyleClass().removeAll("nav-button-active");
        
        // Add active class to the selected button
        activeButton.getStyleClass().add("nav-button-active");
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
            if (announcementsButton.getStyleClass().contains("nav-button-active") && 
                announcementController != null) {
                announcementController.refreshData();
            } else if (activitiesButton.getStyleClass().contains("nav-button-active") && 
                       activityController != null) {
                activityController.refreshData();
            } else if (deploymentsButton.getStyleClass().contains("nav-button-active") && 
                       deploymentController != null) {
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
            "A JavaFX desktop application for team communication and deployment tracking.\n\n" +
            "Features:\n" +
            "• Team announcements management\n" +
            "• Activity scheduling and tracking\n" +
            "• Software deployment monitoring\n" +
            "• Real-time updates with audit logging"
        );
    }
}