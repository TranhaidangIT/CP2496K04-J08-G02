package controller.controllerAdmin;

import dao.UserDAO;
import models.User;

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
import java.util.Optional;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    @FXML private TextField searchField;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> fullNameCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, Void> editCol;
    @FXML private TableColumn<User, Void> deleteCol;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nameField;
    @FXML private TextField roleField;
    @FXML private Button addNewButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        loadUserData();
        setupEditButtonColumn();
        setupDeleteButtonColumn();

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
                currentUser = newSelection;
            }
        });
    }

    private void loadUserData() {
        userList.clear();
        userList.addAll(UserDAO.getAllUsers());
        userTable.setItems(userList);
    }

    private void setupEditButtonColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btn = new Button("Edit");

            {
                btn.setOnAction((ActionEvent event) -> {
                    User user = getTableView().getItems().get(getIndex());
                    populateForm(user);
                    currentUser = user;
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

    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText().toLowerCase();
        ObservableList<User> filteredList = userList.filtered(user ->
                user.getUsername().toLowerCase().contains(keyword) ||
                        user.getFullName().toLowerCase().contains(keyword) ||
                        user.getRole().toLowerCase().contains(keyword)
        );
        userTable.setItems(filteredList);
    }

    @FXML
    private void handleAddNew(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml_Admin/AddUserDialog.fxml"));
            Parent root = loader.load();

            AddUserDialogController dialogController = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thêm Người Dùng Mới");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));

            dialogController.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (dialogController.isSaveClicked()) {
                User newUser = dialogController.getNewUser();
                if (UserDAO.insertUser(newUser)) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm người dùng thành công!");
                    loadUserData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Thêm người dùng thất bại.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải giao diện thêm người dùng.");
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (currentUser == null) {
            // Logic thêm người dùng mới
            // Code này đã được chuyển sang AddUserDialogController
        } else {
            // Cập nhật người dùng hiện tại
            currentUser.setUsername(usernameField.getText());
            currentUser.setFullName(nameField.getText());
            currentUser.setRole(roleField.getText());

            // Cập nhật mật khẩu nếu người dùng nhập mật khẩu mới
            if (!passwordField.getText().isEmpty()) {
                String newPassword = passwordField.getText();
                if (UserDAO.updatePasswordByUsername(currentUser.getUsername(), newPassword)) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật mật khẩu thành công!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật mật khẩu thất bại.");
                    return;
                }
            }

            if (UserDAO.updateUser(currentUser)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật người dùng thành công!");
                loadUserData();
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật người dùng thất bại.");
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        clearForm();
        currentUser = null;
        userTable.getSelectionModel().clearSelection();
        loadUserData();
    }

    private void populateForm(User user) {
        usernameField.setText(user.getUsername());
        passwordField.setText("");
        nameField.setText(user.getFullName());
        roleField.setText(user.getRole());
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        nameField.clear();
        roleField.clear();
    }

    private void confirmAndDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Xóa người dùng " + user.getUsername() + "?");
        alert.setContentText("Bạn có chắc chắn muốn xóa người dùng này không?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (UserDAO.deleteUser(user.getUserId())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa người dùng thành công!");
                loadUserData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xóa người dùng thất bại.");
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