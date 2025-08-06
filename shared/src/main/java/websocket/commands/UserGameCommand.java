package websocket.commands;

import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 *
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    private final CommandType commandType;

    private final String authToken;

    public final String username;

    private final Integer gameID;

    public final String role;

    public UserGameCommand(CommandType commandType, String authToken, String username, Integer gameID, String role) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.username = username;
        this.gameID = gameID;
        this.role = role;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    public String toJson() {
        Gson serializer = new Gson();
        return serializer.toJson(Map.of("commandType", this.getCommandType(), "authToken", this.getAuthToken(), "username", this.username, "role", role, "gameID", this.getGameID()));
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserGameCommand)) {
            return false;
        }
        UserGameCommand that = (UserGameCommand) o;
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID());
    }
}
