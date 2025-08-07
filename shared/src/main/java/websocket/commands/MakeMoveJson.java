package websocket.commands;

import chess.ChessMove;

public record MakeMoveJson(UserGameCommand.CommandType commandType, String authToken, Integer gameID, MoveJson move) {
}
