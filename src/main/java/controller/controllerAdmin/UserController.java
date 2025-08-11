package controller.controllerAdmin;

import dao.UserDAO;
import models.User;
import utils.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    @FXML private TextField searchField;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> fullNameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, Void> editCol;
    @FXML private TableColumn<User, Void> deleteCol;
    @FXML private Button addNewButton;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        loadUserData();
        setupEditButtonColumn();
        setupDeleteButtonColumn();
    }

    private void loadUserData() {
        userList.clear();
        try {
            userList.addAll(UserDAO.getAllUsers());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Data Load Error", "Unable to load user data from the database.");
            e.printStackTrace();
        }
        userTable.setItems(userList);
    }

    private void setupEditButtonColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btn = new Button("Edit");
            {
                btn.setOnAction((ActionEvent event) -> {
                    User userToEdit = getTableView().getItems().get(getIndex());
                    openEditUserDialog(userToEdit);
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        };
        editCol.setCellFactory(cellFactory);
    }

    private void setupDeleteButtonColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btn = new Button("Delete");

            {
                btn.setOnAction((ActionEvent event) -> {
                    User user = getTableView().getItems().get(getIndex());
                    confirmAndDeleteUser(user);
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        };
        deleteCol.setCellFactory(cellFactory);
    }

    @FXML private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText().toLowerCase();
        ObservableList<User> filteredList = userList.filtered(user ->
                user.getUsername().toLowerCase().contains(keyword) ||
                        user.getFullName().toLowerCase().contains(keyword) ||
                        user.getRole().toLowerCase().contains(keyword)
        );
        userTable.setItems(filteredList);
    }

    @FXML private void handleAddNew(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Admin/AddUserDialog.fxml"));
            Parent root = loader.load();

            AddUserDialogController dialogController = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New User");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogController.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            if (dialogController.isSaveClicked()) {
                User newUser = dialogController.getNewUser();
                try {
                    if (UserDAO.addUser(newUser)) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "User added successfully!");
                        loadUserData();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to add user.");
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to add user to the database.");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load Add User interface.");
        }
    }

    private void openEditUserDialog(User userToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Admin/EditUserDialog.fxml"));
            Parent root = loader.load();

            EditUserDialogController dialogController = loader.getController();
            dialogController.setUser(userToEdit);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogController.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            loadUserData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load Edit User interface.");
        }
    }

    private void confirmAndDeleteUser(User user) {
        User currentUser = Session.getCurrentUser();
        if (currentUser != null && user.getUserId() == currentUser.getUserId()) {
            showAlert(Alert.AlertType.ERROR, "Delete User Error", "You cannot delete your own account!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Delete user " + user.getUsername() + "?");
        alert.setContentText("Are you sure you want to delete this user?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (UserDAO.deleteUser(user.getUserId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully!");
                    loadUserData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to delete user.");
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
