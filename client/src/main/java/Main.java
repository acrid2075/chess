import chess.*;
import model.GameData;
import response.BlanketResponse;
import ui.EscapeSequences;
import ui.ServerFacade;

import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        ServerFacade server = new ServerFacade("http://localhost:8080");

        while (true) {
            try {
                System.out.printf("[LOGGED_OUT] >>> ");
                Scanner scanner = new Scanner(System.in);
                String line = scanner.nextLine();
                var values = line.split(" ");
                if (values.length < 1) {
                    System.out.println("Please input a keyword.");
                    System.out.println();
                    continue;
                }
                String code = values[0];
                if (code.equals("quit")) {
                    break;
                }
                if (code.equals("help")) {
                    System.out.println("register <USERNAME> <PASSWORD> <EMAIL> - to create an account");
                    System.out.println("login <USERNAME> <PASSWORD> - to play chess");
                    System.out.println("quit - to quit application");
                    System.out.println("help - with possible commands");
                    System.out.println();
                    continue;
                }
                if (values.length < 3) {
                    System.out.println("Missing keywords.");
                    System.out.println();
                    continue;
                }
                if (code.equals("login")) {
                    BlanketResponse response = server.login(values[1], values[2]);
                    if (response.message() != null) {
                        System.out.println("Error in login, please try again");
                        System.out.println();
                        continue;
                    }
                    System.out.println("Successful login");
                    System.out.println();
                    if (logged_in(server, values[1])) {
                        break;
                    }
                }
                if (values.length < 4) {
                    System.out.println("Missing keywords.");
                    System.out.println();
                    continue;
                }
                if (code.equals("register")) {
                    BlanketResponse response = server.register(values[1], values[2], values[3]);
                    if (response.message() != null) {
                        System.out.println("Error in register, please try again");
                        System.out.println();
                        continue;
                    }
                    System.out.println("Successful registration");
                    System.out.println();
                    if (logged_in(server, values[1])) {
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("An error occurred. Please contact the software provider.");
                System.out.println();
            }
        }
    }

    static boolean logged_in(ServerFacade server, String username) { //returns true if quitting application
        Collection<GameData> games;
        HashMap<Integer, GameData> gameDict = new HashMap<>();
        while (true) {
            System.out.printf("[LOGGED_IN] >>> ");
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            var values = line.split(" ");
            try {
                if (values.length < 1) {
                    System.out.println("Please input a keyword.");
                    System.out.println();
                    continue;
                }
            } catch (Exception e) {
                System.out.println("An error has occurred.");
                System.out.println();
                continue;
            }
            String code = values[0];
            try {
                if (code.equals("help")) {
                    System.out.println("create <NAME> - to create a game");
                    System.out.println("list - games");
                    System.out.println("join <ID> [WHITE|BLACK] - a game");
                    System.out.println("observe <ID> - a game");
                    System.out.println("logout - logs out the user");
                    System.out.println("quit - to quit application");
                    System.out.println("help - with possible commands");
                    System.out.println();
                    continue;
                }
                if (code.equals("logout")) {
                    System.out.println("Logging out.");
                    server.logout();
                    System.out.println();
                    return false;
                }
                if (code.equals("quit")) {
                    return true;
                }
                if (code.equals("list")) {
                    BlanketResponse response = server.listGames();
                    if (response.games() != null) {
                        games = response.games();
                        int i = 1;
                        gameDict = new HashMap<>();
                        for (GameData game : games) {
                            System.out.printf("" + i + ") Name: " + game.gameName() + ", White: " + game.whiteUsername() +
                                    ", Black: " + game.blackUsername() + "%n");
                            gameDict.put(i++, game);
                        }
                        System.out.println();
                    }
                    continue;
                }
                if (values.length < 2) {
                    System.out.println("Missing keywords.");
                    System.out.println();
                    continue;
                }
                if (code.equals("create")) {
                    BlanketResponse response = server.createGame(values[1]);
                    if (response.message() != null) {
                        System.out.println("Failed to create game.");
                        System.out.println();
                        continue;
                    }
                    System.out.println("Created game.");
                    System.out.println();
                    continue;
                }
            } catch (Exception e) {
                System.out.println("An error has occurred.");
                System.out.println();
                continue;
            }
            if (code.equals("observe")) {
                int gameNum = 0;
                try {
                    gameNum = Integer.parseInt(values[1]);
                } catch (Exception e) {
                    System.out.println("Game number was not a number."); System.out.println(); continue;
                }
                try {
                    GameData game = gameDict.get(gameNum);
                    observe(game, username);
                    System.out.println(); continue;
                } catch (Exception e) {
                    System.out.println("Unsuccessful observing the game."); System.out.println(); continue;
                }
            }
            if (values.length < 3) {
                System.out.println("Missing keywords."); System.out.println(); continue;
            }
            if (code.equals("join")) {
                int gameNum = 0;
                try {
                    gameNum = Integer.parseInt(values[1]);
                } catch (Exception e) {
                    System.out.println("Game number was not a number."); System.out.println();
                    continue;
                }
                try {
                    GameData game = gameDict.get(gameNum);
                    server.joinGame(values[2], game.gameID());
                    System.out.println();
                } catch (Exception e) {
                    System.out.println("Unsuccessful joining the game."); System.out.println();
                }
            }
        }
    }

    static void observe(GameData gameData, String username) {
        ChessGame game = gameData.game();
        ChessBoard board = game.getBoard();
        ChessPiece piece;
        int j;
        if (username.equals(gameData.blackUsername())) {
            for (int i = 8; i >= 1; i--) {
                for (j = 8; j >= 1; j--) {
                    System.out.print(((((i + j) % 2) == 0) ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY));
                    piece = board.getPiece(new ChessPosition(i, j));

                    String output = EscapeSequences.EMPTY;
                    if (piece != null) {
                        output = switch (piece.hashCode()) {
                            case 0 -> EscapeSequences.WHITE_KING;
                            case 1 -> EscapeSequences.WHITE_QUEEN;
                            case 2 -> EscapeSequences.WHITE_BISHOP;
                            case 3 -> EscapeSequences.WHITE_KNIGHT;
                            case 4 -> EscapeSequences.WHITE_ROOK;
                            case 5 -> EscapeSequences.WHITE_PAWN;
                            case 10 -> EscapeSequences.BLACK_KING;
                            case 11 -> EscapeSequences.BLACK_QUEEN;
                            case 12 -> EscapeSequences.BLACK_BISHOP;
                            case 13 -> EscapeSequences.BLACK_KNIGHT;
                            case 14 -> EscapeSequences.BLACK_ROOK;
                            case 15 -> EscapeSequences.BLACK_PAWN;

                            default -> throw new IllegalStateException("Unexpected value: " + piece);
                        };
                    };
                    System.out.print(output);
                }
                System.out.print(EscapeSequences.RESET_BG_COLOR);
                System.out.println();
            }
            return;
        }
        for (int i = 1; i <= 8; i++) {
            System.out.println();
            for (j = 1; j <= 8; j++) {
                System.out.print(((((i + j) % 2) == 0) ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY));
                piece = board.getPiece(new ChessPosition(i, j));
                String output = EscapeSequences.EMPTY;
                if (piece != null) {
                    output = switch (piece.hashCode()) {
                        case 0 -> EscapeSequences.WHITE_KING;
                        case 1 -> EscapeSequences.WHITE_QUEEN;
                        case 2 -> EscapeSequences.WHITE_BISHOP;
                        case 3 -> EscapeSequences.WHITE_KNIGHT;
                        case 4 -> EscapeSequences.WHITE_ROOK;
                        case 5 -> EscapeSequences.WHITE_PAWN;
                        case 10 -> EscapeSequences.BLACK_KING;
                        case 11 -> EscapeSequences.BLACK_QUEEN;
                        case 12 -> EscapeSequences.BLACK_BISHOP;
                        case 13 -> EscapeSequences.BLACK_KNIGHT;
                        case 14 -> EscapeSequences.BLACK_ROOK;
                        case 15 -> EscapeSequences.BLACK_PAWN;

                        default -> throw new IllegalStateException("Unexpected value: " + piece);
                    };
                };
                System.out.print(output);
            }
            System.out.print(EscapeSequences.RESET_BG_COLOR);
            System.out.println();
        }
        return;
    }
}