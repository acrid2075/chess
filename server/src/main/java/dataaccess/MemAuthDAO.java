package dataaccess;

import model.*;

import java.util.HashMap;

public class MemAuthDAO implements AuthDAO {
    HashMap<String, AuthData> authdata = new HashMap<>();
    public void clear() {
        authdata.clear();
    }

    public AuthData getAuth(String authToken) {
        if (authdata.containsKey(authToken)) {
            return authdata.get(authToken);
        }
        return null;
    }

    public void createAuth(AuthData authData) {
        authdata.put(authData.authToken(), authData);
    }

    public void deleteAuth(String authToken) {
        authdata.remove(authToken, authdata.get(authToken));
    }
}
