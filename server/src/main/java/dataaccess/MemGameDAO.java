package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class MemGameDAO implements GameDAO {
    HashMap<Integer, GameData> gamedata = new HashMap<>();
    HashMap<String, GameData> gamenames = new HashMap<>();
    private int gameID = 1;
    public void clear() {
        gamedata.clear();
        gamenames.clear();
    }

    @Override
    public GameData getGame(int gameID) {
        if (gamedata.containsKey(gameID)) {
            return gamedata.get(gameID);
        }
        return null;
    }

    @Override
    public boolean isGame(String gameName) {
        if (gamenames.containsKey(gameName)) {
            return true;
        }
        return false;
    }

    @Override
    public GameData createGame(String gameName) {
        this.gameID++;
        gamedata.put(this.gameID, new GameData(this.gameID, null, null, gameName,
                new ChessGame()));
        return gamedata.get(this.gameID);
    }

    @Override
    public Collection<GameData> listGames() {
        return gamedata.values();
    }

    @Override
    public GameData updateGame(int gameID, String username, String playerColor) {
        GameData currentGameData = gamedata.get(gameID);
        if (Objects.equals(playerColor, "BLACK")) {
            gamedata.replace(currentGameData.gameID(), new GameData(currentGameData.gameID(),
                    currentGameData.whiteUsername(), username, currentGameData.gameName(), currentGameData.game()));
            return gamedata.get(currentGameData.gameID());
        }
        gamedata.replace(gameID, new GameData(currentGameData.gameID(), username, currentGameData.blackUsername(),
                currentGameData.gameName(), currentGameData.game()));
        return gamedata.get(currentGameData.gameID());
    }

    @Override
    public GameData updateBoard(int gameID, String username, ChessGame chessGame) {
        GameData currentGameData = gamedata.get(gameID);
        gamedata.replace(gameID, new GameData(currentGameData.gameID(), currentGameData.whiteUsername(), currentGameData.blackUsername(),
                currentGameData.gameName(), chessGame));
        return gamedata.get(currentGameData.gameID());
    }

    @Override
    public void gameOver(int gameID) {
        GameData currentGameData = gamedata.get(gameID);
        ChessGame game = currentGameData.game();
        game.setTeamTurn(null);
        gamedata.replace(gameID, new GameData(currentGameData.gameID(), currentGameData.whiteUsername(), currentGameData.blackUsername(),
                currentGameData.gameName(), game));
    }

    @Override
    public void leaveGame(int gameID, String playerColor) {
        GameData currentGameData = gamedata.get(gameID);
        if (Objects.equals(playerColor, "BLACK")) {
            gamedata.replace(currentGameData.gameID(), new GameData(currentGameData.gameID(),
                    currentGameData.whiteUsername(), null, currentGameData.gameName(), currentGameData.game()));
        }
        gamedata.replace(gameID, new GameData(currentGameData.gameID(), null, currentGameData.blackUsername(),
                currentGameData.gameName(), currentGameData.game()));
    }

}
