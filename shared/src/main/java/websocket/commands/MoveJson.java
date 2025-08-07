package websocket.commands;

import chess.ChessPiece;

public record MoveJson(PositionJson start, PositionJson end, PositionJson startPosition, PositionJson endPosition, ChessPiece.PieceType piece) {
}
