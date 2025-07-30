package com.openteam;

import com.openteam.repository.DatabaseConnection;
import com.openteam.util.UIUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for Open Team Communication App.
 * JavaFX desktop application for team communication and deployment tracking.
 */
public class OpenTeamApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(OpenTeamApplication.class);
    
    private static final String APPLICATION_TITLE = "Open Team Communication App";
    private static final String MAIN_FXML = "/fxml/main-view.fxml";
    private static final String APPLICATION_CSS = "/css/futuristic-theme.css";
    private static final String APPLICATION_ICON = "/icons/app-icon.png";
    
    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting Open Team Application");
        
        try {
            // Test database connection
            if (!testDatabaseConnection()) {
                return; // Exit if database connection fails
            }
            
            // Load main FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAIN_FXML));
            Scene scene = new Scene(fxmlLoader.load());
            
            // Apply CSS theme
            scene.getStylesheets().add(getClass().getResource(APPLICATION_CSS).toExternalForm());
            
            // Configure primary stage
            primaryStage.setTitle(APPLICATION_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);
            
            // Set application icon
            UIUtils.setApplicationIcon(primaryStage, APPLICATION_ICON);
            
            // Show the application
            primaryStage.show();
            
            logger.info("Open Team Application started successfully");
            
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
        
        // Set system properties for JavaFX
        System.setProperty("javafx.application.name", APPLICATION_TITLE);
        
        // Launch JavaFX application
        launch(args);
    }
}