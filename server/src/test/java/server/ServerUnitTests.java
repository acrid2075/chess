package server;

import chess.ChessBoard;
import chess.ChessPosition;
import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.eclipse.jetty.util.log.Log;
import passoff.chess.EqualsTestingUtility;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import results.LoginResult;
import server.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.ClearService;
import service.GameService;
import service.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerUnitTests extends EqualsTestingUtility<Server> {
    public ServerUnitTests() {
        super("Server", "servers");
    }
    @Test
    @DisplayName("Construct DAO")
    public void constructDAO() {
        GameDAO gameDAO = new MemGameDAO();
        AuthDAO authDAO = new MemAuthDAO();
        UserDAO userDAO = new MemUserDAO();
        assert gameDAO.listGames().isEmpty();
    }

    @Test
    @DisplayName("Test getAuth fails for missing authToken")
    public void getAuthFalse() {
        GameDAO gameDAO = new MemGameDAO();
        AuthDAO authDAO = new MemAuthDAO();
        UserDAO userDAO = new MemUserDAO();
        assert authDAO.getAuth(" ") == null;
    }

    @Test
    @DisplayName("Test register succeeds for new user")
    public void RegisterTrue() {
        GameDAO gameDAO = new MemGameDAO();
        AuthDAO authDAO = new MemAuthDAO();
        UserDAO userDAO = new MemUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = true;
        try {
            userService.register(new RegisterRequest(userData));
        }
        catch (AlreadyTakenException e) {
            success = false;
        }
        assert success;
        assert userDAO.getUser("andycrid") == userData;
    }

    @Test
    @DisplayName("Test register fails wit")
    public void RegisterFalse() {
        GameDAO gameDAO = new MemGameDAO();
        AuthDAO authDAO = new MemAuthDAO();
        UserDAO userDAO = new MemUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = false;
        try {
            userService.register(new RegisterRequest(userData));
            try {
                userService.register(new RegisterRequest(userData));
            }
            catch (AlreadyTakenException e) {
                success = true;
            }
        }
        catch (AlreadyTakenException ignored) {
        }
        assert success;
        assert userDAO.getUser("andycrid") == userData;
    }

    @Test
    @DisplayName("Test register succeeds for new user")
    public void LoginTrue() {
        GameDAO gameDAO = new MemGameDAO();
        AuthDAO authDAO = new MemAuthDAO();
        UserDAO userDAO = new MemUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = true;
        try {
            userService.register(new RegisterRequest(userData));
            userService.login(new LoginRequest(userData.username(), userData.password()));
        }
        catch (Exception e) {
            success = false;
        }
        assert success;
    }

    @Test
    @DisplayName("Test register fails wit")
    public void LoginFalse() {
        GameDAO gameDAO = new MemGameDAO();
        AuthDAO authDAO = new MemAuthDAO();
        UserDAO userDAO = new MemUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = true;
        try {
            userService.register(new RegisterRequest(userData));
            userService.login(new LoginRequest(userData.username(), "12344"));
        }
        catch (Exception e) {
            success = false;
        }
        assert !success;
        try {
            userService.login(new LoginRequest("bencrid", "12345"));
        }
        catch (Exception e) {
            success = false;
        }
        assert !success;
    }


    @Test
    @DisplayName("Test register succeeds for new user")
    public void LogoutTrue() {
        GameDAO gameDAO = new MemGameDAO();
        AuthDAO authDAO = new MemAuthDAO();
        UserDAO userDAO = new MemUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = true;
        try {
            userService.register(new RegisterRequest(userData));
            LoginResult loginResult = userService.login(new LoginRequest(userData.username(), "12345"));
            userService.logout(new LogoutRequest(loginResult.authData().authToken()));
        }
        catch (Exception e) {
            success = false;
        }
        assert success;

    }

    @Test
    @DisplayName("Test register fails wit")
    public void LogoutFalse() {
        GameDAO gameDAO = new MemGameDAO();
        AuthDAO authDAO = new MemAuthDAO();
        UserDAO userDAO = new MemUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = false;
        try {
            userService.register(new RegisterRequest(userData));
            LoginResult loginResult = userService.login(new LoginRequest(userData.username(), "12345"));
            userService.logout(new LogoutRequest(loginResult.authData().authToken()));
            userService.logout(new LogoutRequest(loginResult.authData().authToken()));
        }
        catch (Exception e) {
            success = true;
        }
        assert success;
    }


    @Override
    protected Server buildOriginal() {
        return null;
    }

    @Override
    protected Collection<Server> buildAllDifferent() {
        return List.of();
    }
}
