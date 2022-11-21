package client.Network;

import Server.AbstractObjects.*;
import client.UI.ClientApp;
import client.UI.SceneController;
import constants.Constants;
import javafx.application.Platform;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public class CallbackImpl implements Callback {
    @Override
    public void onReceived(ByteObject object) {
        String className = object.getClass().getSimpleName();
        try {
            switch (className) {
                case "StringObject":
                    processStringMessage(object);
                    break;
                case "StorageListObject":
                    processStorageList(object);
                    break;
                case "FileObject":
                    downloadFile(object);
                    break;
                case "DirectoryObject":
                    downloadDirectory(object);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processStringMessage(ByteObject object) {
        StringObject stringObject = (StringObject) object;
        String command = stringObject.getMessage();

        switch (command) {
            case "#Пользователь не существует#":
                Platform.runLater(() -> ClientApp.getLoginPageController().userNotExist());
                break;
            case "#Неверный пароль#":
                Platform.runLater(() -> ClientApp.getLoginPageController().wrongPassword());
                break;
            case Constants.AUTH_SUCCESSFUL:
                Platform.runLater(() -> ClientApp.getLoginPageController().authCorrect());
                break;
            case "#Такой пользователь уже зарегистрирован#":
                Platform.runLater(() -> SceneController.getRegistrationPageController().userPresentInDb());
                break;
            case "#Регистрация успешна#":
                Platform.runLater(() -> SceneController.getRegistrationPageController().registrationSuccessful());
                break;
            case "#Регистрация временно не работает#":
                Platform.runLater(() -> SceneController.getRegistrationPageController().registrationProblem());
                break;
            case "#Файлы уже существуют#":
                Platform.runLater(() -> SceneController.getMainPageController().displayResultLabel("Файлы уже существует в хранилище"));
                break;
            case "#Папка уже существует#":
                Platform.runLater(() -> SceneController.getMainPageController().displayResultLabel("Папка уже существует в хранилище"));
                break;
            case "#Загрузка файлов завершена#":
                Platform.runLater(() -> SceneController.getMainPageController().displayResultLabel("Загрузка файлов завершена"));
                break;
            case "#Файлы удалены#":
                Platform.runLater(() -> SceneController.getMainPageController().displayResultLabel("Файлы удалены"));
                break;
        }
    }

    private void downloadDirectory(ByteObject object) throws IOException {
        DirectoryObject sendDirectory = (DirectoryObject) object;
        String fileName = sendDirectory.getFileName();
        String fileFolders = sendDirectory.getFileFolders();
        byte[] fileData = sendDirectory.getFile();

        String userHome = System.getProperty("user.home");
        String downloadsFolder = userHome + "/" + "Downloads";

        Path foldersPath = Paths.get(downloadsFolder + "/" + fileFolders);
        if (Files.notExists(foldersPath)) {
            Files.createDirectories(foldersPath);
        }
        Path filePath = Paths.get(foldersPath + "/" + fileName);
        if (Files.notExists(filePath)) {
            Files.createFile(filePath);
            Files.write(filePath, fileData);
        } else {
            System.out.println("такой файл уже существует ");
        }
    }

    private void downloadFile(ByteObject object) throws IOException {
        FileObject fileObject = (FileObject) object;
        String fileName = fileObject.getFileName();
        byte[] fileData = fileObject.getFile();

        String userHome = System.getProperty("user.home");
        String downloadsFolder = userHome + "/" + "Downloads";

        Path downloadFilePath = Paths.get(downloadsFolder + "/" + fileName);
        if (Files.notExists(downloadFilePath)) {
            Files.createFile(downloadFilePath);
            Files.write(downloadFilePath, fileData);
        } else {
            System.out.println("Тайкой файл уже существует хранилище");
        }
    }

    private void processStorageList(ByteObject object) {
        StorageListObject storageList = (StorageListObject) object;
        String[] clientContent = storageList.getClientContent();
        Platform.runLater(() -> SceneController.getMainPageController().displayContent(clientContent));
    }
}
