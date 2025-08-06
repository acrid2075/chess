package ui;

import chess.*;
import model.GameData;
import response.BlanketResponse;
import websocketFacade.WebSocketFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

public class GameUI {
    WebSocketFacade webSocketFacade;
    String authToken;
    ServerFacade server;
    static int gameID;
    String username;
    String role;

    public GameUI(WebSocketFacade webSocketFacade, String authToken, ServerFacade server, int gameID, String username, String role) {
        this.webSocketFacade = webSocketFacade;
        this.authToken = authToken;
        this.server = server;
        this.gameID = gameID;
        this.username = username;
        this.role = role;
    }

    public void run() {
        while (true) {
            try {
                GameData game = getGame(server);
                if (game == null) {
                    System.out.println("Unable to retrieve game.");
                    System.out.println();
                    break;
                }
                System.out.printf("[GAME] >>> ");
                Scanner scanner = new Scanner(System.in);
                String line = scanner.nextLine();
                var values = line.split(" ");
                if (values.length < 1) {
                    System.out.println("Please input a keyword.");
                    System.out.println();
                    continue;
                }
                String code = values[0];
                if (code.equals("help")) {
                    System.out.println("move <startcol><startrow><endcol><endrow><kqbnrp for piece if promotion>");
                    System.out.println("redraw - redraw board");
                    System.out.println("highlight <col><row> - highlight valid moves at given space");
                    System.out.println("leave - exit game");
                    System.out.println("resign - to surrender game");
                    System.out.println("help - with possible commands");
                    System.out.println();
                    continue;
                }
                if (code.equals("leave")) {
                    webSocketFacade.leaveGame(authToken, gameID, role);
                    break;
                }
                if (code.equals("redraw")) {
                    seeGame(game, username, null);
                    continue;
                }
                if (code.equals("resign")) {
                    webSocketFacade.resignGame(authToken, gameID, role);
                    continue;
                }
                if (values.length < 2) {
                    System.out.println("Missing keywords.");
                    System.out.println(values[0]);
                    System.out.println();
                    continue;
                }
                if (code.equals("highlight")) {
                    String location = values[1];
                    seeGame(game, username, new ChessPosition( ((int) location.charAt(1) - '0'), 8 - ((int) location.charAt(0) - 'a')));
                    continue;
                }
                if (code.equals("move")) {
                    String location = values[1];
                    ChessMove move = new ChessMove(location);
                    webSocketFacade.makeMove(authToken, gameID, move, role);
                    continue;
                }
            } catch (Exception e) {
                System.out.println("An unknown error has occurred. Please contact the software provider." + e.getMessage());
                System.out.println(); return;
            }
        }
    }


    static private GameData getGame(ServerFacade server) {
        BlanketResponse response = server.listGames();
        Collection<GameData> games;
        if (response.games() != null) {
            if (response.games().isEmpty()) {
                return null;
            }
            games = response.games();
            for (GameData game : games) {
                if (game.gameID() == gameID) {
                    return game;
                }
            }
            return null;
        }
        return null;
    }


    static void seeGame(GameData gameData, String username, ChessPosition highlightPosition) {
        ChessGame game = gameData.game();
        ChessBoard board = game.getBoard();
        Collection<ChessMove> validMoveList;
        if (highlightPosition == null) {
            validMoveList = new ArrayList<ChessMove>();
        } else {
            validMoveList = game.validMoves(highlightPosition);
        }
        int j;
        if (username.equals(gameData.blackUsername())) {
            System.out.print("    H\u2003 G\u2003 F\u2003 E\u2003 D\u2003 C\u2003 B\u2003 A\u2003");
            System.out.println();
            for (int i = 1; i <= 8; i++) {
                System.out.print(" " + (i) + " ");
                for (j = 1; j <= 8; j++) {
                    if (new ChessPosition(i, j).equals(highlightPosition)) {
                        printSquare(i, j, board, false, true);
                    } else if (validMoveList.contains(new ChessMove(highlightPosition, new ChessPosition(i, j), null))) {
                        printSquare(i, j, board, true, false);
                    } else {
                        printSquare(i, j, board, false, false);
                    }
                }
                System.out.print(EscapeSequences.RESET_BG_COLOR);
                System.out.print(" " + (i) + " ");
                System.out.println();
            }
            System.out.print("    H\u2003 G\u2003 F\u2003 E\u2003 D\u2003 C\u2003 B\u2003 A\u2003");
            System.out.println();
            return;
        }
        System.out.print("    A\u2003 B\u2003 C\u2003 D\u2003 E\u2003 F\u2003 G\u2003 H\u2003");
        System.out.println();
        for (int i = 8; i >= 1; i--) {
            System.out.print(" " + (i) + " ");
            for (j = 8; j >= 1; j--) {
                if (new ChessPosition(i, j).equals(highlightPosition)) {
                    printSquare(i, j, board, false, true);
                } else if (validMoveList.contains(new ChessMove(highlightPosition, new ChessPosition(i, j), null))) {
                    printSquare(i, j, board, true, false);
                } else {
                    printSquare(i, j, board, false, false);
                }
            }
            System.out.print(EscapeSequences.RESET_BG_COLOR);
            System.out.print(" " + (i) + " ");
            System.out.println();
        }
        System.out.print("    A\u2003 B\u2003 C\u2003 D\u2003 E\u2003 F\u2003 G\u2003 H\u2003");
        System.out.println();
        return;
    }

    static private void printSquare(int i, int j, ChessBoard board, boolean highlight, boolean center) {
        if (center) {
            System.out.print(EscapeSequences.SET_BG_COLOR_YELLOW);
            ChessPiece piece = board.getPiece(new ChessPosition(i, j));
            String output = EscapeSequences.EMPTY;
            if (piece != null) {
                output = getPiece(piece.hashCode());
            }
            System.out.print(output);
        } else if (highlight) {
            System.out.print(((((i + j) % 2) == 1) ? EscapeSequences.SET_BG_COLOR_GREEN : EscapeSequences.SET_BG_COLOR_DARK_GREEN));
            ChessPiece piece = board.getPiece(new ChessPosition(i, j));
            String output = EscapeSequences.EMPTY;
            if (piece != null) {
                output = getPiece(piece.hashCode());
            }
            System.out.print(output);
        } else {
            System.out.print(((((i + j) % 2) == 1) ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY));
            ChessPiece piece = board.getPiece(new ChessPosition(i, j));
            String output = EscapeSequences.EMPTY;
            if (piece != null) {
                output = getPiece(piece.hashCode());
            }
            System.out.print(output);
        }
    }

    static private String getPiece(int hashCode){
        return switch (hashCode) {
            case 0 -> EscapeSequences.BLACK_KING;
            case 1 -> EscapeSequences.BLACK_QUEEN;
            case 2 -> EscapeSequences.BLACK_BISHOP;
            case 3 -> EscapeSequences.BLACK_KNIGHT;
            case 4 -> EscapeSequences.BLACK_ROOK;
            case 5 -> EscapeSequences.BLACK_PAWN;
            case 10 -> EscapeSequences.WHITE_KING;
            case 11 -> EscapeSequences.WHITE_QUEEN;
            case 12 -> EscapeSequences.WHITE_BISHOP;
            case 13 -> EscapeSequences.WHITE_KNIGHT;
            case 14 -> EscapeSequences.WHITE_ROOK;
            case 15 -> EscapeSequences.WHITE_PAWN;
            default -> EscapeSequences.EMPTY;
        };
    }
}
