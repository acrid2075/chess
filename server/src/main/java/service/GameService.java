package service;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import jdk.jshell.spi.ExecutionControl;
import model.GameData;
import requests.CreateGamesRequest;
import requests.JoinGameRequest;
import spark.Request;
import spark.Response;

import java.util.Collection;
import java.util.Map;

public class GameService {

    private GameDAO gameDAO;
    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public Collection<GameData> listGames() {
        return this.gameDAO.listGames();
    }

    public GameData createGame(CreateGamesRequest createGamesRequest) throws DataAccessException {
        if (gameDAO.isGame(createGamesRequest.gameName())) {
            throw new DataAccessException("Already exists game with gameName.");
        }
        return this.gameDAO.createGame(createGamesRequest.gameName());
    }

    public GameData joinGame(JoinGameRequest joinGameRequest) {
        return this.gameDAO.updateGame(joinGameRequest.gameID(), joinGameRequest.username(), joinGameRequest.playerColor());
    }

    public GameData getGame(int gameID) {
        return this.gameDAO.getGame(gameID);
    }

    public GameData makeMove(int gameID, String username, ChessMove chessMove) throws InvalidMoveException {
        ChessGame game = this.gameDAO.getGame(gameID).game();
        System.out.println(game.getTeamTurn());
        System.out.println(game.getBoard().getPiece(chessMove.getStartPosition()).getTeamColor());
        game.makeMove(chessMove);
        return this.gameDAO.updateBoard(gameID, username, game);
    }

    public void gameOver(int gameID) {
        this.gameDAO.gameOver(gameID);
    }

    public void leaveGame(int gameID, String username) {
        GameData gameData = getGame(gameID);
        if (username.equals(gameData.whiteUsername())) {
            this.gameDAO.leaveGame(gameID, "WHITE");
        }
        this.gameDAO.leaveGame(gameID, "BLACK");
    }
}
