package server.websocket;


import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.GameDAO;
import dataaccess.SysGameDAO;
import model.GameData;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
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
    public WebSocketHandler(GameService gameService) {this.gameService = gameService;}

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand userGameCommand = new Gson().fromJson(message, UserGameCommand.class);
        System.out.println(message);
        System.out.println(userGameCommand);
        System.out.println(userGameCommand.getCommandType());
        System.out.println(userGameCommand.username);
        System.out.println(userGameCommand.getGameID());
        System.out.println(userGameCommand.role);
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

    private void connect(UserGameCommand userGameCommand, Session session) throws IOException {
        connectionManager.add(userGameCommand.username, userGameCommand.getAuthToken(), session, userGameCommand.getGameID());
        connectionManager.broadcast(userGameCommand.username, new ServerMessage(NOTIFICATION, userGameCommand.username + " connected to game as " + userGameCommand.role), userGameCommand.getGameID());
    }

    private void makeMove(UserGameCommand userGameCommand, String message, Session session, GameService gameService) {
        SysGameDAO sysGameDAO = new SysGameDAO();
        MakeMoveCommand makeMoveCommand = new MakeMoveCommand(message);
        GameData gameData = gameService.getGame(makeMoveCommand.getGameID());
        
        if (gameData.game().getTeamTurn().equals(ChessGame.TeamColor.WHITE)) {
            
            if (makeMoveCommand.username.equals(gameData.whiteUsername())) {
                
                try {
                    if (gameData.game().validMoves(makeMoveCommand.move.getStartPosition()).contains(makeMoveCommand.move)) {
                        
                        gameData = gameService.makeMove(makeMoveCommand.getGameID(), makeMoveCommand.username, makeMoveCommand.move);
                        ServerMessage serverMessage = new ServerMessage(LOAD_GAME, gameData.toJson());
                        int gameID = gameData.gameID();
                        System.out.println("Sending game refresh");
                        connectionManager.broadcast("", serverMessage, gameID);
                        connectionManager.msg(makeMoveCommand.username, new ServerMessage(NOTIFICATION, makeMoveCommand.move.toString()));
                        if (gameData.game().isInCheck(ChessGame.TeamColor.BLACK)) {
                            connectionManager.broadcast("", new ServerMessage(NOTIFICATION, "Check"), gameData.gameID());
                        } else if (gameData.game().isInCheckmate(ChessGame.TeamColor.BLACK)) {
                            connectionManager.broadcast("", new ServerMessage(NOTIFICATION, "Checkmate. White wins"), gameData.gameID());
                            gameService.gameOver(gameData.gameID());
                        } else if (gameData.game().isInStalemate(ChessGame.TeamColor.BLACK)) {
                            connectionManager.broadcast("", new ServerMessage(NOTIFICATION, "Stalemate"), gameData.gameID());
                            gameService.gameOver(gameData.gameID());
                        }
                        
                    } else {
                        connectionManager.msg(makeMoveCommand.username, new ServerMessage(ERROR, "Error: invalid move."));
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                    System.out.println(e.getMessage());
                    try {connectionManager.msg(makeMoveCommand.username, new ServerMessage(ERROR, "Error: invalid move."));} catch (Exception f) {throw new RuntimeException(f.getMessage());
                    }
                }
            } else {
                try {connectionManager.msg(makeMoveCommand.username, new ServerMessage(ERROR, "Error: not your turn"));} catch (Exception f) {throw new RuntimeException(f.getMessage());
                }
            }
        } else {
            if (makeMoveCommand.username.equals(gameData.blackUsername())) {
                try {
                    if (gameData.game().validMoves(makeMoveCommand.move.getStartPosition()).contains(makeMoveCommand.move)) {
                        gameData = gameService.makeMove(makeMoveCommand.getGameID(), makeMoveCommand.username, makeMoveCommand.move);
                        connectionManager.broadcast("", new ServerMessage(LOAD_GAME, gameData.toJson()), gameData.gameID());
                        connectionManager.msg(makeMoveCommand.username, new ServerMessage(NOTIFICATION, makeMoveCommand.move.toString()));
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
                        connectionManager.msg(makeMoveCommand.username, new ServerMessage(ERROR, "Error: invalid move."));
                    }
                } catch (Exception e) {
                    try {connectionManager.msg(makeMoveCommand.username, new ServerMessage(ERROR, "Error: invalid move."));} catch (Exception f) {throw new RuntimeException(f.getMessage());
                    }
                }
            } else {
                try {connectionManager.msg(makeMoveCommand.username, new ServerMessage(ERROR, "Error: not your turn"));} catch (Exception f) {throw new RuntimeException(f.getMessage());
                }
            }
        }
    }

    private void leave(UserGameCommand userGameCommand, Session session, GameService gameService) {
        String username = userGameCommand.username;
        GameData gameData = gameService.getGame(userGameCommand.getGameID());
        if ((!username.equals(gameData.whiteUsername())) && (!username.equals(gameData.blackUsername()))) {
            return;
        }
        gameService.leaveGame(userGameCommand.getGameID(), username);
        try {
            connectionManager.remove(username, userGameCommand.getGameID());
            connectionManager.broadcast(username, new ServerMessage(NOTIFICATION, username + " has left the game."), userGameCommand.getGameID());
        } catch (Exception e) {throw new RuntimeException("failed to broadcast leave notification");}
    }

    private void resign(UserGameCommand userGameCommand, Session session, GameService gameService) {
        String username = userGameCommand.username;
        GameData gameData = gameService.getGame(userGameCommand.getGameID());
        if ((!username.equals(gameData.whiteUsername())) && (!username.equals(gameData.blackUsername()))) {
            try {
                connectionManager.msg(username, new ServerMessage(ERROR, "Error: not a player."));
            } catch (Exception e) {throw new RuntimeException("failed to send error message");}
            return;
        }
        gameService.gameOver(gameData.gameID());
        try {
            connectionManager.broadcast(username, new ServerMessage(NOTIFICATION, username + ", " + (username.equals(gameData.whiteUsername()) ? "WHITE" : "BLACK") + ", has resigned."), userGameCommand.getGameID());
        } catch (Exception e) {throw new RuntimeException("failed to notification");
        }
    }
}
