package dataaccess;
import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import requests.*;
import results.LoginResult;
import service.ClearService;
import service.GameService;
import service.UserService;
import java.util.Objects;

public class DataAccessUnitTests {
    public DataAccessUnitTests() {
    }
    @Test
    @DisplayName("Assert invertible serializer")
    public void testFlip() {
        ChessGame game = new ChessGame();
        ChessPiece piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        ChessBoard board = game.getBoard();
        board.addPiece(new ChessPosition(4, 4), piece);
        game.setBoard(board);
        SysGameDAO serializer = new SysGameDAO();
        String jsonval = serializer.togglejsonon(game);
        ChessGame out = serializer.togglejsonoff(jsonval);
        var newserializer = new Gson();
        ChessGameJson chessGameJson = newserializer.fromJson(jsonval, ChessGameJson.class);
        Class<?> f = chessGameJson.getClass();
        String pieceCode;
        try {
            pieceCode = (String) f.getDeclaredField("d4").get(chessGameJson);
        } catch (Exception e) {
            pieceCode = "not" + e.toString();
        }
        Assertions.assertEquals(out, game, " Failed." + pieceCode + out.getBoard().getPiece(
                new ChessPosition(4, 4))+ game.getBoard().getPiece(new
                ChessPosition(4, 4)).getPieceType() + serializer.togglejsonon(game));

    }
    private void clearDAO() {
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        gameDAO.clear();
        authDAO.clear();
        userDAO.clear();
    }
    @Test
    @DisplayName("Construct DAO")
    public void constructDAO() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        assert gameDAO.listGames().isEmpty();
    }
    @Test
    @DisplayName("Test getAuth fails for missing authToken")
    public void getAuthFalse() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        assert authDAO.getAuth(" ") == null;
    }
    @Test
    @DisplayName("Test getAuth succeeds for present authToken")
    public void getAuthTrue() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        AuthData authData = new AuthData("1", "andycrid");
        authDAO.createAuth(authData);
        assert authDAO.getAuth("1").equals(authData);
    }
    @Test
    @DisplayName("Test register succeeds for new user")
    public void registerTrue() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
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
        assert Objects.equals(userDAO.getUser("andycrid").username(), userData.username()) :
                userDAO.getUser("andycrid").username() + userData.username();
    }
    @Test
    @DisplayName("Test register fails with existing username.")
    public void registerFalse() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = false;
        // Tests register, as data access unit
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
        assert Objects.equals(userDAO.getUser("andycrid").username(), userData.username());
    }

    @Test
    @DisplayName("Test Login succeeds for existing user.")
    public void loginTrue() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = true;
        String message = "";
        // Tests login with existing, as data access unit
        try {
            userService.register(new RegisterRequest(userData));
            userService.login(new LoginRequest(userData.username(), "12345"));
        }
        catch (Exception e) {
            success = false;
            message = e.getMessage();
        }
        assert success : message;
    }
    @Test
    @DisplayName("Test Login fails with nonexistent users and incorrect usernames.")
    public void loginFalse() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = true;
        // Tests login with nonexistent, as data access unit
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
    public void logoutTrue() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = true;
        // Tests logout, as data access unit
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
    public void logoutFalse() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = false;
        // Tests login, as data access unit
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
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = true;
        // Tests listgames, as data access unit
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
    public void createGameFalse() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = true;
        // Tests creategame, as data access unit
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
    public void getGameTrue() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid", "12345", "acriddl2@byu.edu");
        Boolean success = true;
        try {
            userService.register(new RegisterRequest(userData));
            LoginResult loginResult = userService.login(new LoginRequest(userData.username(), "12345"));
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            assert (gameService.getGame(gameData.gameID()).equals(gameData));
        }
        catch (Exception e) {
            success = false;
        }
        assert success;
    }
    @Test
    @DisplayName("Test that createGame doesn't leave list games empty.")
    public void getGameFalse() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
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
            success = false; }
        assert success;
    }
    @Test
    @DisplayName("Test that ListGames contains a game.")
    public void listGamesTrue() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid",  "12345", "acriddl2@byu.edu");
        Boolean success = true;
        // Tests listgames, as data access unit
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
    public void listGamesFalse() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid",  "12345", "acriddl2@byu.edu");
        Boolean success = true;
        try {
            userService.register(new RegisterRequest(userData));
            LoginResult loginResult = userService.login(new LoginRequest(userData.username(), "12345"));
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            assert !gameService.listGames().isEmpty();
        }
        catch (Exception e) {
            success = false; }
        assert success;
    }
    @Test
    @DisplayName("Test that places the user in the slot.")
    public void joinGameTrue() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid",  "12345", "acriddl2@byu.edu");
        Boolean success = true;
        try {
            userService.register(new RegisterRequest(userData));
            LoginResult loginResult = userService.login(new LoginRequest(userData.username(), "12345"));
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            gameService.joinGame(new JoinGameRequest(gameData.gameID(), "andycrid", "BLACK"));
            assert gameService.listGames().contains(new GameData(gameData.gameID(), null, "andycrid", "Jerry", gameData.game()));
        }
        catch (Exception e) {
            success = false; }
        assert success;
    }
    @Test
    @DisplayName("Test that places user in the right slot.")
    public void joinGameFalse() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid",  "12345", "acriddl2@byu.edu");
        Boolean success = true;
        try {
            userService.register(new RegisterRequest(userData));
            LoginResult loginResult = userService.login(new LoginRequest(userData.username(), "12345"));
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            gameService.joinGame(new JoinGameRequest(gameData.gameID(), "andycrid", "BLACK"));
            assert !gameService.listGames().contains(new GameData(gameData.gameID(), "andycrid", null, "Jerry", gameData.game()));
        }
        catch (Exception e) {
            success = false; }
        assert success;
    }
    @Test
    @DisplayName("Test the new game is in ListGames when created.")
    public void clearTrue() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid",  "12345", "acriddl2@byu.edu");
        Boolean success = true;
        try {
            userService.register(new RegisterRequest(userData));
            LoginResult loginResult = userService.login(new LoginRequest(userData.username(), "12345"));
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            clearService.clear();
            assert gameService.listGames().isEmpty();
        }
        catch (Exception e) {
            success = false; }
        assert success;
    }
    @Test
    @DisplayName("Test that createGame doesn't leave list games empty.")
    public void clearFalse() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid",  "12345", "acriddl2@byu.edu");
        Boolean success = true;
        try {
            userService.register(new RegisterRequest(userData));
            LoginResult loginResult = userService.login(new LoginRequest(userData.username(), "12345"));
            GameData gameData = gameService.createGame(new CreateGamesRequest("Jerry"));
            clearService.clear();
            assert !gameService.listGames().contains(gameData);
        }
        catch (Exception e) {
            success = false; }
        assert success;
    }
    @Test
    @DisplayName("Test register succeeds for new user")
    public void isUserTrue() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid",  "12345", "acriddl2@byu.edu");
        Boolean success = true;
        try {
            userService.register(new RegisterRequest(userData));
            assert userService.isUser("andycrid");
        }
        catch (AlreadyTakenException e) {
            success = false; }
        assert success;
    }
    @Test
    @DisplayName("Test register fails with existing username.")
    public void isUserFalse() {
        clearDAO();
        GameDAO gameDAO = new SysGameDAO(); AuthDAO authDAO = new SysAuthDAO(); UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        UserData userData = new UserData("andycrid",  "12345", "acriddl2@byu.edu");
        Boolean success = true;
        try {
            userService.register(new RegisterRequest(userData));
            assert !userService.isUser("bencrid");
        }
        catch (AlreadyTakenException ignored) {}
        assert success;
    }
}
