package websocket.messages;

import model.GameData;

public class LoadGameMessage extends ServerMessage {
    public String gameID;
    public String whiteUsername;
    public String blackUsername;
    public String gameName;
    public String game;
    public LoadGameMessage(ServerMessageType type, String message, String gameID, String whiteUsername, String blackUsername, String gameName, String game) {
        super(type, message);
        this.gameID = gameID;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gameName;
        this.game = game;
    }
}
