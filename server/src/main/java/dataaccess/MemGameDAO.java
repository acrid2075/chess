package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class MemGameDAO implements GameDAO {
    HashMap<Integer, GameData> gamedata = new HashMap<>();
    public void clear() {
        gamedata.clear();
    }

    @Override
    public GameData getGame(int gameID) {
        if (gamedata.containsKey(gameID)) {
            return gamedata.get(gameID);
        }
        return null;
    }

    @Override
    public GameData createGame(String gameName) {
        gamedata.put(gameName.hashCode(), new GameData(gameName.hashCode(), null, null, gameName, new ChessGame()));
        return gamedata.get(gameName.hashCode());
    }

    @Override
    public Collection<GameData> listGames() {
        return gamedata.values();
    }

    @Override
    public GameData updateGame(String gameName, String username, String PlayerColor) {
        GameData currentGameData = gamedata.get(gameName.hashCode());
        if (Objects.equals(PlayerColor, "BLACK")) {
            gamedata.replace(currentGameData.gameID(), new GameData(currentGameData.gameID(), currentGameData.whiteUsername(), username, gameName, currentGameData.game()));
            return gamedata.get(currentGameData.gameID());
        }
        gamedata.replace(gameName.hashCode(), new GameData(currentGameData.gameID(), username, currentGameData.blackUsername(), gameName, currentGameData.game()));
        return gamedata.get(currentGameData.gameID());
    }


}
