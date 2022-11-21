package client.UI;

import Server.AbstractObjects.StringObject;
import client.Network.NettyNet;
import constants.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.TextAlignment;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.fxml.FXML;

public class RegistrationPageController implements Initializable {
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private TextField repeatPasswordTextField;
    @FXML
    private Label signInLabel;
    private SceneController sceneController;
    private NettyNet net;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        net = LoginPageController.getNet();
        sceneController = new SceneController();
        setOnEnterPressed();
    }

    private void setOnEnterPressed() {
        repeatPasswordTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                performRegistration();
            }
        });
    }

    public void performRegistration() {
        String userName = usernameTextField.getText();
        String password = passwordTextField.getText();
        if (isCorrectInput(userName, password)) {
            net.sendObject((new StringObject(Constants.REGISTRATION_COMMAND + " " + userName + " " + password)));

        }
    }

    private void displaySignInLabel(String string) {
        signInLabel.setStyle("-fx-border-color: red;");
        signInLabel.setWrapText(true);
        signInLabel.setTextAlignment(TextAlignment.JUSTIFY);
        signInLabel.setText(string);
    }

    private boolean isCorrectInput(String userName, String password) {
        if (userName.length() == 0) {
            displaySignInLabel("Введите имя пользователя");
            return false;
        }
        if (password.length() == 0) {
            displaySignInLabel("Введите пароль");
            return false;
        }
        if (userName.length() < 6) {
            displaySignInLabel("Длинна имени должна быть не меньше 6 символов");
            return false;
        }
        if (password.length() < 6) {
            displaySignInLabel("Длинна пароля должна быть не меньше 6 символов");
            return false;
        }
        if (userName.equals(password)) {
            displaySignInLabel("Имя пользователя и пароль не должны совпадать");
            return false;
        }
        if (!(passwordTextField.getText().equals(repeatPasswordTextField.getText()))) {
            displaySignInLabel("Введенные пароли не совпадают");
            return false;
        }
        return true;
    }

    public void userPresentInDb() {
        displaySignInLabel("Пользователь с таким именем уже существует");
    }

    public void registrationSuccessful() {
        signInLabel.setStyle("-fx-border-color: green;");
        signInLabel.setWrapText(true);
        signInLabel.setTextAlignment(TextAlignment.JUSTIFY);
        signInLabel.setText("Регистрация успешна!");
        try {
            Files.createDirectory(Paths.get("cloudUsers" + "/" + usernameTextField.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registrationProblem() {
        displaySignInLabel("Регистрация временно не работает");
    }

    public void clearButtonMethod() {
        usernameTextField.setText("");
        passwordTextField.setText("");
        repeatPasswordTextField.setText("");
        signInLabel.setText("");
        signInLabel.setStyle("-fx-border-color: ;");
    }

    public void goToLoginPage(ActionEvent event) throws IOException {
        sceneController.switchToLoginPage(event);
    }
}
