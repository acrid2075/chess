package websocket.commands;

import chess.ChessMove;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;

public class MakeMoveCommand extends UserGameCommand {
    public ChessMove chessMove;
    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove chessMove) {
        super(commandType, authToken, gameID);
        this.chessMove = chessMove;
    }

    public String toJson() {
        Gson serializer = new Gson();
        return serializer.toJson(Map.of("CommandType", this.getCommandType(), "authToken", this.getAuthToken(), "gameID", this.getGameID(), "chessMove", this.chessMove.toString()));
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
