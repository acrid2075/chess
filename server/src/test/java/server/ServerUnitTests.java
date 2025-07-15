package server;

import chess.ChessBoard;
import chess.ChessPosition;
import passoff.chess.EqualsTestingUtility;
import server.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerUnitTests extends EqualsTestingUtility<Server> {
    public ServerUnitTests() {
        super("Server", "servers");
    }
    @Test
    @DisplayName("Construct Empty ChessBoard")
    public void constructChessBoard() {


    }

    @Override
    protected Server buildOriginal() {
        return null;
    }

    @Override
    protected Collection<Server> buildAllDifferent() {
        return List.of();
    }
}
