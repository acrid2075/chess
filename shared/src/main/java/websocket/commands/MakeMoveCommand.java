package websocket.commands;

import chess.ChessMove;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;

public class MakeMoveCommand extends UserGameCommand {
    public ChessMove chessMove;
    public MakeMoveCommand(CommandType commandType, String authToken, String username, Integer gameID, String role, ChessMove chessMove) {
        super(commandType, authToken, username, gameID, role);
        this.chessMove = chessMove;
    }


    @Override
    public String toJson() {
        Gson serializer = new Gson();
        return serializer.toJson(Map.of("commandType", this.getCommandType(), "authToken", this.getAuthToken(), "username", this.username, "gameID", this.getGameID(), "role", role, "chessMove", this.chessMove.toString()));
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
                Objects.equals(this.chessMove, that.chessMove);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID()) + this.chessMove.hashCode();
    }
}
