package com.openteam.controller;

import com.openteam.controller.LoginController.UserSession;
import com.openteam.model.User;
import com.openteam.model.UserRole;
import com.openteam.model.Workspace;
import com.openteam.service.AuthenticationService;
import com.openteam.service.WorkspaceService;
import com.openteam.util.UIUtils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @FXML private Label userInfoLabel;
    @FXML private Label workspaceInfoLabel;
    @FXML private MFXComboBox<WorkspaceOption> workspaceNavigationCombo;
    @FXML private MFXButton logoutButton;
    @FXML private MFXButton adminButton;
    
    private AnnouncementController announcementController;
    private TargetDateController targetDateController;
    private DeploymentController deploymentController;
    private AdminPanelController adminPanelController;
    
    private User currentUser;
    private AuthenticationService authService;
    private WorkspaceService workspaceService;
    private WorkspaceOption selectedWorkspaceOption;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("Initializing main controller");
        
        authService = new AuthenticationService();
        workspaceService = new WorkspaceService();
        setupEventHandlers();
        setupTabNavigation();
        setupWorkspaceNavigation();
        
        // Initial setup will be completed when setCurrentUser is called
    }
    
    /**
     * Sets the current user and configures the UI based on their role.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        configureUIForUser();
        configureWorkspaceNavigation();
        loadDefaultView();
        updateLastUpdateTime();
        updateUserInfo();
    }
    
    /**
     * Configures UI elements based on user role and permissions.
     */
    private void configureUIForUser() {
        if (currentUser == null) {
            return;
        }
        
        // Configure admin access
        if (currentUser.isAdmin() || currentUser.isSuperAdmin()) {
            if (adminButton != null) {
                adminButton.setVisible(true);
            }
        } else {
            if (adminButton != null) {
                adminButton.setVisible(false);
            }
        }
        
        logger.info("UI configured for user: {} with role: {}", 
            currentUser.getUsername(), currentUser.getRole());
    }
    
    /**
     * Sets up event handlers for buttons and UI components.
     */
    private void setupEventHandlers() {
        if (logoutButton != null) {
            logoutButton.setOnAction(e -> handleLogout());
        }
        
        if (adminButton != null) {
            adminButton.setOnAction(e -> openAdminPanel());
        }
    }
    
    /**
     * Updates the user information display.
     */
    private void updateUserInfo() {
        if (userInfoLabel != null && currentUser != null) {
            // Display just the user name in the main label
            userInfoLabel.setText(currentUser.getFullName());
            
            // Display workspace information in the separate workspace label
            if (workspaceInfoLabel != null) {
                if (currentUser.getWorkspace() != null) {
                    workspaceInfoLabel.setText("Workspace: " + currentUser.getWorkspace().getName());
                } else if (currentUser.isSuperAdmin()) {
                    workspaceInfoLabel.setText("Role: System Administrator");
                } else {
                    workspaceInfoLabel.setText("Workspace: --");
                }
            }
        }
    }
    
    /**
     * Handles user logout.
     */
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will need to login again to continue using the application.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                performLogout();
            }
        });
    }
    
    /**
     * Performs the actual logout process.
     */
    private void performLogout() {
        try {
            // Logout from authentication service
            if (UserSession.getSessionToken() != null) {
                authService.logout(UserSession.getSessionToken());
            }
            
            // Clear session
            UserSession.clear();
            
            // Close current window and show login
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            showLoginWindow();
            currentStage.close();
            
        } catch (Exception e) {
            logger.error("Error during logout", e);
            UIUtils.showErrorDialog("Logout Error", "An error occurred during logout: " + e.getMessage());
        }
    }
    
    /**
     * Shows the login window.
     */
    private void showLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Parent root = loader.load();
            
            LoginController loginController = loader.getController();
            
            Stage loginStage = new Stage();
            loginStage.setTitle("Open Team - Login");
            loginStage.setScene(new Scene(root, 500, 600));
            loginStage.setResizable(false);
            
            // Apply login theme
            root.getStylesheets().add(getClass().getResource("/css/login-theme.css").toExternalForm());
            
            loginController.setLoginStage(loginStage);
            loginStage.show();
            
        } catch (IOException e) {
            logger.error("Failed to show login window", e);
            Platform.exit();
        }
    }
    
    /**
     * Opens the admin panel in a new window.
     */
    private void openAdminPanel() {
        if (!currentUser.isAdmin() && !currentUser.isSuperAdmin()) {
            UIUtils.showErrorDialog("Access Denied", "You don't have permission to access the admin panel.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-panel-view.fxml"));
            Parent root = loader.load();
            
            AdminPanelController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage adminStage = new Stage();
            adminStage.setTitle("Open Team - Administration Panel");
            adminStage.setScene(new Scene(root, 1200, 750));
            adminStage.initModality(Modality.APPLICATION_MODAL);
            adminStage.initOwner(adminButton.getScene().getWindow());
            
            // Apply MaterialFX theme to match main application
            root.getStylesheets().add(getClass().getResource("/css/materialfx-theme.css").toExternalForm());
            
            adminStage.showAndWait();
            
        } catch (IOException e) {
            logger.error("Failed to open admin panel", e);
            UIUtils.showErrorDialog("Error", "Failed to open admin panel: " + e.getMessage());
        }
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
                announcementController.setCurrentUser(currentUser);
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
                targetDateController.setCurrentUser(currentUser);
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
                deploymentController.setCurrentUser(currentUser);
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
    
    /**
     * Sets up workspace navigation functionality.
     */
    private void setupWorkspaceNavigation() {
        if (workspaceNavigationCombo != null) {
            workspaceNavigationCombo.setOnAction(e -> handleWorkspaceChange());
        }
    }
    
    /**
     * Configures workspace navigation based on user role.
     */
    private void configureWorkspaceNavigation() {
        if (workspaceNavigationCombo == null || currentUser == null) {
            return;
        }
        
        if (currentUser.isSuperAdmin()) {
            // Super admin can see all workspaces + ALL option
            workspaceNavigationCombo.getItems().clear();
            workspaceNavigationCombo.getItems().add(new WorkspaceOption(null, "ALL")); // ALL option
            
            try {
                for (Workspace workspace : workspaceService.getAccessibleWorkspaces(currentUser)) {
                    workspaceNavigationCombo.getItems().add(new WorkspaceOption(workspace, workspace.getName()));
                }
                
                // Default to ALL for super admins
                workspaceNavigationCombo.setValue(workspaceNavigationCombo.getItems().get(0));
                selectedWorkspaceOption = workspaceNavigationCombo.getItems().get(0);
                
                workspaceNavigationCombo.setVisible(true);
                workspaceNavigationCombo.setDisable(false);
                
            } catch (Exception e) {
                logger.error("Error loading workspaces for navigation", e);
            }
        } else if (currentUser.getWorkspace() != null) {
            // Regular users see only their workspace (disabled)
            workspaceNavigationCombo.getItems().clear();
            workspaceNavigationCombo.getItems().add(new WorkspaceOption(currentUser.getWorkspace(), currentUser.getWorkspace().getName()));
            workspaceNavigationCombo.setValue(workspaceNavigationCombo.getItems().get(0));
            selectedWorkspaceOption = workspaceNavigationCombo.getItems().get(0);
            
            workspaceNavigationCombo.setVisible(true);
            workspaceNavigationCombo.setDisable(true);
        } else {
            // No workspace assigned
            workspaceNavigationCombo.setVisible(false);
        }
    }
    
    /**
     * Handles workspace selection change.
     */
    private void handleWorkspaceChange() {
        WorkspaceOption selected = workspaceNavigationCombo.getValue();
        if (selected != null && !selected.equals(selectedWorkspaceOption)) {
            selectedWorkspaceOption = selected;
            logger.info("Workspace navigation changed to: {}", selected.getDisplayName());
            
            // Refresh all views to show data for the selected workspace
            refreshAllViews();
        }
    }
    
    /**
     * Gets the currently selected workspace for data filtering.
     * @return Selected workspace, or null if "ALL" is selected
     */
    public Workspace getCurrentSelectedWorkspace() {
        return selectedWorkspaceOption != null ? selectedWorkspaceOption.getWorkspace() : null;
    }
    
    /**
     * Checks if "ALL" workspaces is selected.
     * @return true if ALL is selected (super admin only)
     */
    public boolean isAllWorkspacesSelected() {
        return selectedWorkspaceOption != null && selectedWorkspaceOption.getWorkspace() == null;
    }
    
    /**
     * Refreshes all loaded views to reflect workspace changes.
     */
    private void refreshAllViews() {
        if (announcementController != null) {
            announcementController.refreshData();
        }
        if (targetDateController != null) {
            targetDateController.refreshData();
        }
        if (deploymentController != null) {
            deploymentController.refreshData();
        }
    }
    
    /**
     * Workspace option for navigation ComboBox.
     */
    public static class WorkspaceOption {
        private final Workspace workspace;
        private final String displayName;
        
        public WorkspaceOption(Workspace workspace, String displayName) {
            this.workspace = workspace;
            this.displayName = displayName;
        }
        
        public Workspace getWorkspace() {
            return workspace;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            WorkspaceOption that = (WorkspaceOption) obj;
            return java.util.Objects.equals(workspace, that.workspace) &&
                   java.util.Objects.equals(displayName, that.displayName);
        }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(workspace, displayName);
        }
    }
}