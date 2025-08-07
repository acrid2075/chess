package model;

import chess.ChessGame;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;


public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {

    public String toJson() {
        Gson serializer = new Gson();
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", gameID);
        map.put("whiteUsername", whiteUsername);
        map.put("blackUsername", blackUsername);
        map.put("gameName", gameName);
        map.put("game", Serializer.togglejsonon(game));
        return serializer.toJson(map);
    }

    public GameData(String json) {
        this(parse(json));
    }

    private static GameDataJson parse(String json) {
        Gson serializer = new Gson();
        GameDataJson gameDataJson = serializer.fromJson(json, GameDataJson.class);
        return gameDataJson;
    }

    public GameData(GameDataJson gameDataJson) {
        this(gameDataJson.gameID(), gameDataJson.whiteUsername(), gameDataJson.blackUsername(), gameDataJson.gameName(),
                Serializer.togglejsonoff(gameDataJson.game()));
    }
}

