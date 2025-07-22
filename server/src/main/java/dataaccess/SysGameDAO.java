package dataaccess;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.GameData;
import model.UserData;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SysGameDAO implements GameDAO {
    static {
        try {
            configureDatabase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static void configureDatabase() throws SQLException {
        try (var conn = DatabaseManager.getConnection()) {
            DatabaseManager.createDatabase();
            conn.setCatalog("ChessDB");

            var createGamesTable = """
            CREATE TABLE IF NOT EXISTS games (
                id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                whiteUsername TEXT,
                blackUsername TEXT,
                gameName TEXT,
                game TEXT
            )""";


            try (var createTableStatement = conn.prepareStatement(createGamesTable)) {
                createTableStatement.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void clear() {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM games", RETURN_GENERATED_KEYS)) {
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameData getGame(int gameID) {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT id, whiteUsername, blackUsername, gameName, game FROM games WHERE id=?")) {
                preparedStatement.setInt(1, gameID);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        var id = rs.getInt("id");
                        var whiteUsername = rs.getString("whiteUsername");
                        var blackUsername = rs.getString("blackUsername");
                        var gameName = rs.getString("gameName");
                        var game = rs.getString("game");
                        return new GameData(id, whiteUsername, blackUsername, gameName, togglejsonoff(game));
                    }
                    return null;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameData createGame(String gameName) {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(
                    "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES(?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, null);
                preparedStatement.setString(2, null);
                preparedStatement.setString(3, gameName);
                preparedStatement.setString(4, togglejsonon(new ChessGame()));

                preparedStatement.executeUpdate();

                var resultSet = preparedStatement.getGeneratedKeys();
                var id = 0;
                if (resultSet.next()) {
                    id = resultSet.getInt(1);
                }

                return new GameData(id, null, null, gameName, new ChessGame());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<GameData> listGames() {
        var games = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT id, whiteUsername, blackUsername, gameName, game FROM games")) {
                try (var rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        var id = rs.getInt("id");
                        var whiteUsername = rs.getString("whiteUsername");
                        var blackUsername = rs.getString("blackUsername");
                        var gameName = rs.getString("gameName");
                        var game = rs.getString("game");
                        games.add(new GameData(id, whiteUsername, blackUsername, gameName, togglejsonoff(game)));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return games;
    }

    @Override
    public GameData updateGame(int gameID, String username, String playerColor) {
        GameData gameData;
        int id;
        String whiteUsername = "";
        String blackUsername = "";
        String gameName = "";
        String game = "";
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT id, whiteUsername, blackUsername, gameName, game FROM games")) {
                try (var rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        id = rs.getInt("id");
                        whiteUsername = rs.getString("whiteUsername");
                        blackUsername = rs.getString("blackUsername");
                        gameName = rs.getString("gameName");
                        game = rs.getString("game");
                        gameData = new GameData(id, whiteUsername, blackUsername, gameName, togglejsonoff(game));
                    }
                }
            }
            if (Objects.equals(playerColor, "BLACK")) {
                try (var preparedStatement = conn.prepareStatement("UPDATE games SET blackUsername=? WHERE id=?")) {
                    preparedStatement.setString(1, username);
                    preparedStatement.setInt(2, gameID);
                    preparedStatement.executeUpdate();
                }
                return new GameData(gameID,
                    whiteUsername, username, gameName, togglejsonoff(game));
            }

            try (var preparedStatement = conn.prepareStatement("UPDATE games SET whiteUsername=? WHERE id=?")) {
                preparedStatement.setString(1, username);
                preparedStatement.setInt(2, gameID);
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            return new GameData(gameID,
                    username, blackUsername, gameName, togglejsonoff(game));

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isGame(String gameName) {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT id FROM games WHERE gameName=?")) {
                preparedStatement.setString(1, gameName);
                try (var rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public ChessGame togglejsonoff (String jsoncontent) {
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
                    ChessGame newGame = new ChessGame();
                    ChessBoard newboard = newGame.getBoard();
                    board.addPiece(new ChessPosition(4, 4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
                    newGame.setBoard(newboard);
                    return newGame;
                }

            }
        }

        ChessGame output = new ChessGame();
        output.setBoard(board);
        output.setTeamTurn((details.turn()).equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK);
        return output;
    }

    public String togglejsonon (ChessGame game) {
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

        HashMap<String, String> args = new HashMap<>(Map.of("turn", game.getTeamTurn().equals(ChessGame.TeamColor.WHITE) ? "WHITE" : "BLACK"));

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
