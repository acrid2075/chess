package service;

import com.google.gson.Gson;
import dataaccess.*;
import jdk.jshell.spi.ExecutionControl;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import results.LoginResult;
import results.RegisterResult;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.UUID;

public class UserService {
    private UserDAO userDAO;
    private AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws AlreadyTakenException {
        if (userDAO.getUser(registerRequest.userData().username()) != null) {
            throw new AlreadyTakenException("Username already taken.");
        }
        this.userDAO.createUser(registerRequest.userData());
        String authToken = String.valueOf(UUID.randomUUID());
        String username = registerRequest.userData().username();
        this.authDAO.createAuth(new AuthData(authToken, username));
        return new RegisterResult(username, authToken);
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException, IncorrectPasswordException {
        String username = loginRequest.username();
        UserData userData = this.userDAO.getUser(username);
        if (userData == null) {
            throw new DataAccessException("No user with that username.");
        }
        String hashedpassword = userData.password();
        String inputpassword = loginRequest.password();
        if (!BCrypt.checkpw(inputpassword, hashedpassword)) {
            throw new IncorrectPasswordException("Incorrect Password. " + hashedpassword + " " + inputpassword + " " +
                    BCrypt.checkpw(inputpassword, hashedpassword));
        }
        String authToken = String.valueOf(UUID.randomUUID());
        AuthData authData = new AuthData(authToken, username);
        authDAO.createAuth(authData);
        return new LoginResult(authData);
    }

    public void logout(LogoutRequest logoutRequest) throws DataAccessException {
        String authToken = logoutRequest.authToken();
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Invalid AuthToken.");
        }
        authDAO.deleteAuth(authToken);
    }

    public AuthData getAuth(String authToken) {
        return authDAO.getAuth(authToken);
    }

    public Boolean isUser(String username) {
        return (userDAO.getUser(username) != null) ;
    }
}
