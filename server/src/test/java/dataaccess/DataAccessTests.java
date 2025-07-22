package dataaccess;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import passoff.chess.EqualsTestingUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DataAccessTests {
    public DataAccessTests() {
    }
    @Test
    @DisplayName("Assert invertible serializer")
    public void testFlip() {
        ChessGame game = new ChessGame();
        ChessPiece piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        ChessBoard board = game.getBoard();
        board.addPiece(new ChessPosition(4, 4), piece);
        game.setBoard(board);
        SysGameDAO serializer = new SysGameDAO();
        String jsonval = serializer.to_json(game);
        ChessGame out = serializer.from_json(jsonval);
        var newserializer = new Gson();
        ChessGameJson chessGameJson = newserializer.fromJson(jsonval, ChessGameJson.class);
        Class<?> f = chessGameJson.getClass();
        String pieceCode;
        try {
            pieceCode = (String) f.getDeclaredField("d4").get(chessGameJson);
        } catch (Exception e) {
            pieceCode = "not" + e.toString();
        }
        Assertions.assertEquals(out, game, " Failed." + pieceCode + out.getBoard().getPiece(new ChessPosition(4, 4))+ game.getBoard().getPiece(new ChessPosition(4, 4)).getPieceType() + serializer.to_json(game));

    }
}
