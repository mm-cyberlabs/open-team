package com.openteam.controller;

import com.openteam.model.Announcement;
import com.openteam.model.Priority;
import com.openteam.model.User;
import com.openteam.service.AnnouncementService;
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
    @FXML private TableColumn<Announcement, String> archivedColumn;
    @FXML private Button updateButton;
    @FXML private Label statusLabel;
    @FXML private TextArea contentTextArea;
    @FXML private ComboBox<Priority> priorityFilter;
    @FXML private CheckBox showArchivedCheckBox;
    @FXML private SplitPane mainSplitPane;
    
    private final AnnouncementService announcementService;
    private final ObservableList<Announcement> announcements;
    private final User currentUser; // For CRUD operations
    
    public AnnouncementController() {
        this.announcementService = new AnnouncementService();
        this.announcements = FXCollections.observableArrayList();
        // TODO: Get current user from session/authentication
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
        logger.info("Initializing announcement controller");
        
        setupTable();
        setupPriorityFilter();
        setupEventHandlers();
        setupRightPanelToggle();
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
        
        archivedColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getIsArchived() != null && cellData.getValue().getIsArchived() ? "Yes" : "No"
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
        
        // Prevent extra empty columns from appearing
        announcementsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Handle row selection
        announcementsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    displayAnnouncementContent(newSelection);
                    showRightPanel();
                } else {
                    hideRightPanel();
                }
            }
        );
        
        // Allow clicking on selected row to deselect
        announcementsTable.setRowFactory(tv -> {
            TableRow<Announcement> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    if (row.isSelected()) {
                        announcementsTable.getSelectionModel().clearSelection();
                    }
                }
            });
            return row;
        });
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
            mainSplitPane.setDividerPositions(0.6);
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
        contentTextArea.clear();
    }
    
    private void adjustTableColumns(double dividerPosition) {
        // Calculate available width for table based on divider position
        double tableWidthFactor = dividerPosition;
        double baseWidth = 800; // Assume base container width of 800px
        double availableTableWidth = baseWidth * tableWidthFactor;
        
        // Adjust column widths proportionally
        if (availableTableWidth > 600) {
            // Full width - expand columns
            titleColumn.setPrefWidth(300);
            priorityColumn.setPrefWidth(100);
            createdByColumn.setPrefWidth(150);
            createdAtColumn.setPrefWidth(150);
            archivedColumn.setPrefWidth(90);
        } else {
            // Compressed width - shrink columns
            titleColumn.setPrefWidth(250);
            priorityColumn.setPrefWidth(80);
            createdByColumn.setPrefWidth(120);
            createdAtColumn.setPrefWidth(130);
            archivedColumn.setPrefWidth(80);
        }
        
        // Force table to refresh layout
        announcementsTable.refresh();
    }
    
    private void filterByArchiveStatus() {
        loadAnnouncements();
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
        hideRightPanel();
    }
    
    /**
     * Handles creating a new announcement.
     */
    @FXML
    private void createAnnouncement() {
        Optional<Announcement> result = DialogUtils.showAnnouncementDialog(null, currentUser);
        
        result.ifPresent(announcement -> {
            try {
                Announcement created = announcementService.createAnnouncement(
                    announcement.getTitle(),
                    announcement.getContent(),
                    announcement.getPriority(),
                    currentUser
                );
                
                refreshData();
                statusLabel.setText("Announcement created successfully");
                logger.info("Created announcement: {}", created.getTitle());
                
            } catch (Exception e) {
                logger.error("Error creating announcement", e);
                UIUtils.showErrorDialog("Error Creating Announcement", 
                    "Failed to create announcement: " + e.getMessage());
            }
        });
    }
    
    /**
     * Handles editing the selected announcement.
     */
    @FXML
    private void editAnnouncement() {
        Announcement selected = announcementsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showWarningDialog("No Selection", "Please select an announcement to edit.");
            return;
        }
        
        Optional<Announcement> result = DialogUtils.showAnnouncementDialog(selected, currentUser);
        
        result.ifPresent(announcement -> {
            try {
                Announcement updated = announcementService.updateAnnouncement(
                    announcement.getId(),
                    announcement.getTitle(),
                    announcement.getContent(),
                    announcement.getPriority(),
                    currentUser
                );
                
                refreshData();
                statusLabel.setText("Announcement updated successfully");
                logger.info("Updated announcement: {}", updated.getTitle());
                
            } catch (Exception e) {
                logger.error("Error updating announcement", e);
                UIUtils.showErrorDialog("Error Updating Announcement", 
                    "Failed to update announcement: " + e.getMessage());
            }
        });
    }
    
    /**
     * Handles deleting the selected announcement.
     */
    @FXML
    private void deleteAnnouncement() {
        Announcement selected = announcementsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showWarningDialog("No Selection", "Please select an announcement to delete.");
            return;
        }
        
        boolean confirmed = DialogUtils.showDeleteConfirmation("announcement", selected.getTitle());
        
        if (confirmed) {
            try {
                announcementService.deleteAnnouncement(selected.getId());
                refreshData();
                statusLabel.setText("Announcement deleted (archived) successfully");
                logger.info("Deleted announcement: {}", selected.getTitle());
                
            } catch (Exception e) {
                logger.error("Error deleting announcement", e);
                UIUtils.showErrorDialog("Error Deleting Announcement", 
                    "Failed to delete announcement: " + e.getMessage());
            }
        }
    }
}