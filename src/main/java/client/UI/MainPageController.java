package client.UI;


import Server.AbstractObjects.DirectoryObject;
import Server.AbstractObjects.FileObject;
import Server.AbstractObjects.StringObject;
import client.Network.NettyNet;
import constants.Constants;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import javafx.fxml.FXML;

public class MainPageController implements Initializable {
    @FXML
    private Label resultLabel;
    @FXML
    private MenuButton menuButton;
    @FXML
    private Label pathLabel;
    @FXML
    private BorderPane borderPane;
    @FXML
    private ListView<String> listView;
    private List<File> files;
    private NettyNet net;
    private File selectedDirectory;
    private Image folder;
    private Image file;
    private String userName;
    private static final int slash = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        net = LoginPageController.getNet();
        userName = LoginPageController.getTrueUserName();
        pathLabel.setText(userName);

        folder = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/folder.png")));
        file = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/file.png")));
        Image plusImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/plus.png")));

        ImageView view = new ImageView(plusImage);
        view.setFitHeight(30);
        view.setPreserveRatio(true);
        menuButton.setGraphic(view);

        setDoubleClick();
        setIconsAndContextMenu();
    }

    private void setDoubleClick() {
        listView.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                String folderName = listView.getSelectionModel().getSelectedItem();
                if (!folderName.contains(".")) {
                    pathLabel.setText(pathLabel.getText() + "/" + folderName);
                    String folderPath = pathLabel.getText();
                    net.sendObject(new StringObject(Constants.DISPLAY_FOLDER_CONTENT + " " + folderPath));
                }
            }
        });
    }

    public void displayContent(String[] files) {
        listView.getItems().clear();
        for (String file : files) {
            listView.getItems().add(file);
        }
    }

    public void displayAllFiles() {
        hideResultLabel();
        pathLabel.setText("Все файлы:");
        listView.getItems().clear();
        net.sendObject(new StringObject(Constants.LIST_ONLY_FILES_COMMAND));
    }

    public void displayStorageCommand() {
        hideResultLabel();
        pathLabel.setText(userName);
        listView.getItems().clear();
        net.sendObject(new StringObject(Constants.DISPLAY_STORAGE_COMMAND));
    }

    public void displayResultLabel(String string) {
        resultLabel.setStyle("-fx-border-color: green;");
        resultLabel.setWrapText(true);
        resultLabel.setTextAlignment(TextAlignment.JUSTIFY);
        resultLabel.setText(string);
    }

    private void hideResultLabel() {
        resultLabel.setStyle("-fx-border-color: none;");
        resultLabel.setText("");
    }

    private void download(String item) {
        String filePath = pathLabel.getText() + "/" + item;
        net.sendObject(new StringObject(Constants.DOWNLOAD_COMMAND + " " + filePath));
    }

    private void downloadFileFromFiles(String item) {
        net.sendObject(new StringObject(Constants.DOWNLOAD_FILE_FROM_FILES + " " + item));
    }

    private void delete(String item) {
        net.sendObject(new StringObject(Constants.DELETE_COMMAND + " " + item));
    }

    public void chooseFiles() {
        hideResultLabel();
        FileChooser fileChooser = new FileChooser();
        files = fileChooser.showOpenMultipleDialog(null);
        try {
            if (files != null) {
                sendFiles();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFiles() {
        for (File file : files) {
            if (file != null) {
                String filePath = file.toString().replace(File.separatorChar, '/');
                String[] tokens = filePath.split("/");
                String fileName = tokens[tokens.length - 1];
                byte[] fileData = new byte[(int) file.length()];
                try {
                    fileData = Files.readAllBytes(file.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                net.sendObject(new FileObject(fileName, fileData));
                addToListView(file.getName());
            }
        }
    }

    public void chooseDirectory() throws IOException {
        hideResultLabel();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) borderPane.getScene().getWindow();
        selectedDirectory = directoryChooser.showDialog(stage);
        String directoryName = selectedDirectory.getName();

        Path directoryPath = Paths.get(selectedDirectory.toString());
        try (Stream<Path> stream = Files.walk(directoryPath)) {
            stream.forEach(file -> {
                if (!(Files.isDirectory(file))) {
                    String fileName = file.getFileName().toString();
                    int startIndex = selectedDirectory.toString().length() + slash;
                    int endIndex = file.toString().length() - fileName.length();
                    String filePathInDirectory = selectedDirectory.getName() + "/" + file.toString().substring(startIndex, endIndex);
                    try {
                        sendDirectory(file, fileName, filePathInDirectory);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            addToListView(directoryName);
        }
    }


    public void sendDirectory(Path filePath, String fileName, String fileFolders) throws IOException {
        byte[] fileData = Files.readAllBytes(filePath);
        net.sendObject(new DirectoryObject(fileName, fileFolders, fileData));
    }

    private void addToListView(String fileName) {
        ObservableList<String> listviewItems = listView.getItems();
        if (!listviewItems.contains(fileName)) {
            listviewItems.add(fileName);
        }
    }

    private void setIconsAndContextMenu() {
        listView.setCellFactory(listView -> {
            ListCell<String> cell = new ListCell<String>() {
                private final ImageView imageView = new ImageView();

                @Override
                public void updateItem(String name, boolean empty) {

                    imageView.setFitHeight(16);
                    imageView.setFitWidth(16);
                    super.updateItem(name, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if (name.contains(".")) {
                            imageView.setImage(file);
                        } else {
                            imageView.setImage(folder);
                        }
                        setText(name);
                        setGraphic(imageView);
                    }
                }
            };
            ContextMenu contextMenu = new ContextMenu();
            MenuItem downloadItem = new MenuItem();
            downloadItem.textProperty().bind(Bindings.format("Download        ", cell.itemProperty()));
            downloadItem.setOnAction(event -> {
                String item = cell.getItem();
                if (pathLabel.getText().equals("Все файлы:")) {
                    downloadFileFromFiles(item);
                } else {
                    download(item);
                }
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete        ", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
                String item = cell.getItem();
                delete(item);
                listView.getItems().remove(cell.getItem());
            });
            contextMenu.getItems().addAll(downloadItem, deleteItem);
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });
    }
}


