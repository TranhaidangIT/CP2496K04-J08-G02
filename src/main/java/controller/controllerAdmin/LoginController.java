package controller.controllerAdmin;

import dao.UserDAO;
import models.User;
import utils.Session;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller handling login for 3 roles: admin, manager, employee.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    /**
     * Handles when the Login button is clicked.
     */
    @FXML
    private void handleLogin(ActionEvent event) throws SQLException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Call DAO to check username & password
        User user = UserDAO.login(username, password);

        if (user != null) {
            // Save user information into session
            Session.setCurrentUser(user);

            // Show notification
            showAlert(AlertType.INFORMATION, "Success", "Welcome " + user.getFullName());

            // Redirect based on role
            switch (user.getRole().toLowerCase()) {
                case "admin":
                    loadScene("/views/AdminDashboard.fxml", event);
                    break;
                case "manager":
                    loadScene("/views/Dashboard.fxml", event);
                    break;
                case "employee":
                    loadScene("/views/EmployeeSidebar.fxml", event);
                    break;
                default:
                    showAlert(AlertType.ERROR, "Error", "Undefined role.");
            }

        } else {
            showAlert(AlertType.ERROR, "Login Failed", "Incorrect username or password.");
        }
    }

    /**
     * Display a popup alert.
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Open a new interface.
     */
    private void loadScene(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Unable to load interface: " + fxmlPath);
        }
    }

    /**
     * Open the Forgot Password interface when clicking "Forgot Password?" link.
     */
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        loadScene("/views/fxml_Admin/ForgotPassword.fxml", event);
    }
}
