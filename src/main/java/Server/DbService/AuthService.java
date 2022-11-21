package Server.DbService;
import java.sql.SQLException;

public interface AuthService {
    String checkUsernameAndPassword(String login, String password);
    String addNewUser(String userName, String password) throws SQLException;
    boolean isPresent(String userName);
}
