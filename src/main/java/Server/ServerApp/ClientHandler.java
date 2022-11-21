package Server.ServerApp;


import Server.AbstractObjects.ByteObject;
import Server.AbstractObjects.StringObject;
import Server.DbService.AuthUsers;
import constants.Constants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.SQLException;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<ByteObject> {
    private final ServerFunctions serverFunctions = new ServerFunctions();
    private final String space = " ";
    private final AuthUsers authUsers;
    private String userName;

    ClientHandler(AuthUsers authUsers) {
        this.authUsers = authUsers;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Канал открыт");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Канал закрыт");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channel, ByteObject object) {
        String className = object.getClass().getSimpleName();
        try {
            switch (className) {
                case "StringObject":
                    processStringMessage(channel, object);
                    break;
                case "FileObject":
                    serverFunctions.storeFileOnServer(channel, userName, object);
                    break;
                case "DirectoryObject":
                    serverFunctions.storeDirectoryOnServer(channel, userName, object);
                    break;
            }
        } catch (NoSuchFieldException
                 | IllegalAccessException
                 | SQLException
                 | IOException e) {
            e.printStackTrace();
        }
    }

    private void processStringMessage(ChannelHandlerContext channel, ByteObject object) throws NoSuchFieldException, IllegalAccessException, SQLException, IOException {
        StringObject stringMessage = (StringObject) object;
        String message = stringMessage.getMessage();
        String[] tokens = message.split(space);
        String command = tokens[0];

        switch (command) {
            case Constants.AUTH_COMMAND:
                performAuth(channel, message);
                break;
            case Constants.REGISTRATION_COMMAND:
                performRegistration(channel, message);
                break;
            case Constants.DOWNLOAD_COMMAND:
                serverFunctions.performDownload(channel, message);
                break;
            case Constants.DELETE_COMMAND:
                serverFunctions.performDelete(channel, userName, message);
                break;
            case Constants.LIST_ONLY_FILES_COMMAND:
                serverFunctions.performListFiles(userName, channel);
                break;
            case Constants.DISPLAY_STORAGE_COMMAND:
                serverFunctions.sendStorageContent(userName, channel);
                break;
            case Constants.DISPLAY_FOLDER_CONTENT:
                serverFunctions.displayFolderContent(channel, message);
                break;
            case Constants.DOWNLOAD_FILE_FROM_FILES:
                serverFunctions.downloadFileFromFiles(userName, channel, message);
                break;
        }
    }

    private void performRegistration(ChannelHandlerContext channel, String str) throws SQLException {
        String[] tokens = str.split(space);
        String response = authUsers.addNewUser(tokens[1], tokens[2]);
        channel.writeAndFlush(new StringObject(response));
    }

    private void performAuth(ChannelHandlerContext channel, String str) {
        String[] tokens = str.split(space);
        String response = authUsers.checkUsernameAndPassword(tokens[1], tokens[2]);
        channel.writeAndFlush(new StringObject(response));
        if (response.equals(Constants.AUTH_SUCCESSFUL)) {
            userName = tokens[1];
            serverFunctions.sendStorageContent(userName, channel);
            log.debug("Клиент " + userName + " вошел в аккаунт");
        }
    }

}

