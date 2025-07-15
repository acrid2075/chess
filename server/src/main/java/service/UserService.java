package service;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import jdk.jshell.spi.ExecutionControl;
import spark.Request;
import spark.Response;

import java.util.Map;

public class UserService {
    private UserDAO userDAO;
    private AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        throw new ExecutionControl.NotImplementedException("Not Implemented");
        res.type("application/json");
        return new Gson().toJson(Map.of("name", names));
    }

    public LoginResult login(LoginRequest loginRequest) {
        throw new ExecutionControl.NotImplementedException("Not Implemented");
        res.type("application/json");
        return new Gson().toJson(Map.of("name", names));
    }

    public void logout(LogoutRequest logoutRequest) {
        throw new ExecutionControl.NotImplementedException("Not Implemented");
        res.type("application/json");
        return new Gson().toJson(Map.of("name", names));
    }
}
