package service;

import com.google.gson.Gson;
import dataaccess.GameDAO;
import jdk.jshell.spi.ExecutionControl;
import spark.Request;
import spark.Response;

import java.util.Map;

public class GameService {

    private GameDAO gameDAO;
    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        throw new ExecutionControl.NotImplementedException("Not Implemented");
        res.type("application/json");
        return new Gson().toJson(Map.of("name", names));
    }

    public ListGamesResult listGames(ListGamesRequest listGamesRequest) {
        throw new ExecutionControl.NotImplementedException("Not Implemented");
        res.type("application/json");
        return new Gson().toJson(Map.of("name", names));
    }

    public CreateGamesResult createGame(CreateGamesRequest createGamesRequest) {
        throw new ExecutionControl.NotImplementedException("Not Implemented");
        res.type("application/json");
        return new Gson().toJson(Map.of("name", names));
    }

    public JoinGameResult joinGame(JoinGameRequest joinGameRequest) {
        throw new ExecutionControl.NotImplementedException("Not Implemented");
        res.type("application/json");
        return new Gson().toJson(Map.of("name", names));
    }
}
