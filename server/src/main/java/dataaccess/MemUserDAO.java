package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemUserDAO implements UserDAO {

    HashMap<String, UserData> userdata = new HashMap<>();
    public void clear() {
        userdata.clear();
    }

    @Override
    public void createUser(UserData userData) {
        userdata.put(userData.username(), userData);
    }

    @Override
    public UserData getUser(String username) {
        if (userdata.containsKey(username)) {
            return userdata.get(username);
        }
        return null;
    }
}
