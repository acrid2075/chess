package server;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPosition;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.util.log.Log;
import passoff.chess.EqualsTestingUtility;
import requests.*;
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

public class ServerUnitTests { // extends EqualsTestingUtility<Server>
    public ServerUnitTests() { //super("Server", "servers");

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
    @DisplayName("Test getAuth succeeds for present authToken")
    public void getAuthTrue() {
        GameDAO gameDAO = new MemGameDAO();
        AuthDAO authDAO = new MemAuthDAO();
        UserDAO userDAO = new MemUserDAO();
        AuthData authData = new AuthData("1", "andycrid");
        authDAO.createAuth(authData);
        assert authDAO.getAuth("1") == authData;
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
    @DisplayName("Test register fails with existing username.")
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
    @DisplayName("Test Login succeeds for existing user.")
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
    @DisplayName("Test Login fails with nonexistent users and incorrect usernames.")
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
    @DisplayName("Test Logout succeeds for logged-in user")
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
    @DisplayName("Test Logout fails with logged out users.")
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


    @Test
    @DisplayName("Test the new game is in ListGames when created.")
    public void createGameTrue() {
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
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            assert gameService.listGames().contains(gameData);
        }
        catch (Exception e) {
            success = false;
        }
        assert success;
    }

    @Test
    @DisplayName("Test that createGame doesn't leave list games empty.")
    public void CreateGameFalse() {
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
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            assert !gameService.listGames().isEmpty();
        }
        catch (Exception e) {
            success = false;
        }
        assert success;
    }


    @Test
    @DisplayName("Test the new game is in ListGames when created.")
    public void GetGameTrue() {
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
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            assert (gameService.getGame(gameData.gameID()) == gameData);
        }
        catch (Exception e) {
            success = false;
        }
        assert success;
    }

    @Test
    @DisplayName("Test that createGame doesn't leave list games empty.")
    public void GetGameFalse() {
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
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            assert (gameService.getGame(gameData.gameID()) != new GameData(1222, "me", "you", "there", new ChessGame()));
        }
        catch (Exception e) {
            success = false;
        }
        assert success;
    }



    @Test
    @DisplayName("Test that ListGames contains a game.")
    public void ListGamesTrue() {
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
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            assert gameService.listGames().contains(gameData);
        }
        catch (Exception e) {
            success = false;
        }
        assert success;
    }

    @Test
    @DisplayName("Test that listGames is not empty.")
    public void ListGamesFalse() {
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
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            assert !gameService.listGames().isEmpty();
        }
        catch (Exception e) {
            success = false;
        }
        assert success;
    }


    @Test
    @DisplayName("Test that places the user in the slot.")
    public void JoinGameTrue() {
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
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            gameService.joinGame(new JoinGameRequest(gameData.gameID(), "andycrid", "BLACK"));
            assert gameService.listGames().contains(new GameData(gameData.gameID(), null, "andycrid", "Jerry", gameData.game()));
        }
        catch (Exception e) {
            success = false;
        }
        assert success;
    }

    @Test
    @DisplayName("Test that places user in the right slot.")
    public void JoinGameFalse() {
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
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            gameService.joinGame(new JoinGameRequest(gameData.gameID(), "andycrid", "BLACK"));
            assert !gameService.listGames().contains(new GameData(gameData.gameID(), "andycrid", null, "Jerry", gameData.game()));
        }
        catch (Exception e) {
            success = false;
        }
        assert success;
    }



    @Test
    @DisplayName("Test the new game is in ListGames when created.")
    public void ClearTrue() {
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
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            clearService.clear();
            assert gameService.listGames().isEmpty();
        }
        catch (Exception e) {
            success = false;
        }
        assert success;
    }

    @Test
    @DisplayName("Test that createGame doesn't leave list games empty.")
    public void ClearFalse() {
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
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            clearService.clear();
            assert !gameService.listGames().contains(gameData);
        }
        catch (Exception e) {
            success = false;
        }
        assert success;
    }


    @Test
    @DisplayName("Test register succeeds for new user")
    public void IsUserTrue() {
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
            assert userService.isUser("andycrid");
        }
        catch (AlreadyTakenException e) {
            success = false;
        }
        assert success;
    }

    @Test
    @DisplayName("Test register fails with existing username.")
    public void IsUserFalse() {
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
            assert !userService.isUser("bencrid");
        }
        catch (AlreadyTakenException ignored) {
        }
        assert success;
    }

}
