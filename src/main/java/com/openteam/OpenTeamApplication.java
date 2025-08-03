package com.openteam;

import com.openteam.controller.LoginController;
import com.openteam.repository.DatabaseConnection;
import com.openteam.util.UIUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for Open Team Communication App.
 * JavaFX desktop application for team communication and deployment tracking.
 * Now includes authentication - starts with login screen.
 */
public class OpenTeamApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(OpenTeamApplication.class);
    
    private static final String APPLICATION_TITLE = "Open Team - Login";
    private static final String LOGIN_FXML = "/fxml/login-view.fxml";
    private static final String LOGIN_CSS = "/css/login-theme.css";
    private static final String APPLICATION_ICON = "/icons/openteam-logo.png";
    
    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting Open Team Application");
        
        try {
            // Test database connection
            if (!testDatabaseConnection()) {
                return; // Exit if database connection fails
            }
            
            // Load login FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LOGIN_FXML));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 600, 700);
            
            // Set scene background to dark
            scene.setFill(javafx.scene.paint.Color.web("#1a1a1a"));
            
            // Get login controller
            LoginController loginController = fxmlLoader.getController();
            loginController.setLoginStage(primaryStage);
            
            // Apply CSS theme
            scene.getStylesheets().add(getClass().getResource(LOGIN_CSS).toExternalForm());
            
            // Configure primary stage
            primaryStage.setTitle(APPLICATION_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            
            // Set application icon
            UIUtils.setApplicationIcon(primaryStage, APPLICATION_ICON);
            
            // Show the login screen
            primaryStage.show();
            
            logger.info("Open Team Application login screen displayed");
            
        } catch (Exception e) {
            logger.error("Failed to start Open Team Application", e);
            UIUtils.showErrorDialog(
                "Startup Error",
                "Failed to start the application",
                "Error: " + e.getMessage()
            );
        }
    }
    
    @Override
    public void stop() {
        logger.info("Stopping Open Team Application");
        
        try {
            // Close database connections
            DatabaseConnection.getInstance().close();
            logger.info("Database connections closed");
        } catch (Exception e) {
            logger.error("Error during application shutdown", e);
        }
        
        logger.info("Open Team Application stopped");
    }
    
    /**
     * Tests the database connection on startup.
     * 
     * @return true if connection is successful, false otherwise
     */
    private boolean testDatabaseConnection() {
        try {
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            if (dbConnection.testConnection()) {
                logger.info("Database connection test successful");
                
                // Run database migrations to ensure schema is up to date
                com.openteam.util.DatabaseMigrationUtil.ensureColumnsExist();
                
                return true;
            } else {
                logger.error("Database connection test failed");
                UIUtils.showErrorDialog(
                    "Database Connection Error",
                    "Cannot connect to the database",
                    "Please check your database configuration in ~/.openteam/config.yml\n\n" +
                    "Make sure PostgreSQL is running and the database exists."
                );
                return false;
            }
        } catch (Exception e) {
            logger.error("Database connection error during startup", e);
            UIUtils.showConfigurationErrorDialog(e);
            return false;
        }
    }
    
    /**
     * Main method to launch the JavaFX application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        logger.info("Open Team Application main method called");
        
        // Set system properties for JavaFX and macOS
        System.setProperty("javafx.application.name", APPLICATION_TITLE);
        
        // macOS specific properties
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", APPLICATION_TITLE);
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", APPLICATION_TITLE);
            
            // Try to set dock icon at startup
            try {
                var iconUrl = OpenTeamApplication.class.getResource(APPLICATION_ICON);
                if (iconUrl != null) {
                    System.setProperty("apple.awt.application.icon", iconUrl.getPath());
                }
            } catch (Exception e) {
                logger.debug("Could not set apple.awt.application.icon property", e);
            }
        }
        
        // Launch JavaFX application
        launch(args);
    }
}