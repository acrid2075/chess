package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public interface GameDAO {
    public abstract void clear();
    public abstract GameData getGame(int gameID);
    public abstract GameData createGame(String gameName);
    public abstract Collection<GameData> listGames();
    public abstract GameData updateGame(int gameID, String username, String playerColor);
    public abstract boolean isGame(String gameName);
}
