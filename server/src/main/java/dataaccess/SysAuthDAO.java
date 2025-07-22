package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SysAuthDAO implements AuthDAO {

    static {
        try {
            configureDatabase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "This!sMyPassw0rd");
        return conn;
    }

    private static void configureDatabase() throws SQLException {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        try (var conn = DatabaseManager.getConnection()) {
            conn.setCatalog("ChessDB");

            var createauthtable = """
                CREATE TABLE IF NOT EXISTS authtable (
                auth VARCHAR(1000) PRIMARY KEY,
                username TEXT
            )""";

            try (var createTableStatement = conn.prepareStatement(createauthtable)) {
                createTableStatement.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void clear() {
        try (var conn = DatabaseManager.getConnection()) {
            conn.setCatalog("ChessDB");
            try (var preparedStatement = conn.prepareStatement("DELETE FROM authtable", RETURN_GENERATED_KEYS)) {
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) {
        try (var conn = DatabaseManager.getConnection()) {
            conn.setCatalog("ChessDB");
            try (var preparedStatement = conn.prepareStatement("SELECT auth, username FROM authtable WHERE auth=?")) {
                preparedStatement.setString(1, authToken);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        var auth = rs.getString("auth");
                        var username = rs.getString("username");
                        return new AuthData(auth, username);
                    }
                    return null;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createAuth(AuthData authData) {
        try (var conn = DatabaseManager.getConnection()) {
            conn.setCatalog("ChessDB");
            try (var preparedStatement = conn.prepareStatement("INSERT INTO authtable (auth, username) VALUES(?, ?)", RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, authData.authToken());
                preparedStatement.setString(2, authData.username());

                preparedStatement.executeUpdate();

                var resultSet = preparedStatement.getGeneratedKeys();

                return;
            } catch (Exception e) {

            }
        } catch (Exception e) {

        }
        return;
    }

    @Override
    public void deleteAuth(String authToken) {
        try (var conn = DatabaseManager.getConnection()) {
            conn.setCatalog("ChessDB");
            try (var preparedStatement = conn.prepareStatement("DELETE FROM authtable WHERE auth=?", RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, authToken);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
