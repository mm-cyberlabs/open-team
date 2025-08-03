package com.openteam.controller;

import com.openteam.controller.LoginController.UserSession;
import com.openteam.model.User;
import com.openteam.model.UserRole;
import com.openteam.model.Workspace;
import com.openteam.service.UserManagementService;
import com.openteam.service.WorkspaceService;
import com.openteam.util.UIUtils;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.collections.transformation.FilteredList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the admin panel that handles workspace and user management.
 * Provides role-based access to administrative functions.
 */
public class AdminPanelController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(AdminPanelController.class);
    
    // Tab Pane
    @FXML private TabPane adminTabPane;
    @FXML private Tab workspacesTab;
    @FXML private Tab usersTab;
    
    // Workspace Management
    @FXML private TableView<Workspace> workspaceTable;
    @FXML private TableColumn<Workspace, String> workspaceNameColumn;
    @FXML private TableColumn<Workspace, String> workspaceDescriptionColumn;
    @FXML private TableColumn<Workspace, String> workspaceCreatedColumn;
    @FXML private TableColumn<Workspace, String> workspaceActionsColumn;
    @FXML private MFXButton addWorkspaceButton;
    @FXML private MFXButton refreshWorkspacesButton;
    
    // User Management
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> userUsernameColumn;
    @FXML private TableColumn<User, String> userFullNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, String> userWorkspaceColumn;
    @FXML private TableColumn<User, String> userActionsColumn;
    @FXML private MFXButton addUserButton;
    @FXML private MFXButton refreshUsersButton;
    @FXML private TextField workspaceSearchField;
    @FXML private TextField userSearchField;
    
    // Status
    @FXML private Label statusLabel;
    
    private WorkspaceService workspaceService;
    private UserManagementService userService;
    private User currentUser;
    
    private ObservableList<Workspace> workspaceList;
    private ObservableList<User> userList;
    private FilteredList<Workspace> filteredWorkspaceList;
    private FilteredList<User> filteredUserList;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        workspaceService = new WorkspaceService();
        userService = new UserManagementService();
        workspaceList = FXCollections.observableArrayList();
        userList = FXCollections.observableArrayList();
        
        setupWorkspaceTable();
        setupUserTable();
        setupEventHandlers();
        setupSearchFilters();
    }
    
    /**
     * Sets the current user and configures access based on role.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        configureAccessByRole();
        loadData();
    }
    
    /**
     * Configures UI access based on user role.
     */
    private void configureAccessByRole() {
        if (currentUser == null) {
            return;
        }
        
        // Super admins have access to everything
        if (currentUser.isSuperAdmin()) {
            workspacesTab.setDisable(false);
            usersTab.setDisable(false);
            addWorkspaceButton.setDisable(false);
        }
        // Workspace admins can only manage users in their workspace
        else if (currentUser.getRole() == UserRole.ADMIN) {
            workspacesTab.setDisable(true);
            usersTab.setDisable(false);
            addWorkspaceButton.setDisable(true);
            
            // Switch to users tab if workspaces tab was selected
            if (adminTabPane.getSelectionModel().getSelectedItem() == workspacesTab) {
                adminTabPane.getSelectionModel().select(usersTab);
            }
        }
        // Regular users shouldn't have access to admin panel
        else {
            workspacesTab.setDisable(true);
            usersTab.setDisable(true);
            showStatus("Access denied. Admin privileges required.", "error");
        }
    }
    
    /**
     * Sets up the workspace table columns and properties.
     */
    private void setupWorkspaceTable() {
        workspaceNameColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getName()));
        
        workspaceDescriptionColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getDescription()));
        
        workspaceCreatedColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getCreatedAt()
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
        
        workspaceActionsColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(""));
        workspaceActionsColumn.setCellFactory(column -> new WorkspaceActionCell());
        
        filteredWorkspaceList = new FilteredList<>(workspaceList, p -> true);
        workspaceTable.setItems(filteredWorkspaceList);
    }
    
    /**
     * Sets up the user table columns and properties.
     */
    private void setupUserTable() {
        userUsernameColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getUsername()));
        
        userFullNameColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getFullName()));
        
        userEmailColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getEmail()));
        
        userRoleColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getRole().toString()));
        
        userWorkspaceColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getWorkspace() != null ? 
                cellData.getValue().getWorkspace().getName() : "N/A"));
        
        userActionsColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(""));
        userActionsColumn.setCellFactory(column -> new UserActionCell());
        
        filteredUserList = new FilteredList<>(userList, p -> true);
        userTable.setItems(filteredUserList);
    }
    
    /**
     * Sets up event handlers for buttons.
     */
    private void setupEventHandlers() {
        addWorkspaceButton.setOnAction(e -> showAddWorkspaceDialog());
        refreshWorkspacesButton.setOnAction(e -> loadWorkspaces());
        addUserButton.setOnAction(e -> showAddUserDialog());
        refreshUsersButton.setOnAction(e -> loadUsers());
    }
    
    /**
     * Sets up search filters for workspaces and users.
     */
    private void setupSearchFilters() {
        // Workspace search filter
        if (workspaceSearchField != null) {
            workspaceSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredWorkspaceList.setPredicate(workspace -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    
                    String lowerCaseFilter = newValue.toLowerCase();
                    return workspace.getName().toLowerCase().contains(lowerCaseFilter) ||
                           workspace.getDescription().toLowerCase().contains(lowerCaseFilter);
                });
            });
        }
        
        // User search filter
        if (userSearchField != null) {
            userSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredUserList.setPredicate(user -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    
                    String lowerCaseFilter = newValue.toLowerCase();
                    return user.getUsername().toLowerCase().contains(lowerCaseFilter) ||
                           user.getFullName().toLowerCase().contains(lowerCaseFilter) ||
                           user.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                           user.getRole().toString().toLowerCase().contains(lowerCaseFilter) ||
                           (user.getWorkspace() != null && user.getWorkspace().getName().toLowerCase().contains(lowerCaseFilter));
                });
            });
        }
    }
    
    /**
     * Loads all data based on user permissions.
     */
    private void loadData() {
        if (currentUser.isSuperAdmin() || currentUser.isAdmin()) {
            loadWorkspaces();
            loadUsers();
        }
    }
    
    /**
     * Loads workspaces in background thread.
     */
    private void loadWorkspaces() {
        if (currentUser == null) return;
        
        Task<List<Workspace>> task = new Task<>() {
            @Override
            protected List<Workspace> call() {
                return workspaceService.getAccessibleWorkspaces(currentUser);
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                workspaceList.clear();
                workspaceList.addAll(task.getValue());
                showStatus("Workspaces loaded successfully", "success");
            });
        });
        
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                logger.error("Failed to load workspaces", task.getException());
                showStatus("Failed to load workspaces", "error");
            });
        });
        
        new Thread(task).start();
    }
    
    /**
     * Loads users in background thread.
     */
    private void loadUsers() {
        if (currentUser == null) return;
        
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() {
                return userService.getManageableUsers(currentUser);
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                userList.clear();
                userList.addAll(task.getValue());
                showStatus("Users loaded successfully", "success");
            });
        });
        
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                logger.error("Failed to load users", task.getException());
                showStatus("Failed to load users", "error");
            });
        });
        
        new Thread(task).start();
    }
    
    /**
     * Shows dialog to add a new workspace.
     */
    private void showAddWorkspaceDialog() {
        if (!currentUser.isSuperAdmin()) {
            showStatus("Only super administrators can create workspaces", "error");
            return;
        }
        
        Dialog<Workspace> dialog = new Dialog<>();
        dialog.setTitle("Add New Workspace");
        dialog.setHeaderText("Create a new workspace");
        
        VBox content = new VBox(10);
        content.setPrefWidth(450);
        content.setMinWidth(450);
        MFXTextField nameField = new MFXTextField();
        nameField.setPromptText("Workspace name");
        nameField.setPrefWidth(400);
        MFXTextField descField = new MFXTextField();
        descField.setPromptText("Description");
        descField.setPrefWidth(400);
        
        content.getChildren().addAll(
            new Label("Name:"), nameField,
            new Label("Description:"), descField
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Apply MaterialFX theme to dialog
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/materialfx-theme.css").toExternalForm());
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String name = nameField.getText().trim();
                String description = descField.getText().trim();
                
                if (name.isEmpty()) {
                    showStatus("Workspace name is required", "error");
                    return null;
                }
                
                return workspaceService.createWorkspace(name, description, currentUser);
            }
            return null;
        });
        
        Optional<Workspace> result = dialog.showAndWait();
        result.ifPresent(workspace -> {
            if (workspace != null) {
                workspaceList.add(workspace);
                showStatus("Workspace created successfully", "success");
            } else {
                showStatus("Failed to create workspace", "error");
            }
        });
    }
    
    /**
     * Shows dialog to add a new user.
     */
    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new user account");
        
        VBox content = new VBox(10);
        content.setPrefWidth(500);
        content.setMinWidth(500);
        MFXTextField usernameField = new MFXTextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(450);
        MFXTextField fullNameField = new MFXTextField();
        fullNameField.setPromptText("Full name");
        fullNameField.setPrefWidth(450);
        MFXTextField emailField = new MFXTextField();
        emailField.setPromptText("Email address");
        emailField.setPrefWidth(450);
        MFXPasswordField passwordField = new MFXPasswordField();
        passwordField.setPromptText("Initial password");
        passwordField.setPrefWidth(450);
        
        MFXComboBox<UserRole> roleCombo = new MFXComboBox<>();
        roleCombo.setPrefWidth(450);
        if (currentUser.isSuperAdmin()) {
            roleCombo.getItems().addAll(UserRole.ADMIN, UserRole.USER);
        } else {
            roleCombo.getItems().add(UserRole.USER);
        }
        roleCombo.setValue(UserRole.USER);
        
        MFXComboBox<Workspace> workspaceCombo = new MFXComboBox<>();
        workspaceCombo.setPrefWidth(450);
        if (currentUser.isSuperAdmin()) {
            workspaceCombo.getItems().addAll(workspaceList);
        } else if (currentUser.getWorkspace() != null) {
            workspaceCombo.getItems().add(currentUser.getWorkspace());
            workspaceCombo.setValue(currentUser.getWorkspace());
        }
        
        content.getChildren().addAll(
            new Label("Username:"), usernameField,
            new Label("Full Name:"), fullNameField,
            new Label("Email:"), emailField,
            new Label("Password:"), passwordField,
            new Label("Role:"), roleCombo,
            new Label("Workspace:"), workspaceCombo
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Apply MaterialFX theme to dialog
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/materialfx-theme.css").toExternalForm());
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String username = usernameField.getText().trim();
                String fullName = fullNameField.getText().trim();
                String email = emailField.getText().trim();
                String password = passwordField.getText();
                UserRole role = roleCombo.getValue();
                Workspace workspace = workspaceCombo.getValue();
                
                if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
                    showStatus("All fields are required", "error");
                    return null;
                }
                
                if (workspace == null && role != UserRole.SUPER_ADMIN) {
                    showStatus("Workspace is required for non-super-admin users", "error");
                    return null;
                }
                
                return userService.createUser(username, fullName, email, password, 
                    role, workspace != null ? workspace.getId() : null, currentUser);
            }
            return null;
        });
        
        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            if (user != null) {
                userList.add(user);
                showStatus("User created successfully", "success");
            } else {
                showStatus("Failed to create user", "error");
            }
        });
    }
    
    /**
     * Shows a status message.
     */
    private void showStatus(String message, String type) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.getStyleClass().removeAll("status-error", "status-success", "status-info");
            statusLabel.getStyleClass().add("status-" + type);
            
            // Auto-hide success messages after 3 seconds
            if ("success".equals(type)) {
                Platform.runLater(() -> {
                    Task<Void> hideTask = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            Thread.sleep(3000);
                            return null;
                        }
                    };
                    hideTask.setOnSucceeded(e -> statusLabel.setText(""));
                    new Thread(hideTask).start();
                });
            }
        }
    }
    
    /**
     * Custom table cell for workspace actions.
     */
    private class WorkspaceActionCell extends TableCell<Workspace, String> {
        private final HBox actionBox = new HBox(5);
        private final MFXButton editButton = new MFXButton("Edit");
        private final MFXButton deleteButton = new MFXButton("Delete");
        
        public WorkspaceActionCell() {
            editButton.getStyleClass().add("small-button");
            deleteButton.getStyleClass().add("small-button");
            deleteButton.getStyleClass().add("danger-button");
            
            editButton.setOnAction(e -> editWorkspace(getTableRow().getItem()));
            deleteButton.setOnAction(e -> deleteWorkspace(getTableRow().getItem()));
            
            actionBox.getChildren().addAll(editButton, deleteButton);
        }
        
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || getTableRow().getItem() == null) {
                setGraphic(null);
            } else {
                // Only super admins can delete workspaces
                deleteButton.setVisible(currentUser != null && currentUser.isSuperAdmin());
                setGraphic(actionBox);
            }
        }
        
        private void editWorkspace(Workspace workspace) {
            if (workspace == null || !currentUser.isSuperAdmin()) {
                showStatus("Only super administrators can edit workspaces", "error");
                return;
            }
            
            Dialog<Workspace> dialog = new Dialog<>();
            dialog.setTitle("Edit Workspace");
            dialog.setHeaderText("Edit workspace: " + workspace.getName());
            
            VBox content = new VBox(10);
            content.setPrefWidth(450);
            content.setMinWidth(450);
            MFXTextField nameField = new MFXTextField();
            nameField.setText(workspace.getName());
            nameField.setPromptText("Workspace name");
            nameField.setPrefWidth(400);
            MFXTextField descField = new MFXTextField();
            descField.setText(workspace.getDescription());
            descField.setPromptText("Description");
            descField.setPrefWidth(400);
            
            content.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Description:"), descField
            );
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Apply MaterialFX theme to dialog
            dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/materialfx-theme.css").toExternalForm());
            
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    String name = nameField.getText().trim();
                    String description = descField.getText().trim();
                    
                    if (name.isEmpty()) {
                        showStatus("Workspace name is required", "error");
                        return null;
                    }
                    
                    return workspaceService.updateWorkspace(workspace.getId(), name, description, currentUser);
                }
                return null;
            });
            
            Optional<Workspace> result = dialog.showAndWait();
            result.ifPresent(updatedWorkspace -> {
                if (updatedWorkspace != null) {
                    // Update the workspace in the list
                    int index = workspaceList.indexOf(workspace);
                    if (index >= 0) {
                        workspaceList.set(index, updatedWorkspace);
                    }
                    showStatus("Workspace updated successfully", "success");
                } else {
                    showStatus("Failed to update workspace", "error");
                }
            });
        }
        
        private void deleteWorkspace(Workspace workspace) {
            if (workspace == null || !currentUser.isSuperAdmin()) return;
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Workspace");
            alert.setHeaderText("Are you sure you want to delete this workspace?");
            alert.setContentText("Workspace: " + workspace.getName());
            
            // Apply MaterialFX theme to dialog
            alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/materialfx-theme.css").toExternalForm());
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (workspaceService.deleteWorkspace(workspace.getId(), currentUser)) {
                    workspaceList.remove(workspace);
                    showStatus("Workspace deleted successfully", "success");
                } else {
                    showStatus("Failed to delete workspace", "error");
                }
            }
        }
    }
    
    /**
     * Custom table cell for user actions.
     */
    private class UserActionCell extends TableCell<User, String> {
        private final HBox actionBox = new HBox(5);
        private final MFXButton editButton = new MFXButton("Edit");
        private final MFXButton resetPasswordButton = new MFXButton("Reset Password");
        private final MFXButton deactivateButton = new MFXButton("Deactivate");
        
        public UserActionCell() {
            editButton.getStyleClass().add("small-button");
            resetPasswordButton.getStyleClass().add("small-button");
            deactivateButton.getStyleClass().add("small-button");
            deactivateButton.getStyleClass().add("danger-button");
            
            editButton.setOnAction(e -> editUser(getTableRow().getItem()));
            resetPasswordButton.setOnAction(e -> resetUserPassword(getTableRow().getItem()));
            deactivateButton.setOnAction(e -> deactivateUser(getTableRow().getItem()));
            
            actionBox.getChildren().addAll(editButton, resetPasswordButton, deactivateButton);
        }
        
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || getTableRow().getItem() == null) {
                setGraphic(null);
            } else {
                User user = getTableRow().getItem();
                // Don't show deactivate button for current user
                deactivateButton.setVisible(!user.getId().equals(currentUser.getId()));
                setGraphic(actionBox);
            }
        }
        
        private void editUser(User user) {
            if (user == null) return;
            
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Edit User");
            dialog.setHeaderText("Edit user: " + user.getUsername());
            
            VBox content = new VBox(10);
            content.setPrefWidth(500);
            content.setMinWidth(500);
            MFXTextField usernameField = new MFXTextField();
            usernameField.setText(user.getUsername());
            usernameField.setPromptText("Username");
            usernameField.setDisable(true); // Username shouldn't be editable
            usernameField.setPrefWidth(450);
            
            MFXTextField fullNameField = new MFXTextField();
            fullNameField.setText(user.getFullName());
            fullNameField.setPromptText("Full name");
            fullNameField.setPrefWidth(450);
            
            MFXTextField emailField = new MFXTextField();
            emailField.setText(user.getEmail());
            emailField.setPromptText("Email address");
            emailField.setPrefWidth(450);
            
            MFXComboBox<UserRole> roleCombo = new MFXComboBox<>();
            roleCombo.setPrefWidth(450);
            if (currentUser.isSuperAdmin()) {
                roleCombo.getItems().addAll(UserRole.ADMIN, UserRole.USER);
            } else {
                roleCombo.getItems().add(UserRole.USER);
            }
            roleCombo.setValue(user.getRole());
            
            MFXComboBox<Workspace> workspaceCombo = new MFXComboBox<>();
            workspaceCombo.setPrefWidth(450);
            if (currentUser.isSuperAdmin()) {
                workspaceCombo.getItems().addAll(workspaceList);
            } else if (currentUser.getWorkspace() != null) {
                workspaceCombo.getItems().add(currentUser.getWorkspace());
            }
            workspaceCombo.setValue(user.getWorkspace());
            
            content.getChildren().addAll(
                new Label("Username:"), usernameField,
                new Label("Full Name:"), fullNameField,
                new Label("Email:"), emailField,
                new Label("Role:"), roleCombo,
                new Label("Workspace:"), workspaceCombo
            );
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Apply MaterialFX theme to dialog
            dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/materialfx-theme.css").toExternalForm());
            
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    String fullName = fullNameField.getText().trim();
                    String email = emailField.getText().trim();
                    UserRole role = roleCombo.getValue();
                    Workspace workspace = workspaceCombo.getValue();
                    
                    if (fullName.isEmpty()) {
                        showStatus("Full name is required", "error");
                        return null;
                    }
                    
                    if (workspace == null && role != UserRole.SUPER_ADMIN) {
                        showStatus("Workspace is required for non-super-admin users", "error");
                        return null;
                    }
                    
                    return userService.updateUser(user.getId(), fullName, email, role, 
                        workspace != null ? workspace.getId() : null, currentUser);
                }
                return null;
            });
            
            Optional<User> result = dialog.showAndWait();
            result.ifPresent(updatedUser -> {
                if (updatedUser != null) {
                    // Update the user in the list
                    int index = userList.indexOf(user);
                    if (index >= 0) {
                        userList.set(index, updatedUser);
                    }
                    showStatus("User updated successfully", "success");
                } else {
                    showStatus("Failed to update user", "error");
                }
            });
        }
        
        private void resetUserPassword(User user) {
            if (user == null) return;
            
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Reset Password");
            dialog.setHeaderText("Reset password for " + user.getFullName());
            dialog.setContentText("New password:");
            
            // Apply MaterialFX theme to dialog
            dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/materialfx-theme.css").toExternalForm());
            
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(password -> {
                if (userService.resetUserPassword(user.getId(), password, currentUser)) {
                    showStatus("Password reset successfully", "success");
                } else {
                    showStatus("Failed to reset password", "error");
                }
            });
        }
        
        private void deactivateUser(User user) {
            if (user == null || user.getId().equals(currentUser.getId())) return;
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Deactivate User");
            alert.setHeaderText("Are you sure you want to deactivate this user?");
            alert.setContentText("User: " + user.getFullName() + " (" + user.getUsername() + ")");
            
            // Apply MaterialFX theme to dialog
            alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/materialfx-theme.css").toExternalForm());
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (userService.deactivateUser(user.getId(), currentUser)) {
                    userList.remove(user);
                    showStatus("User deactivated successfully", "success");
                } else {
                    showStatus("Failed to deactivate user", "error");
                }
            }
        }
    }
}