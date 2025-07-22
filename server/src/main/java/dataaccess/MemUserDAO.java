package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;

public class MemUserDAO implements UserDAO {

    HashMap<String, UserData> userdata = new HashMap<>();
    public void clear() {
        userdata.clear();
    }

    @Override
    public void createUser(UserData userData) {
        userdata.put(userData.username(), new UserData(userData.username(), BCrypt.hashpw(userData.password(), BCrypt.gensalt()), userData.email()));
    }

    @Override
    public UserData getUser(String username) {
        if (userdata.containsKey(username)) {
            return userdata.get(username);
        }
        return null;
    }
}
