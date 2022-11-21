package Server.ServerApp;

import Server.AbstractObjects.*;
import constants.Constants;
import io.netty.channel.ChannelHandlerContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerFunctions {
    private final Object displayFolderContentLock = new Object();
    private final Object performDownloadLock = new Object();
    private final Object sendFolderToClientLock = new Object();
    private final Object sendFileToClientLock = new Object();
    private final Object downloadFileFromFilesLock = new Object();
    private final Object performListFilesLock = new Object();
    private final Object performDeleteLock = new Object();
    private final Object sendStorageContentLock = new Object();
    private final Object downloadFileOnServerLock = new Object();
    private final Object downloadFolderOnServerLock = new Object();
    private final static int space = 1;

    protected void displayFolderContent(ChannelHandlerContext channel, String str) throws IOException {
        synchronized (displayFolderContentLock) {
            String folderPath = "cloudUsers/" + str.substring(Constants.DISPLAY_FOLDER_CONTENT.length() + space);
            try (Stream<Path> stream = Files.list(Paths.get(folderPath))) {
                String[] folderContent = stream.map(Path::getFileName)
                        .map(Path::toString)
                        .toArray(String[]::new);
                channel.writeAndFlush(new StorageListObject(folderContent));
            }
        }
    }

    protected void performDownload(ChannelHandlerContext channel, String str) throws IOException {
        synchronized (performDownloadLock) {
            String filePath = str.substring(Constants.DOWNLOAD_COMMAND.length() + space);
            Path path = Paths.get("cloudUsers/" + filePath);
            String fileName = path.getFileName().toString();
            if (Files.isDirectory(path)) {
                sendFolderToClient(channel, path, fileName);
            } else {
                sendFileToClient(channel, path, fileName);
            }
        }
    }

    protected void sendFolderToClient(ChannelHandlerContext channel, Path folderPath, String folderName) throws IOException {
        synchronized (sendFolderToClientLock) {
            try (Stream<Path> stream = Files.walk(folderPath)) {
                stream.forEach(file -> {
                    if (!(Files.isDirectory(file))) {
                        String fileName = file.getFileName().toString();
                        int startIndex = folderPath.toString().length();
                        int endIndex = file.toString().length() - fileName.length();
                        String filePathInFolder = folderName + file.toString().substring(startIndex, endIndex);
                        byte[] fileData = new byte[(int) file.toFile().length()];
                        try {
                            fileData = Files.readAllBytes(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        channel.writeAndFlush(new DirectoryObject(fileName, filePathInFolder, fileData));
                        log.debug("Клиент загрузил директорию " + fileName);
                    }
                });
            }
        }
    }

    protected void sendFileToClient(ChannelHandlerContext channel, Path filePath, String fileName) throws IOException {
        synchronized (sendFileToClientLock) {
            byte[] fileData = Files.readAllBytes(filePath);
            channel.writeAndFlush(new FileObject(fileName, fileData));
            log.debug("Клиент загрузил файл " + fileName);
        }
    }

    protected void downloadFileFromFiles(String userName, ChannelHandlerContext channel, String str) throws IOException {
        synchronized (downloadFileFromFilesLock) {
            String fileName = str.substring(Constants.DOWNLOAD_FILE_FROM_FILES.length() + space);
            try (Stream<Path> stream = Files.walk(Paths.get("cloudUsers/" + userName))) {
                Path path = stream.filter(file -> file.getFileName().toString().equals(fileName))
                        .findAny()
                        .orElse(null);
                if (path != null) {
                    sendFileToClient(channel, path, fileName);
                    log.debug("Клиент загрузил файл " + fileName);
                }
            }
        }
    }

    protected void performListFiles(String userName, ChannelHandlerContext channel) throws IOException {
        synchronized (performListFilesLock) {
            Path path = Paths.get("cloudUsers/" + userName);
            try (Stream<Path> stream = Files.walk(path)) {
                String[] filesArray = stream.filter(file -> !Files.isDirectory(file))
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .toArray(String[]::new);
                channel.writeAndFlush(new StorageListObject(filesArray));
            }
        }

    }

    protected void performDelete(ChannelHandlerContext channel, String userName, String str) throws IOException {
        synchronized (performDeleteLock) {
            String fileName = str.substring(Constants.DELETE_COMMAND.length() + space);
            Path filePath;
            try (Stream<Path> stream = Files.list(Paths.get("cloudUsers/" + userName))) {
                filePath = stream.filter(file -> file.getFileName().toString().equals(fileName))
                        .findAny()
                        .orElse(null);
            }
            if (Files.isDirectory(filePath)) {
                try (Stream<Path> stream = Files.walk(filePath)) {
                    stream.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                    log.debug("клиент " + userName + " удалил директорию " + filePath);
                    channel.writeAndFlush(new StringObject("#Файлы удалены#"));
                }
            } else {
                Files.deleteIfExists(filePath);
                log.debug("клиент " + userName + " удалил файл " + filePath);
                channel.writeAndFlush(new StringObject("#Файлы удалены#"));
            }
        }
    }

    protected void sendStorageContent(String userName, ChannelHandlerContext channel) {
        synchronized (sendStorageContentLock) {
            File clientDirectory = new File("cloudUsers/" + userName);
            String[] clientContent = clientDirectory.list();
            channel.writeAndFlush(new StorageListObject(clientContent));
        }
    }

    protected void storeFileOnServer(ChannelHandlerContext channel, String userName, ByteObject object) throws
            IOException {
        synchronized (downloadFileOnServerLock) {
            FileObject file = (FileObject) object;
            String fileName = file.getFileName();
            byte[] fileData = file.getFile();

            Path userDirectoryPath = Paths.get("cloudUsers/" + userName);
            if (Files.notExists(userDirectoryPath)) {
                Files.createDirectory(userDirectoryPath);
            }
            Path userFile = Paths.get("cloudUsers/" + userName + "/" + fileName);
            if (Files.notExists(userFile)) {
                Files.createFile(userFile);
                Files.write(userFile, fileData);

                channel.writeAndFlush(new StringObject("#Загрузка файлов завершена#"));
                log.debug("Клиент " + userName + " загрузил файл " + fileName);
            } else {
                channel.writeAndFlush(new StringObject("#Файлы уже существуют#"));
            }
        }
    }

    protected void storeDirectoryOnServer(ChannelHandlerContext channel, String userName, ByteObject object) throws
            NoSuchFieldException, IllegalAccessException, IOException {
        synchronized (downloadFolderOnServerLock) {
            DirectoryObject sendDirectory = (DirectoryObject) object;
            String fileName = sendDirectory.getFileName();
            String fileFolders = sendDirectory.getFileFolders();
            byte[] fileData = sendDirectory.getFile();

            Path foldersPath = Paths.get("cloudUsers" + "/" + userName + "/" + fileFolders);
            if (Files.notExists(foldersPath)) {
                Files.createDirectories(foldersPath);
            }
            Path filePath = Paths.get(foldersPath + "/" + fileName);
            if (Files.notExists(filePath)) {
                Files.createFile(filePath);
                Files.write(filePath, fileData);
                channel.writeAndFlush(new StringObject("#Загрузка файлов завершена#"));
                log.debug("Клиент " + userName + " загрузил файл " + fileName);
            } else {
                channel.writeAndFlush(new StringObject("#Папка уже существует#"));
            }
        }
    }
}
