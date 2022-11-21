package client.UI;

import Server.AbstractObjects.StringObject;
import client.Network.NettyNet;
import constants.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.TextAlignment;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;

public class LoginPageController implements Initializable {
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label loginLabel;
    private SceneController sceneController;
    private String userName;
    private static String trueUserName;
    private static NettyNet net;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        net = new NettyNet();
        sceneController = new SceneController();
        setOnEnterPressed();
    }

    private void setOnEnterPressed() {
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendUserNameAndPassword();
            }
        });
    }

    public void sendUserNameAndPassword() {
        userName = usernameTextField.getText();
        String password = passwordField.getText();
        if (isCorrectInput(userName, password)) {
            net.sendObject((new StringObject(Constants.AUTH_COMMAND + " " + userName + " " + password)));
        }
    }

    private void displayLoginLabel(String string) {
        loginLabel.setStyle("-fx-border-color: red;");
        loginLabel.setWrapText(true);
        loginLabel.setTextAlignment(TextAlignment.JUSTIFY);
        loginLabel.setText(string);
    }

    private boolean isCorrectInput(String userName, String password) {
        if (userName.length() == 0) {
            displayLoginLabel("Введите имя пользователя");
            return false;
        }
        if (password.length() == 0) {
            displayLoginLabel("Введите пароль");
            return false;
        }
        if (userName.length() < 6) {
            displayLoginLabel("Длинна имени должна быть не меньше 6 символов");
            return false;
        }
        if (password.length() < 6) {
            displayLoginLabel("Длинна пароля должна быть не меньше 6 символов");
            return false;
        }
        if (userName.equals(password)) {
            displayLoginLabel("Имя пользователя и пароль не должны совпадать");
            return false;
        }
        return true;
    }

    public void userNotExist() {
        displayLoginLabel("Пользователь с таким именем не существует");
    }

    public void wrongPassword() {
        displayLoginLabel("Неверный пароль");
    }

    public void authCorrect() {
        loginLabel.getScene().getWindow().hide();
        trueUserName = userName;
        try {
            sceneController.switchToMainPage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createAccount(ActionEvent actionEvent) throws IOException {
        sceneController.switchToRegistrationPage(actionEvent);
    }

    public static NettyNet getNet() {
        return net;
    }

    public static String getTrueUserName() {
        return trueUserName;
    }


}
