package com.openteam.controller;

import com.openteam.model.User;
import com.openteam.service.AuthenticationService;
import com.openteam.util.UIUtils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the login screen.
 * Handles user authentication and navigation to the main application.
 */
public class LoginController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML private VBox loginContainer;
    @FXML private MFXTextField usernameField;
    @FXML private MFXPasswordField passwordField;
    @FXML private MFXButton loginButton;
    @FXML private MFXButton exitButton;
    @FXML private Label statusLabel;
    @FXML private Label versionLabel;
    
    private AuthenticationService authService;
    private Stage loginStage;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = new AuthenticationService();
        setupUI();
        loadStoredCredentials();
    }
    
    /**
     * Sets the login stage reference for closing after successful login.
     */
    public void setLoginStage(Stage loginStage) {
        this.loginStage = loginStage;
    }
    
    /**
     * Sets up the UI components and event handlers.
     */
    private void setupUI() {
        // Set version label
        versionLabel.setText("Open Team v1.0.0");
        
        // Set up button actions
        loginButton.setOnAction(e -> handleLogin());
        exitButton.setOnAction(e -> Platform.exit());
        
        // Set up Enter key handling
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());
        
        // Initial focus
        Platform.runLater(() -> {
            if (usernameField.getText().isEmpty()) {
                usernameField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
        });
        
        // Clear status on field changes
        usernameField.textProperty().addListener((obs, old, newVal) -> clearStatus());
        passwordField.textProperty().addListener((obs, old, newVal) -> clearStatus());
    }
    
    /**
     * Loads stored credentials from keychain if available.
     */
    private void loadStoredCredentials() {
        try {
            AuthenticationService.KeychainCredentials credentials = authService.getStoredCredentials();
            if (credentials != null && !credentials.isEmpty()) {
                usernameField.setText(credentials.getUsername());
                logger.debug("Loaded stored username from keychain");
            }
        } catch (Exception e) {
            logger.debug("Could not load stored credentials: {}", e.getMessage());
        }
    }
    
    /**
     * Handles the login button click.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validate input
        if (username.isEmpty()) {
            showStatus("Please enter your username", "error");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showStatus("Please enter your password", "error");
            passwordField.requestFocus();
            return;
        }
        
        // Disable UI during authentication
        setUIEnabled(false);
        showStatus("Authenticating...", "info");
        
        // Perform authentication in background thread
        Task<AuthenticationService.AuthenticationResult> authTask = new Task<>() {
            @Override
            protected AuthenticationService.AuthenticationResult call() {
                return authService.authenticate(username, password);
            }
        };
        
        authTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                AuthenticationService.AuthenticationResult result = authTask.getValue();
                handleAuthenticationResult(result);
            });
        });
        
        authTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                logger.error("Authentication task failed", authTask.getException());
                showStatus("Authentication failed. Please try again.", "error");
                setUIEnabled(true);
            });
        });
        
        Thread authThread = new Thread(authTask);
        authThread.setDaemon(true);
        authThread.start();
    }
    
    /**
     * Handles the result of authentication attempt.
     */
    private void handleAuthenticationResult(AuthenticationService.AuthenticationResult result) {
        if (result.isSuccess()) {
            showStatus("Login successful!", "success");
            
            // Store user session and navigate to main application
            UserSession.setCurrentUser(result.getUser());
            UserSession.setSessionToken(result.getSessionToken());
            
            try {
                openMainApplication();
            } catch (IOException e) {
                logger.error("Failed to open main application", e);
                showStatus("Failed to open main application", "error");
                setUIEnabled(true);
            }
        } else {
            showStatus(result.getMessage(), "error");
            passwordField.clear();
            passwordField.requestFocus();
            setUIEnabled(true);
        }
    }
    
    /**
     * Opens the main application window and closes the login window.
     */
    private void openMainApplication() throws IOException {
        // Load main application FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
        Parent root = loader.load();
        
        // Get the main controller and pass user session
        MainController mainController = loader.getController();
        mainController.setCurrentUser(UserSession.getCurrentUser());
        
        // Create new stage for main application
        Stage mainStage = new Stage();
        mainStage.setTitle("Open Team - " + UserSession.getCurrentUser().getFullName());
        Scene mainScene = new Scene(root, 1200, 800);
        
        // Apply MaterialFX dark theme
        mainScene.getStylesheets().add(getClass().getResource("/css/materialfx-theme.css").toExternalForm());
        mainScene.getStylesheets().add(getClass().getResource("/css/futuristic-theme.css").toExternalForm());
        
        mainStage.setScene(mainScene);
        mainStage.setMinWidth(800);
        mainStage.setMinHeight(600);
        
        // Handle main window close
        mainStage.setOnCloseRequest(e -> {
            // Logout when main window is closed
            if (UserSession.getSessionToken() != null) {
                authService.logout(UserSession.getSessionToken());
            }
            UserSession.clear();
            Platform.exit();
        });
        
        // Show main window and close login window
        mainStage.show();
        
        if (loginStage != null) {
            loginStage.close();
        }
        
        logger.info("User {} logged in successfully", UserSession.getCurrentUser().getUsername());
    }
    
    /**
     * Shows a status message to the user.
     */
    private void showStatus(String message, String type) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-error", "status-success", "status-info");
        statusLabel.getStyleClass().add("status-" + type);
        statusLabel.setVisible(true);
    }
    
    /**
     * Clears the status message.
     */
    private void clearStatus() {
        statusLabel.setVisible(false);
    }
    
    /**
     * Enables or disables the UI components.
     */
    private void setUIEnabled(boolean enabled) {
        usernameField.setDisable(!enabled);
        passwordField.setDisable(!enabled);
        loginButton.setDisable(!enabled);
    }
    
    /**
     * Static class to manage user session state.
     */
    public static class UserSession {
        private static User currentUser;
        private static String sessionToken;
        
        public static User getCurrentUser() {
            return currentUser;
        }
        
        public static void setCurrentUser(User user) {
            currentUser = user;
        }
        
        public static String getSessionToken() {
            return sessionToken;
        }
        
        public static void setSessionToken(String token) {
            sessionToken = token;
        }
        
        public static void clear() {
            currentUser = null;
            sessionToken = null;
        }
        
        public static boolean isLoggedIn() {
            return currentUser != null && sessionToken != null;
        }
    }
}