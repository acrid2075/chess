package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SysAuthDAO implements AuthDAO {

    public SysAuthDAO() {
        try {
            this.configureDatabase();
        } catch (Exception e) {
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "This!sMyPassw0rd");
    }

    private void configureDatabase() throws SQLException {
        try (var conn = getConnection()) {
            var createDbStatement = conn.prepareStatement("CREATE DATABASE IF NOT EXISTS ChessDB");
            createDbStatement.executeUpdate();

            conn.setCatalog("ChessDB");

            var createAuthTable = """
            CREATE TABLE  IF NOT EXISTS authTable (
                auth TEXT,
                username TEXT,
            )""";


            try (var createTableStatement = conn.prepareStatement(createAuthTable)) {
                createTableStatement.executeUpdate();
            }
        }
    }


    @Override
    public void clear() {
        try (var conn = getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE * FROM authTable", RETURN_GENERATED_KEYS)) {
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) {
        try (var conn = getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT auth, username FROM authTable WHERE auth=?")) {
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

        }
        return null;
    }

    @Override
    public void createAuth(AuthData authData) {
        try (var conn = getConnection()) {
            try (var preparedStatement = conn.prepareStatement("INSERT INTO authTable (auth, username) VALUES(?, ?)", RETURN_GENERATED_KEYS)) {
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
        try (var conn = getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE * FROM authTable WHERE auth=?", RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, authToken);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
