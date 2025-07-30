package com.openteam.controller;

import com.openteam.model.Announcement;
import com.openteam.model.Priority;
import com.openteam.service.AnnouncementService;
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
 * Controller for the announcements view.
 * Manages display and interaction with team announcements.
 */
public class AnnouncementController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(AnnouncementController.class);
    
    @FXML private VBox rootContainer;
    @FXML private TableView<Announcement> announcementsTable;
    @FXML private TableColumn<Announcement, String> titleColumn;
    @FXML private TableColumn<Announcement, String> priorityColumn;
    @FXML private TableColumn<Announcement, String> createdByColumn;
    @FXML private TableColumn<Announcement, String> createdAtColumn;
    @FXML private Button updateButton;
    @FXML private Label statusLabel;
    @FXML private TextArea contentTextArea;
    @FXML private ComboBox<Priority> priorityFilter;
    
    private final AnnouncementService announcementService;
    private final ObservableList<Announcement> announcements;
    
    public AnnouncementController() {
        this.announcementService = new AnnouncementService();
        this.announcements = FXCollections.observableArrayList();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("Initializing announcement controller");
        
        setupTable();
        setupPriorityFilter();
        setupEventHandlers();
        loadAnnouncements();
    }
    
    /**
     * Sets up the announcements table columns and behavior.
     */
    private void setupTable() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priorityColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getPriority().getDisplayName()
            )
        );
        createdByColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCreatedBy().getFullName()
            )
        );
        createdAtColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                DateTimeUtil.formatForDisplay(cellData.getValue().getCreatedAt())
            )
        );
        
        // Set custom cell factory for priority column to show colors
        priorityColumn.setCellFactory(column -> new TableCell<Announcement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Announcement announcement = getTableView().getItems().get(getIndex());
                    Priority priority = announcement.getPriority();
                    setStyle("-fx-text-fill: " + priority.getColorCode() + ";");
                }
            }
        });
        
        announcementsTable.setItems(announcements);
        
        // Handle row selection
        announcementsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    displayAnnouncementContent(newSelection);
                }
            }
        );
    }
    
    /**
     * Sets up the priority filter combo box.
     */
    private void setupPriorityFilter() {
        priorityFilter.getItems().add(null); // Add "All" option
        priorityFilter.getItems().addAll(Priority.values());
        
        priorityFilter.setButtonCell(new ListCell<Priority>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "All Priorities" : item.getDisplayName());
            }
        });
        
        priorityFilter.setCellFactory(listView -> new ListCell<Priority>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "All Priorities" : item.getDisplayName());
            }
        });
        
        priorityFilter.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldValue, newValue) -> filterByPriority(newValue)
        );
    }
    
    /**
     * Sets up event handlers for buttons.
     */
    private void setupEventHandlers() {
        updateButton.setOnAction(event -> refreshData());
    }
    
    /**
     * Loads announcements from the service.
     */
    private void loadAnnouncements() {
        try {
            updateButton.setDisable(true);
            statusLabel.setText("Loading announcements...");
            
            List<Announcement> announcementList = announcementService.getAllActiveAnnouncements();
            announcements.clear();
            announcements.addAll(announcementList);
            
            statusLabel.setText("Loaded " + announcementList.size() + " announcements");
            logger.info("Loaded {} announcements", announcementList.size());
            
        } catch (Exception e) {
            logger.error("Error loading announcements", e);
            statusLabel.setText("Error loading announcements");
            UIUtils.showDatabaseErrorDialog(e);
        } finally {
            updateButton.setDisable(false);
        }
    }
    
    /**
     * Refreshes the announcement data.
     */
    public void refreshData() {
        logger.debug("Refreshing announcement data");
        loadAnnouncements();
    }
    
    /**
     * Filters announcements by priority.
     * 
     * @param priority Priority to filter by (null for all)
     */
    private void filterByPriority(Priority priority) {
        try {
            if (priority == null) {
                loadAnnouncements();
            } else {
                List<Announcement> filtered = announcementService.getAnnouncementsByPriority(priority);
                announcements.clear();
                announcements.addAll(filtered);
                statusLabel.setText("Showing " + filtered.size() + " " + 
                                  priority.getDisplayName().toLowerCase() + " priority announcements");
            }
        } catch (Exception e) {
            logger.error("Error filtering announcements by priority", e);
            UIUtils.showDatabaseErrorDialog(e);
        }
    }
    
    /**
     * Displays the content of the selected announcement.
     * 
     * @param announcement Selected announcement
     */
    private void displayAnnouncementContent(Announcement announcement) {
        if (announcement != null) {
            StringBuilder content = new StringBuilder();
            content.append("Title: ").append(announcement.getTitle()).append("\n\n");
            content.append("Priority: ").append(announcement.getPriority().getDisplayName()).append("\n");
            content.append("Created by: ").append(announcement.getCreatedBy().getFullName()).append("\n");
            content.append("Created: ").append(DateTimeUtil.formatForDisplay(announcement.getCreatedAt())).append("\n");
            
            if (!announcement.getCreatedAt().equals(announcement.getUpdatedAt())) {
                content.append("Updated: ").append(DateTimeUtil.formatForDisplay(announcement.getUpdatedAt())).append("\n");
                content.append("Updated by: ").append(announcement.getUpdatedBy().getFullName()).append("\n");
            }
            
            content.append("\n--- Content ---\n\n");
            content.append(announcement.getContent());
            
            contentTextArea.setText(content.toString());
        } else {
            contentTextArea.clear();
        }
    }
    
    /**
     * Returns the root view node for this controller.
     * 
     * @return Root container node
     */
    public Node getView() {
        return rootContainer;
    }
    
    /**
     * Handles the show high priority button action.
     */
    @FXML
    private void showHighPriority() {
        try {
            List<Announcement> highPriority = announcementService.getHighPriorityAnnouncements();
            announcements.clear();
            announcements.addAll(highPriority);
            statusLabel.setText("Showing " + highPriority.size() + " high priority announcements");
            priorityFilter.getSelectionModel().clearSelection();
        } catch (Exception e) {
            logger.error("Error loading high priority announcements", e);
            UIUtils.showDatabaseErrorDialog(e);
        }
    }
    
    /**
     * Handles the clear selection button action.
     */
    @FXML
    private void clearSelection() {
        announcementsTable.getSelectionModel().clearSelection();
        contentTextArea.clear();
    }
}