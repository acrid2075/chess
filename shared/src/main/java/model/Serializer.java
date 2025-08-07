package model;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.ChessGameJson;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Serializer {
    public static ChessGame togglejsonoff (String jsoncontent) {
        var serializer = new Gson();
        ChessGameJson details = serializer.fromJson(jsoncontent, ChessGameJson.class);
        ChessBoard board = new ChessBoard();

        Map<String, ChessPiece> map = Map.ofEntries(
                Map.entry("k", new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING)),
                Map.entry("q", new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN)),
                Map.entry("b", new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP)),
                Map.entry("n", new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT)),
                Map.entry("r", new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK)),
                Map.entry("p", new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN)),
                Map.entry("K", new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING)),
                Map.entry("Q", new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN)),
                Map.entry("B", new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP)),
                Map.entry("N", new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT)),
                Map.entry("R", new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK)),
                Map.entry("P", new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN))
        );

        for (char c = 'a'; c <= 'h'; c++) {
            for (char d = '1'; d <= '8'; d++) {
                int col = (int) c - (int) 'a' + 1;
                int row = (int) d - (int) '1' + 1;
                String posCode = "" + c + d;
                Class<?> f = details.getClass();
                try {
                    Field field = f.getDeclaredField(posCode);
                    field.setAccessible(true);
                    String pieceCode = (String) field.get(details);
                    ChessPiece piece = null;
                    if (map.containsKey(pieceCode)) {
                        piece = map.get(pieceCode);
                    }
                    if (piece != null) {
                        board.addPiece(new ChessPosition(row, col), piece);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }

        ChessGame output = new ChessGame();
        output.setBoard(board);
        if (details.turn() == null) {
            output.setTeamTurn(null);
        } else {
            output.setTeamTurn((details.turn()).equals("WHITE") ? ChessGame.TeamColor.WHITE : ((details.turn()).equals("BLACK") ? ChessGame.TeamColor.BLACK : null));
        }
        return output;
    }

    public static String togglejsonon (ChessGame game) {
        var serializer = new Gson();
        ChessBoard board = game.getBoard();

        Map<ChessPiece, String> reverseMap = Map.ofEntries(
                Map.entry(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING), "k"),
                Map.entry(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN), "q"),
                Map.entry(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP), "b"),
                Map.entry(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT), "n"),
                Map.entry(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK), "r"),
                Map.entry(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN), "p"),
                Map.entry(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING), "K"),
                Map.entry(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN), "Q"),
                Map.entry(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP), "B"),
                Map.entry(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT), "N"),
                Map.entry(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK), "R"),
                Map.entry(new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN), "P")
        );
        HashMap<String, String> args;
        if (game.getTeamTurn() == null) {
            args = new HashMap<>(Map.of());
        } else {
            args = new HashMap<>(Map.of("turn", game.getTeamTurn().equals(ChessGame.TeamColor.WHITE) ? "WHITE" : (game.getTeamTurn().equals(ChessGame.TeamColor.BLACK) ? "BLACK" : "")));
        }
        for (char c = 'a'; c <= 'h'; c++) {
            for (char d = '1'; d <= '8'; d++) {
                int col = (int) c - (int) 'a' + 1;
                int row = (int) d - (int) '1' + 1;
                String posCode = "" + c + d;
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece == null) {
                    args.put(posCode, "");
                    continue;
                }
                args.put(posCode, reverseMap.get(piece));

            }
        }
        return serializer.toJson(args);
    }
}
