package service;

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
}
