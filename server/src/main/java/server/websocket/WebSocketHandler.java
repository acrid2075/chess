package server.websocket;


import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.GameDAO;
import dataaccess.SysGameDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.commands.UserGameCommand.CommandType.*;

import java.io.IOException;
import java.time.Year;

import static websocket.messages.ServerMessage.ServerMessageType.*;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connectionManager = new ConnectionManager();
    private GameService gameService;
    private UserService userService;
    public WebSocketHandler(GameService gameService, UserService userService) {this.gameService = gameService; this.userService = userService;}
    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand userGameCommand = new Gson().fromJson(message, UserGameCommand.class);
        if (!checkAuth(userGameCommand, session)) {
            return;
        }
        System.out.println(message);
        System.out.println(userGameCommand);
        System.out.println(userGameCommand.getCommandType());
        System.out.println(userGameCommand.getGameID());
        System.out.println(userGameCommand.toJson());
        switch (userGameCommand.getCommandType()) {
            case CONNECT -> connect(userGameCommand, session);
            case MAKE_MOVE -> makeMove(userGameCommand, message, session, gameService);
            case LEAVE -> leave(userGameCommand, session, gameService);
            case RESIGN -> resign(userGameCommand, session, gameService);
            default -> connect(userGameCommand, session);
//            case LEAVE -> leave(userGameCommand, session);
        }
    }

    private boolean checkAuth(UserGameCommand userGameCommand, Session session) {
        Gson serializer = new Gson();
        String message = serializer.toJson(new ServerMessage(ERROR, "Error: invalid authtoken."));
        if (userService.getAuth(userGameCommand.getAuthToken()) == null) {
            try {
                session.getRemote().sendString(message);
                return false;
            } catch (Exception e) {
                throw new RuntimeException("Unable to send error message.");
            }
        }
        return true;
    }

    private void connect(UserGameCommand userGameCommand, Session session) throws IOException {
        if (this.checkAuth(userGameCommand, session)) {
            AuthData authData = userService.getAuth(userGameCommand.getAuthToken());
            GameData gameData = gameService.getGame(userGameCommand.getGameID());
            String username = authData.username();
            if (gameData == null) {
                Gson serializer = new Gson();
                String message = serializer.toJson(new ServerMessage(ERROR, "Error: nonexistent game."));
                new Connection(username, userGameCommand.getAuthToken(), session, userGameCommand.getGameID()).send(message);
                return;
            }
            String role = (username.equals(gameData.whiteUsername())) ? "WHITE" : ((username.equals(gameData.blackUsername())) ? "BLACK" : "OBSERVER");
            connectionManager.add(username, userGameCommand.getAuthToken(), session, userGameCommand.getGameID());
            connectionManager.msg(username, new ServerMessage(LOAD_GAME, gameData.toJson()));
            connectionManager.broadcast(username, new ServerMessage(NOTIFICATION, username + " connected to game as " + role), userGameCommand.getGameID());
        }

    }

    private void makeMove(UserGameCommand userGameCommand, String message, Session session, GameService gameService) {
        if (this.checkAuth(userGameCommand, session)) {
            AuthData authData = userService.getAuth(userGameCommand.getAuthToken());
            String username = authData.username();
            SysGameDAO sysGameDAO = new SysGameDAO();
            MakeMoveCommand makeMoveCommand = new MakeMoveCommand(message);
            GameData gameData = gameService.getGame(makeMoveCommand.getGameID());
            if (gameData.game().getTeamTurn() == null) {
                try {
                    connectionManager.msg(username, new ServerMessage(ERROR, "Error: game is over."));
                } catch (Exception e) {
                    System.out.println("Failed to send error message on game over.");
                }
            }
            if (gameData.game().getTeamTurn().equals(ChessGame.TeamColor.WHITE)) {
                if (username.equals(gameData.whiteUsername())) {

                    try {
                        if (gameData.game().validMoves(makeMoveCommand.move.getStartPosition()).contains(makeMoveCommand.move)) {

                            gameData = gameService.makeMove(makeMoveCommand.getGameID(), username, makeMoveCommand.move);
                            ServerMessage serverMessage = new ServerMessage(LOAD_GAME, gameData.toJson());
                            int gameID = gameData.gameID();
                            System.out.println("Sending game refresh");
                            connectionManager.broadcast("", serverMessage, gameID);
                            connectionManager.msg(username, new ServerMessage(NOTIFICATION, makeMoveCommand.move.toString()));
                            if (gameData.game().isInCheckmate(ChessGame.TeamColor.BLACK)) {
                                connectionManager.broadcast("", new ServerMessage(NOTIFICATION, "Checkmate. White wins"), gameData.gameID());
                                gameService.gameOver(gameData.gameID());
                            } else if (gameData.game().isInCheck(ChessGame.TeamColor.BLACK)) {
                                connectionManager.broadcast("", new ServerMessage(NOTIFICATION, "Check"), gameData.gameID());
                            } else if (gameData.game().isInStalemate(ChessGame.TeamColor.BLACK)) {
                                connectionManager.broadcast("", new ServerMessage(NOTIFICATION, "Stalemate"), gameData.gameID());
                                gameService.gameOver(gameData.gameID());
                            }

                        } else {
                            connectionManager.msg(username, new ServerMessage(ERROR, "Error: invalid move."));
                        }
                    } catch (Exception e) {
                        System.out.println(e.toString());
                        System.out.println(e.getMessage());
                        try {
                            connectionManager.msg(username, new ServerMessage(ERROR, "Error: invalid move."));
                        } catch (Exception f) {
                            throw new RuntimeException(f.getMessage());
                        }
                    }
                } else {
                    try {
                        connectionManager.msg(username, new ServerMessage(ERROR, "Error: not your turn"));
                    } catch (Exception f) {
                        throw new RuntimeException(f.getMessage());
                    }
                }
            } else {
                if (username.equals(gameData.blackUsername())) {
                    try {
                        if (gameData.game().validMoves(makeMoveCommand.move.getStartPosition()).contains(makeMoveCommand.move)) {
                            gameData = gameService.makeMove(makeMoveCommand.getGameID(), username, makeMoveCommand.move);
                            connectionManager.broadcast("", new ServerMessage(LOAD_GAME, gameData.toJson()), gameData.gameID());
                            connectionManager.msg(username, new ServerMessage(NOTIFICATION, makeMoveCommand.move.toString()));
                            if (gameData.game().isInCheck(ChessGame.TeamColor.WHITE)) {
                                connectionManager.broadcast("", new ServerMessage(NOTIFICATION, "Check"), gameData.gameID());
                            } else if (gameData.game().isInCheckmate(ChessGame.TeamColor.WHITE)) {
                                connectionManager.broadcast("", new ServerMessage(NOTIFICATION, "Checkmate. Black wins"), gameData.gameID());
                                gameService.gameOver(gameData.gameID());
                            } else if (gameData.game().isInStalemate(ChessGame.TeamColor.WHITE)) {
                                connectionManager.broadcast("", new ServerMessage(NOTIFICATION, "Stalemate"), gameData.gameID());
                                gameService.gameOver(gameData.gameID());
                            }
                        } else {
                            connectionManager.msg(username, new ServerMessage(ERROR, "Error: invalid move."));
                        }
                    } catch (Exception e) {
                        try {
                            connectionManager.msg(username, new ServerMessage(ERROR, "Error: invalid move."));
                        } catch (Exception f) {
                            throw new RuntimeException(f.getMessage());
                        }
                    }
                } else {
                    try {
                        connectionManager.msg(username, new ServerMessage(ERROR, "Error: not your turn"));
                    } catch (Exception f) {
                        throw new RuntimeException(f.getMessage());
                    }
                }
            }
        }
    }

    private void leave(UserGameCommand userGameCommand, Session session, GameService gameService) {
        if (this.checkAuth(userGameCommand, session)) {
            AuthData authData = userService.getAuth(userGameCommand.getAuthToken());
            String username = authData.username();
            GameData gameData = gameService.getGame(userGameCommand.getGameID());
            if ((!username.equals(gameData.whiteUsername())) && (!username.equals(gameData.blackUsername()))) {
                return;
            }
            gameService.leaveGame(userGameCommand.getGameID(), username);
            try {
                connectionManager.remove(username, userGameCommand.getGameID());
                connectionManager.broadcast(username, new ServerMessage(NOTIFICATION, username + " has left the game."), userGameCommand.getGameID());
            } catch (Exception e) {
                throw new RuntimeException("failed to broadcast leave notification");
            }
        }
    }

    private void resign(UserGameCommand userGameCommand, Session session, GameService gameService) {
        if (this.checkAuth(userGameCommand, session)) {
            AuthData authData = userService.getAuth(userGameCommand.getAuthToken());
            String username = authData.username();
            GameData gameData = gameService.getGame(userGameCommand.getGameID());
            if ((!username.equals(gameData.whiteUsername())) && (!username.equals(gameData.blackUsername()))) {
                try {
                    connectionManager.msg(username, new ServerMessage(ERROR, "Error: not a player."));
                } catch (Exception e) {
                    throw new RuntimeException("failed to send error message");
                }
                return;
            }
            gameService.gameOver(gameData.gameID());
            try {
                connectionManager.broadcast(username, new ServerMessage(NOTIFICATION, username + ", " + (username.equals(gameData.whiteUsername()) ? "WHITE" : "BLACK") + ", has resigned."), userGameCommand.getGameID());
            } catch (Exception e) {
                throw new RuntimeException("failed to notification");
            }
        }
    }
}
