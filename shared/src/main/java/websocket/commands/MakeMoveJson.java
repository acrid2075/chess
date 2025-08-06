package websocket.commands;

import chess.ChessMove;

public record MakeMoveJson(UserGameCommand.CommandType commandType, String authToken, String username, Integer gameID, String role, String move) {
}
