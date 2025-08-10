package controller.controllerAdmin;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.User;

import java.sql.SQLException;

public class EditUserDialogController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private Label roleLabel;
    @FXML
    private PasswordField passwordField;

    private User user;
    private Stage dialogStage;
    private boolean isSaved = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            usernameField.setText(user.getUsername());
            fullNameField.setText(user.getFullName());
            emailField.setText(user.getEmail());
            roleLabel.setText(user.getRole());
            passwordField.setText("");
        }
    }

    public boolean isSaved() {
        return isSaved;
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            // Update basic information
            user.setUsername(usernameField.getText());
            user.setFullName(fullNameField.getText());
            user.setEmail(emailField.getText());

            try {
                // Call UserDAO to update user information
                boolean success = UserDAO.updateUser(user);

                // Check and update password if there is a change
                String newPassword = passwordField.getText();
                if (!newPassword.isEmpty()) {
                    boolean passwordSuccess = UserDAO.updatePasswordByUsername(user.getUsername(), newPassword);
                    if (!passwordSuccess) {
                        showAlert("Error", "Password update failed. Please try again.");
                        return;
                    }
                }

                if (success) {
                    isSaved = true;
                    dialogStage.close();
                } else {
                    showAlert("Error", "Updating user information failed. Please try again.");
                }
            } catch (SQLException e) {
                showAlert("Database Error", "An error occurred while updating the user: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";
        if (usernameField.getText() == null || usernameField.getText().isEmpty()) {
            errorMessage += "Username cannot be empty!\n";
        }
        if (fullNameField.getText() == null || fullNameField.getText().isEmpty()) {
            errorMessage += "Full name cannot be empty!\n";
        }
        if (emailField.getText() == null || emailField.getText().isEmpty()) {
            errorMessage += "Email cannot be empty!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert("Invalid Data", errorMessage);
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
