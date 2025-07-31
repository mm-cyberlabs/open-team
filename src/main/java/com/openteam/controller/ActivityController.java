package com.openteam.controller;

import com.openteam.model.Activity;
import com.openteam.model.ActivityType;
import com.openteam.model.User;
import com.openteam.service.ActivityService;
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
 * Controller for the activities view.
 * Manages display and interaction with team activities.
 */
public class ActivityController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);
    
    @FXML private VBox rootContainer;
    @FXML private TableView<Activity> activitiesTable;
    @FXML private TableColumn<Activity, String> titleColumn;
    @FXML private TableColumn<Activity, String> typeColumn;
    @FXML private TableColumn<Activity, String> scheduledDateColumn;
    @FXML private TableColumn<Activity, String> locationColumn;
    @FXML private TableColumn<Activity, String> createdByColumn;
    @FXML private TableColumn<Activity, String> archivedColumn;
    @FXML private Button updateButton;
    @FXML private Label statusLabel;
    @FXML private TextArea detailsTextArea;
    @FXML private ComboBox<ActivityType> typeFilter;
    @FXML private CheckBox showArchivedCheckBox;
    @FXML private SplitPane mainSplitPane;
    
    private final ActivityService activityService;
    private final ObservableList<Activity> activities;
    private final User currentUser;
    
    public ActivityController() {
        this.activityService = new ActivityService();
        this.activities = FXCollections.observableArrayList();
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
        logger.info("Initializing activity controller");
        
        setupTable();
        setupTypeFilter();
        setupEventHandlers();
        loadActivities();
    }
    
    private void setupTable() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getActivityType().getDisplayName()
            )
        );
        scheduledDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                DateTimeUtil.formatForDisplay(cellData.getValue().getScheduledDate())
            )
        );
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        createdByColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCreatedBy().getFullName()
            )
        );
        
        activitiesTable.setItems(activities);
        
        // Prevent extra empty columns from appearing
        activitiesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        activitiesTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    displayActivityDetails(newSelection);
                }
            }
        );
    }
    
    private void setupTypeFilter() {
        typeFilter.getItems().add(null);
        typeFilter.getItems().addAll(ActivityType.values());
        
        typeFilter.setButtonCell(new ListCell<ActivityType>() {
            @Override
            protected void updateItem(ActivityType item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "All Types" : item.getDisplayName());
            }
        });
        
        typeFilter.setCellFactory(listView -> new ListCell<ActivityType>() {
            @Override
            protected void updateItem(ActivityType item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "All Types" : item.getDisplayName());
            }
        });
        
        typeFilter.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldValue, newValue) -> filterByType(newValue)
        );
    }
    
    private void setupEventHandlers() {
        updateButton.setOnAction(event -> refreshData());
    }
    
    private void loadActivities() {
        try {
            updateButton.setDisable(true);
            statusLabel.setText("Loading activities...");
            
            List<Activity> activityList = activityService.getAllActiveActivities();
            activities.clear();
            activities.addAll(activityList);
            
            statusLabel.setText("Loaded " + activityList.size() + " activities");
            logger.info("Loaded {} activities", activityList.size());
            
        } catch (Exception e) {
            logger.error("Error loading activities", e);
            statusLabel.setText("Error loading activities");
            UIUtils.showDatabaseErrorDialog(e);
        } finally {
            updateButton.setDisable(false);
        }
    }
    
    public void refreshData() {
        logger.debug("Refreshing activity data");
        loadActivities();
    }
    
    private void filterByType(ActivityType type) {
        try {
            if (type == null) {
                loadActivities();
            } else {
                List<Activity> filtered = activityService.getActivitiesByType(type);
                activities.clear();
                activities.addAll(filtered);
                statusLabel.setText("Showing " + filtered.size() + " " + 
                                  type.getDisplayName().toLowerCase() + " activities");
            }
        } catch (Exception e) {
            logger.error("Error filtering activities by type", e);
            UIUtils.showDatabaseErrorDialog(e);
        }
    }
    
    private void displayActivityDetails(Activity activity) {
        if (activity != null) {
            StringBuilder details = new StringBuilder();
            details.append("Title: ").append(activity.getTitle()).append("\n\n");
            details.append("Type: ").append(activity.getActivityType().getDisplayName()).append("\n");
            details.append("Scheduled: ").append(DateTimeUtil.formatForDisplay(activity.getScheduledDate())).append("\n");
            details.append("Location: ").append(activity.getLocation() != null ? activity.getLocation() : "Not specified").append("\n");
            details.append("Created by: ").append(activity.getCreatedBy().getFullName()).append("\n");
            details.append("Created: ").append(DateTimeUtil.formatForDisplay(activity.getCreatedAt())).append("\n");
            
            if (!activity.getCreatedAt().equals(activity.getUpdatedAt())) {
                details.append("Updated: ").append(DateTimeUtil.formatForDisplay(activity.getUpdatedAt())).append("\n");
                details.append("Updated by: ").append(activity.getUpdatedBy().getFullName()).append("\n");
            }
            
            details.append("\n--- Description ---\n\n");
            details.append(activity.getDescription() != null ? activity.getDescription() : "No description provided");
            
            detailsTextArea.setText(details.toString());
        } else {
            detailsTextArea.clear();
        }
    }
    
    public Node getView() {
        return rootContainer;
    }
    
    @FXML
    private void showUpcoming() {
        try {
            List<Activity> upcoming = activityService.getUpcomingActivities();
            activities.clear();
            activities.addAll(upcoming);
            statusLabel.setText("Showing " + upcoming.size() + " upcoming activities");
            typeFilter.getSelectionModel().clearSelection();
        } catch (Exception e) {
            logger.error("Error loading upcoming activities", e);
            UIUtils.showDatabaseErrorDialog(e);
        }
    }
    
    @FXML
    private void showToday() {
        try {
            List<Activity> today = activityService.getTodaysActivities();
            activities.clear();
            activities.addAll(today);
            statusLabel.setText("Showing " + today.size() + " activities for today");
            typeFilter.getSelectionModel().clearSelection();
        } catch (Exception e) {
            logger.error("Error loading today's activities", e);
            UIUtils.showDatabaseErrorDialog(e);
        }
    }
    
    @FXML
    private void clearSelection() {
        activitiesTable.getSelectionModel().clearSelection();
        detailsTextArea.clear();
    }
    
    @FXML
    private void createActivity() {
        Optional<Activity> result = DialogUtils.showActivityDialog(null, currentUser);
        
        result.ifPresent(activity -> {
            try {
                Activity created = activityService.createActivity(
                    activity.getTitle(),
                    activity.getDescription(),
                    activity.getActivityType(),
                    activity.getScheduledDate(),
                    activity.getLocation(),
                    currentUser
                );
                
                refreshData();
                statusLabel.setText("Activity created successfully");
                logger.info("Created activity: {}", created.getTitle());
                
            } catch (Exception e) {
                logger.error("Error creating activity", e);
                UIUtils.showErrorDialog("Error Creating Activity", 
                    "Failed to create activity: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void editActivity() {
        Activity selected = activitiesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showWarningDialog("No Selection", "Please select an activity to edit.");
            return;
        }
        
        Optional<Activity> result = DialogUtils.showActivityDialog(selected, currentUser);
        
        result.ifPresent(activity -> {
            try {
                Activity updated = activityService.updateActivity(
                    activity.getId(),
                    activity.getTitle(),
                    activity.getDescription(),
                    activity.getActivityType(),
                    activity.getScheduledDate(),
                    activity.getLocation(),
                    currentUser
                );
                
                refreshData();
                statusLabel.setText("Activity updated successfully");
                logger.info("Updated activity: {}", updated.getTitle());
                
            } catch (Exception e) {
                logger.error("Error updating activity", e);
                UIUtils.showErrorDialog("Error Updating Activity", 
                    "Failed to update activity: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void deleteActivity() {
        Activity selected = activitiesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showWarningDialog("No Selection", "Please select an activity to delete.");
            return;
        }
        
        boolean confirmed = DialogUtils.showDeleteConfirmation("activity", selected.getTitle());
        
        if (confirmed) {
            try {
                activityService.deleteActivity(selected.getId());
                refreshData();
                statusLabel.setText("Activity deleted (archived) successfully");
                logger.info("Deleted activity: {}", selected.getTitle());
                
            } catch (Exception e) {
                logger.error("Error deleting activity", e);
                UIUtils.showErrorDialog("Error Deleting Activity", 
                    "Failed to delete activity: " + e.getMessage());
            }
        }
    }
}