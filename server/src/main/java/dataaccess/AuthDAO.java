package dataaccess;
import model.AuthData;

public interface AuthDAO {
    public abstract void clear();
    public abstract AuthData getAuth(String authToken);
    public abstract void createAuth(AuthData authData);
    public abstract void deleteAuth(String authToken);
}
