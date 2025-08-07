import chess.*;
import model.GameData;
import response.BlanketResponse;
import ui.EscapeSequences;
import ui.GameUI;
import ui.ServerFacade;
import websocket.messages.ServerMessage;
import websocketfacade.ServerMessageHandler;
import websocketfacade.WebSocketFacade;

import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import static ui.GameUI.getPiece;

public class Main {
    public static final String URL = "http://localhost:8080";

    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        ServerFacade server = new ServerFacade(URL);

        while (true) {
            try {
                String line;
                try {
                    System.out.printf("[LOGGED_OUT] >>> ");
                    Scanner scanner = new Scanner(System.in);
                    line = scanner.nextLine();
                } catch (Exception e) {
                    System.out.println("Error in reading input.");
                    System.out.println();
                    continue;
                }
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
                if (code.equals("clear")) {
                    server.clear();
                    continue;
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
                    if (login(server, values[1], values[2])) {
                        break;
                    }
                    continue;
                }
                if (values.length < 4) {
                    System.out.println("Missing keywords.");
                    System.out.println();
                    continue;
                }
                if (code.equals("register")) {
                    if (register(server, values[1], values[2], values[3])) {
                        break;
                    }
                    continue;
                }
                System.out.println("No valid keyword received. Please try again");
                System.out.println();
            } catch (Exception e) {
                System.out.println("An unknown error has occurred. Please contact the software provider.");
                System.out.println();
                break;
            }
        }
    }

    static boolean loggedIn(ServerFacade server, String username, String authToken) { //returns true if quitting
        // application
        Collection<GameData> games; HashMap<Integer, GameData> gameDict = new HashMap<>();
        ServerMessageHandler serverMessageHandler = serverMessage -> {
            if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
                System.out.println("An error has arisen. " + serverMessage.errorMessage);
            }
            if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
                System.out.println(serverMessage.message);
            }
            if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
                GameData gameData = new GameData(serverMessage.game);
                observe(gameData, username); System.out.printf("[GAME] >>> ");
            }
        };
        while (true) {
            try {
                System.out.printf("[LOGGED_IN] >>> "); Scanner scanner = new Scanner(System.in);
                String line = scanner.nextLine(); var values = line.split(" ");
                if (values.length < 1) {
                    System.out.println("Please input a keyword."); System.out.println(); continue;
                }
                String code = values[0];
                if (code.equals("help")) {
                    System.out.println("create <NAME> - to create a game"); System.out.println("list - games");
                    System.out.println("join <ID> [WHITE|BLACK] - a game"); System.out.println("observe <ID> - a game");
                    System.out.println("logout - logs out the user"); System.out.println("quit - to quit application");
                    System.out.println("help - with possible commands"); System.out.println(); continue;
                }
                if (code.equals("logout")) {
                    System.out.println("Logging out."); server.logout(); System.out.println(); return false;
                }
                if (code.equals("quit")) {
                    return true;
                }
                if (code.equals("list")) {
                    HashMap<Integer, GameData> temp = listGames(server);
                    if (temp != null) {
                        gameDict = temp;
                    }
                    continue;
                }
                if (values.length < 2) {
                    System.out.println("Missing keywords."); System.out.println(); continue;
                }
                if (code.equals("create")) {
                    BlanketResponse response = server.createGame(values[1]);
                    if (response.message() != null) {
                        System.out.println("Failed to create game, try using a different game name.");
                        System.out.println(); continue;
                    }
                    System.out.println("Created game."); System.out.println(); continue;
                }
                if (code.equals("observe")) {
                    int gameNum;
                    try {
                        gameNum = Integer.parseInt(values[1]);
                    } catch (Exception e) {
                        System.out.println("Game number was not a number."); System.out.println(); continue;
                    }
                    observeFun(gameNum, gameDict, authToken, username, server, serverMessageHandler); continue;
                }
                if (values.length < 3) {
                    System.out.println("Missing keywords."); System.out.println(); continue;
                }
                if (code.equals("join")) {
                    int gameNum;
                    try {
                        gameNum = Integer.parseInt(values[1]);
                    } catch (Exception e) {
                        System.out.println("Game number was not a number.");
                        System.out.println(); continue;
                    }
                    if (!gameDict.containsKey(gameNum)) {
                        System.out.println("No game with that number. Please try again.");
                        System.out.println(); continue;
                    }
                    if (!values[2].equals("WHITE") && !values[2].equals("BLACK")) {
                        System.out.println("Invalid team color. Please try again, WHITE or BLACK.");
                        System.out.println(); continue;
                    }
                    try {
                        GameData game = gameDict.get(gameNum); server.joinGame(values[2], game.gameID());
                    } catch (Exception e) {
                        System.out.println("That color has already been claimed."); System.out.println(); continue;
                    }
                    System.out.println("Successful in joining game " + values[1] + " as " + values[2] + ".");
                    System.out.println(); gameDict = listGames(server); GameData game = gameDict.get(gameNum);
                    WebSocketFacade webSocketFacade = new WebSocketFacade(URL, username, serverMessageHandler);
                    webSocketFacade.connectGame(authToken, game.gameID());
                    GameUI gui = new GameUI(webSocketFacade, authToken, server, game.gameID(), username, values[2]);
                    gui.run();
                }
            } catch (Exception e) {
                System.out.println("An unknown error has occurred. Please contact the software provider.");
                System.out.println(); return true;
            }
        }
    }

    static void observe(GameData gameData, String username) {
        ChessGame game = gameData.game();
        ChessBoard board = game.getBoard();
        int j;
        System.out.println();
        if (username.equals(gameData.blackUsername())) {
            System.out.print("    H\u2003 G\u2003 F\u2003 E\u2003 D\u2003 C\u2003 B\u2003 A\u2003");
            System.out.println();
            for (int i = 1; i <= 8; i++) {
                System.out.print(" " + (i) + " ");
                for (j = 1; j <= 8; j++) {
                    printSquare(i, j, board);
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
                printSquare(i, j, board);
            }
            System.out.print(EscapeSequences.RESET_BG_COLOR);
            System.out.print(" " + (i) + " ");
            System.out.println();
        }
        System.out.print("    A\u2003 B\u2003 C\u2003 D\u2003 E\u2003 F\u2003 G\u2003 H\u2003");
        System.out.println();
        return;
    }

    static private HashMap<Integer, GameData> listGames(ServerFacade server) {
        BlanketResponse response = server.listGames();
        Collection<GameData> games;
        HashMap<Integer, GameData> gameDict;
        if (response.games() != null) {
            if (response.games().isEmpty()) {
                System.out.println("No games currently active."); System.out.println(); return null;
            }
            games = response.games(); int i = 1; gameDict = new HashMap<>();
            for (GameData game : games) {
                System.out.printf("" + i + ") Name: " + game.gameName() + ", White: " + game.whiteUsername() +
                        ", Black: " + game.blackUsername() + "%n");
                gameDict.put(i++, game);
            }
            System.out.println(); return gameDict;
        }
        System.out.println("No games currently active."); System.out.println(); return null;
    }


    static private void printSquare(int i, int j, ChessBoard board) {
        System.out.print(((((i + j) % 2) == 1) ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY :
                EscapeSequences.SET_BG_COLOR_DARK_GREY));
        ChessPiece piece = board.getPiece(new ChessPosition(i, j));
        String output = EscapeSequences.EMPTY;
        if (piece != null) {
            output = getPiece(piece.hashCode());
        };
        System.out.print(output);
    }

    static private boolean login(ServerFacade server, String username, String password) {
        String authToken;
        try {
            BlanketResponse response = server.login(username, password);
            authToken = response.authToken();
            if (response.message() != null) {
                System.out.println("Error in login, please try again");
                System.out.println();
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error in login, please try again");
            System.out.println();
            return false;
        }
        System.out.println("Successful login");
        System.out.println();
        if (loggedIn(server, username, authToken)) {
            return true;
        }
        return false;
    }

    static private boolean register(ServerFacade server, String username, String password, String email) {
        String authToken;
        try {
            BlanketResponse response = server.register(username, password, email);
            authToken = response.authToken();
            if (response.message() != null) {
                System.out.println("Error in register, please try again");
                System.out.println();
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error in register, please try again");
            System.out.println();
            return false;
        }
        System.out.println("Successful registration");
        System.out.println();
        if (loggedIn(server, username, authToken)) {
            return true;
        }
        return false;
    }

    static private void observeFun(int gameNum, HashMap<Integer, GameData> gameDict, String authToken, String username,
                            ServerFacade server, ServerMessageHandler serverMessageHandler) {
        if (!gameDict.containsKey(gameNum)) {
            System.out.println("No game with that number. Please try again."); System.out.println(); return;
        }
        try {
            GameData game = gameDict.get(gameNum); System.out.println(); WebSocketFacade webSocketFacade = new
                    WebSocketFacade(URL, username, serverMessageHandler);
            webSocketFacade.connectGame(authToken, game.gameID());
            GameUI gui = new GameUI(webSocketFacade, authToken, server, game.gameID(), username, "observer");
            gui.run();
        } catch (Exception e) {
            System.out.println("Unsuccessful observing the game. "); System.out.println();
        }
    }
}