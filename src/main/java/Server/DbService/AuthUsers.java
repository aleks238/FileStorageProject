package Server.DbService;


import constants.Constants;
import java.sql.*;

public class AuthUsers implements AuthService {
    private static final String URL = "jdbc:mysql://localhost:3306/chatdb";
    private static final String userNameDB = "root";
    private static final String passwordDB = "root";
    private final Object addNewUserLock = new Object();
    private final Object checkUsernameAndPasswordLock = new Object();
    private Connection connection;

    public AuthUsers() {
        try {
            connection = DriverManager.getConnection(URL, userNameDB, passwordDB);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String checkUsernameAndPassword(String username, String password) {
        synchronized (checkUsernameAndPasswordLock) {
            String login = null;
            String userPassword = null;
            String query = "SELECT Login, Password FROM AuthData WHERE Login = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        login = rs.getString("Login");
                        userPassword = rs.getString("Password");
                    }
                }
                if (username.equals(login) && password.equals(userPassword)) {
                    return Constants.AUTH_SUCCESSFUL;
                }
                if (login == null) {
                    return "#Пользователь не существует#";
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return "#Неверный пароль#";
        }
    }

    public String addNewUser(String userName, String password) throws SQLException {
        synchronized (addNewUserLock) {
            if (isPresent(userName)) {
                return "#Такой пользователь уже зарегистрирован#";
            } else {
                String query = "INSERT INTO AuthData (Login, Password) VALUES (?,?)";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, userName);
                    ps.setString(2, password);
                    int counter = ps.executeUpdate();
                    if (counter != 0) {
                        return "#Регистрация успешна#";
                    }
                }
            }
            return "#Регистрация временно не работает#";
        }
    }

    public boolean isPresent(String userName) {
        String query = "SELECT * FROM AuthData WHERE Login = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
