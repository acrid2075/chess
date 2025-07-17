package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.GameData;
import model.UserData;
import spark.*;
import service.*;
import requests.*;
import results.*;


import java.util.Collection;
import java.util.Map;

public class Server {

    public int run(int desiredPort) {
        GameDAO gameDAO = new MemGameDAO();
        AuthDAO authDAO = new MemAuthDAO();
        UserDAO userDAO = new MemUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);

        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        Spark.delete("/db", (req, res) -> clearService.clear());


        Spark.post("/user", new Route() {
            public Object handle(Request req, Response res) {
                var serializer = new Gson();
                UserData userData = serializer.fromJson(req.body(), UserData.class);
                if ((userData.username() == null) || (userData.password() == null)) {
                    res.status(400);
                    var body = serializer.toJson(Map.of("message","Error: username or password null" ));
                    res.body(body);
                    return body;
                }

                if (userService.isUser(userData.username())) {
                    res.status(403);
                    var body = serializer.toJson(Map.of("message","Error: already taken" ));
                    res.body(body);
                    return body;
                }
                try {
                    RegisterResult result = userService.register(new RegisterRequest(userData));
                    res.status(200);
                    var body = serializer.toJson(Map.of("username", result.username(), "authToken",
                            result.authToken()));
                    res.body(body);
                    return body;
                } catch (AlreadyTakenException e) {
                    res.status(403);
                    var body = serializer.toJson(Map.of("message","Error: already taken" ));
                    res.body(body);
                    return body;
                } catch (Exception e) {
                    res.status(500);
                    var body = serializer.toJson(Map.of("message",e.getClass().toString() ));
                    res.body(body);
                    return body;
                }
            }
        });

        Spark.post("/session", new Route() {
            public Object handle(Request req, Response res) {
                var serializer = new Gson();
                LoginRequest loginRequest = serializer.fromJson(req.body(), LoginRequest.class);
                if ((loginRequest.password() == null) || (loginRequest.username() == null)) {
                    res.status(400);
                    var body = serializer.toJson(Map.of("message","Error: bad request" ));
                    res.body(body);
                    return body;
                }
                if (!userService.isUser(loginRequest.username())) {
                    res.status(401);
                    var body = serializer.toJson(Map.of("message","Error: unauthorized" ));
                    res.body(body);
                    return body;
                }
                try {
                    LoginResult result = userService.login(loginRequest);
                    res.status(200);
                    var body = serializer.toJson(Map.of("username", result.authData().username(), "authToken",
                            result.authData().authToken()));
                    res.body(body);
                    return body;
                } catch (IncorrectPasswordException e) {
                    res.status(401);
                    var body = serializer.toJson(Map.of("message", "Error: unauthorized" + e.getMessage() ));
                    res.body(body);
                    return body;
                } catch (DataAccessException e) {
                    res.status(500);
                    var body = serializer.toJson(Map.of("message", "Unclear but failed on data access." ));
                    res.body(body);
                    return body;
                } catch (Exception e) {
                    res.status(500);
                    var body = serializer.toJson(Map.of("message", e.getClass().toString() ));
                    res.body(body);
                    return body;
                }
            }
        });

        Spark.delete("/session", new Route() {
            public Object handle(Request req, Response res) {
                var serializer = new Gson();
                String authToken = req.headers("authorization");
                if (authToken == null) {
                    res.status(401);
                    var body = serializer.toJson(Map.of("message","Error: unauthorized" ));
                    res.body(body);
                    return body;
                }

                try {
                    userService.logout(new LogoutRequest(authToken));
                    res.status(200);
                    var body = serializer.toJson(Map.of());
                    res.body(body);
                    return body;
                } catch (DataAccessException e) {
                    res.status(401);
                    var body = serializer.toJson(Map.of("message","Error: unauthorized" ));
                    res.body(body);
                    return body;
                } catch (Exception e) {
                    res.status(500);
                    var body = serializer.toJson(Map.of("message",e.getClass().toString() ));
                    res.body(body);
                    return body;
                }
            }
        });



