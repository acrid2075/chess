package websocket.commands;

import chess.ChessMove;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;

public class MakeMoveCommand extends UserGameCommand {
    public ChessMove move;
    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move) {
        super(commandType, authToken, gameID);
        this.move = move;
    }

    public MakeMoveCommand(String json) {
        this(new Gson().fromJson(json, MakeMoveJson.class));
    }

    public MakeMoveCommand(MakeMoveJson makeMoveJson) {
        super(makeMoveJson.commandType(), makeMoveJson.authToken(), makeMoveJson.gameID());
        this.move = new ChessMove(makeMoveJson.move());
    }

    @Override
    public String toJson() {
        Gson serializer = new Gson();
        return serializer.toJson(Map.of("commandType", this.getCommandType(), "authToken", this.getAuthToken(), "gameID", this.getGameID(), "move", this.move.toString()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MakeMoveCommand)) {
            return false;
        }
        MakeMoveCommand that = (MakeMoveCommand) o;
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID()) &&
                Objects.equals(this.move, that.move);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID()) + this.move.hashCode();
    }
}
