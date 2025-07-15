package dataaccess;

import model.UserData;

public interface UserDAO {
    public abstract void clear();
    public abstract void createUser(UserData userData);
    public abstract UserData getUser(String username);
}