        Spark.get("/game", new Route() {
            public Object handle(Request req, Response res) {
                var serializer = new Gson();
                String authToken = req.headers("authorization");
                if (authToken == null) {
                    res.status(401);
                    var body = serializer.toJson(Map.of("message","Error: unauthorized" ));
                    res.body(body);
                    return body;
                }

                if (userService.getAuth(authToken) == null) {
                    res.status(401);
                    var body = serializer.toJson(Map.of("message","Error: unauthorized" ));
                    res.body(body);
                    return body;
                }

                try {
                    Collection<GameData> games = gameService.listGames();
                    res.status(200);
                    var body = serializer.toJson(Map.of("games", games));
                    res.body(body);
                    return body;
                } catch (Exception e) {
                    res.status(500);
                    var body = serializer.toJson(Map.of("message",e.getClass().toString() ));
                    res.body(body);
                    return body;
                }
            }
        });



        Spark.post("/game", new Route() {
            public Object handle(Request req, Response res) {
                var serializer = new Gson();
                record GameName (String gameName) {};
                GameName gameName = serializer.fromJson(req.body(), GameName.class);
                String authToken = req.headers("authorization");
                if (authToken == null) {
                    res.status(401);
                    var body = serializer.toJson(Map.of("message","Error: unauthorized" ));
                    res.body(body);
                    return body;
                }

                if (gameName.gameName() == null) {
                    res.status(400);
                    var body = serializer.toJson(Map.of("message","Error: Bad Request" ));
                    res.body(body);
                    return body;
                }

                if (userService.getAuth(authToken) == null) {
                    res.status(401);
                    var body = serializer.toJson(Map.of("message","Error: unauthorized" ));
                    res.body(body);
                    return body;
                }
                try {
                    GameData gameData = gameService.createGame(new CreateGamesRequest(gameName.gameName()));
                    res.status(200);
                    var body = serializer.toJson(Map.of("gameID", gameData.gameID()));
                    res.body(body);
                    return body;
                } catch (DataAccessException e) {
                    res.status(400);
                    var body = serializer.toJson(Map.of("message","Error: bad request" ));
                    res.body(body);
                    return body;
                } catch (Exception e) {
                    res.status(500);
                    var body = serializer.toJson(Map.of("message",e.getClass().toString() ));
                    res.body(body);
                    return body;
                }
            }
        });

        Spark.put("/game", new Route() {
            public Object handle(Request req, Response res) {
                var serializer = new Gson();
                record ColorID(String playerColor, int gameID) {};
                ColorID colorID = serializer.fromJson(req.body(), ColorID.class);
                String authToken = req.headers("authorization");
                if (authToken == null) {
                    res.status(401);
                    var body = serializer.toJson(Map.of("message","Error: unauthorized" ));
                    res.body(body);
                    return body;
                }

                if (userService.getAuth(authToken) == null) {
                    res.status(401);
                    var body = serializer.toJson(Map.of("message","Error: unauthorized" ));
                    res.body(body);
                    return body;
                }

                if ((colorID.playerColor() == null) || (colorID.playerColor().equals(""))) {
                    res.status(400);
                    var body = serializer.toJson(Map.of("message","Error: Bad Request"));
                    res.body(body);
                    return body;
                }

                if ((!colorID.playerColor().equals("WHITE")) && ((!colorID.playerColor().equals("BLACK")))) {
                    res.status(400);
                    var body = serializer.toJson(Map.of("message","Error: Bad Request"));
                    res.body(body);
                    return body;
                }

                if (gameService.getGame(colorID.gameID()) == null) {
                    res.status(400);
                    var body = serializer.toJson(Map.of("message","Error: Bad Request" ));
                    res.body(body);
                    return body;
                }

                if (colorID.playerColor().equals("BLACK")) {
                    if (gameService.getGame(colorID.gameID()).blackUsername() != null) {
                        res.status(403);
                        var body = serializer.toJson(Map.of("message","Error: already taken" ));
                        res.body(body);
                        return body;
                    }
                }

                if (colorID.playerColor().equals("WHITE")) {
                    if (gameService.getGame(colorID.gameID()).whiteUsername() != null) {
                        res.status(403);
                        var body = serializer.toJson(Map.of("message","Error: already taken" ));
                        res.body(body);
                        return body;
                    }
                }

                try {
                    GameData gameData = gameService.joinGame(new JoinGameRequest(colorID.gameID(),
                            userService.getAuth(authToken).username(), colorID.playerColor()));
                    res.status(200);
                    var body = serializer.toJson(Map.of());
                    res.body(body);
                    return body;
                } catch (Exception e) {
                    res.status(500);
                    var body = serializer.toJson(Map.of("message",e.getClass().toString() ));
                    res.body(body);
                    return body;
                }
            }
        });

        //This line initializes the server and can be removed once you have a functioning endpoint 
//        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

}

