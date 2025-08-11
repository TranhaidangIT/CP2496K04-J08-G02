package controller.controllerAdmin;

import models.User;
import dao.UserDAO;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;

public class AddUserDialogController {

    @FXML private TextField usernameField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> roleComboBox;

    // Add new fields for security question and answer
    @FXML private TextField securityQuestionField;
    @FXML private TextField securityAnswerField;

    private Stage dialogStage;
    private boolean saveClicked = false;
    private User newUser;

    @FXML
    private void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("Admin", "Manager", "Employee"));
        roleComboBox.getSelectionModel().select("Employee");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public User getNewUser() {
        return newUser;
    }

    private String generateEmployeeId(String role) throws SQLException {
        String prefix = "";
        switch (role) {
            case "Admin":
                prefix = "ADM";
                break;
            case "Manager":
                prefix = "MNE";
                break;
            case "Employee":
                prefix = "EMP";
                break;
            default:
                String uuid = UUID.randomUUID().toString().replace("-", "");
                return uuid.substring(0, 10).toUpperCase();
        }

        String newId;
        Random random = new Random();
        do {
            int number = 100 + random.nextInt(900);
            newId = prefix + number;
        } while (UserDAO.isEmployeeIdExists(newId));

        return newId;
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            newUser = new User();
            try {
                String role = roleComboBox.getValue();
                String employeeId = generateEmployeeId(role);

                newUser.setEmployeeId(employeeId);
                newUser.setUsername(usernameField.getText());
                newUser.setPassword("123");
                newUser.setFullName(nameField.getText());
                newUser.setEmail(emailField.getText());
                newUser.setPhone(phoneField.getText());
                newUser.setRole(role);
                // Get values from new fields and assign them to the User object
                newUser.setSecurityQuestion(securityQuestionField.getText());
                newUser.setSecurityAnswer(securityAnswerField.getText());

                saveClicked = true;
                dialogStage.close();

            } catch (SQLException e) {
                showAlert("Database Error", "An error occurred while generating EmployeeId.");
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
            errorMessage += "No username provided!\n";
        }
        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            errorMessage += "No full name provided!\n";
        }
        if (emailField.getText() == null || emailField.getText().isEmpty()) {
            errorMessage += "No email provided!\n";
        }
        if (phoneField.getText() == null || phoneField.getText().isEmpty()) {
            errorMessage += "No phone number provided!\n";
        }
        if (roleComboBox.getValue() == null) {
            errorMessage += "No role selected!\n";
        }
        // Check new fields
        if (securityQuestionField.getText() == null || securityQuestionField.getText().isEmpty()) {
            errorMessage += "No security question provided!\n";
        }
        if (securityAnswerField.getText() == null || securityAnswerField.getText().isEmpty()) {
            errorMessage += "No security answer provided!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Information");
            alert.setHeaderText("Please fill in all required fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialogStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
