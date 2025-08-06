package model;

import chess.ChessGame;

public record GameDataJson(int gameID, String whiteUsername, String blackUsername, String gameName, String game) {
}
