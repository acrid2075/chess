package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import spark.*;
import service.*;
import requests.*;
import results.*;


import java.util.Collection;
import java.util.Map;

public class Server {

    public int port;

    public int run(int desiredPort) {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        GameDAO gameDAO = new SysGameDAO();
        AuthDAO authDAO = new SysAuthDAO();
        UserDAO userDAO = new SysUserDAO();
        ClearService clearService = new ClearService(gameDAO, userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO, authDAO);

        Spark.port(desiredPort);
        this.port = desiredPort;

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        Spark.delete("/db", new Route() {
            public Object handle(Request req, Response res) {
                return clear(req, res, clearService, gameService, userService);
            }});


        Spark.post("/user", new Route() {
            public Object handle(Request req, Response res) {
                return register(req, res, clearService, gameService, userService);
            }
        });

        Spark.post("/session", new Route() {
            public Object handle(Request req, Response res) {
                return login(req, res, clearService, gameService, userService);
            }
        });

        Spark.delete("/session", new Route() {
            public Object handle(Request req, Response res) {
                return logout(req, res, clearService, gameService, userService);
            }
        });

        Spark.get("/game", new Route() {
            public Object handle(Request req, Response res) {
                return listGames(req, res, clearService, gameService, userService);
            }
        });

        Spark.post("/game", new Route() {
            public Object handle(Request req, Response res) {
                return createGame(req, res, clearService, gameService, userService);
            }
        });

        Spark.put("/game", new Route() {
            public Object handle(Request req, Response res) {
                return joinGame(req, res, clearService, gameService, userService);
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

    private Object register(Request req, Response res, ClearService clearService, GameService gameService,
    UserService userService) {
        var serializer = new Gson();
        try {
            UserData tempuserData = serializer.fromJson(req.body(), UserData.class);
            UserData userData = new UserData(tempuserData.username(), tempuserData.password(), tempuserData.email());
            if ((userData.username() == null) || (tempuserData.password() == null)) {
                res.status(400);
                var body = serializer.toJson(Map.of("message", "Error: username or password null"));
                res.body(body);
                return body;
            }

            if (userService.isUser(userData.username())) {
                res.status(403);
                var body = serializer.toJson(Map.of("message", "Error: already taken"));
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
                var body = serializer.toJson(Map.of("message", "Error: already taken"));
                res.body(body);
                return body;
            } catch (Exception e) {
                // Addresses internal errors for register
                return internalError(res, serializer, e);
            }
        } catch (Exception e) {
            return internalError(res, serializer, e);
        }
    }
    private Object login (Request req, Response res, ClearService clearService, GameService gameService,
                            UserService userService) {
        var serializer = new Gson();
        try {
            LoginRequest loginRequest = serializer.fromJson(req.body(), LoginRequest.class);
            if ((loginRequest.password() == null) || (loginRequest.username() == null)) {
                res.status(400);
                var body = serializer.toJson(Map.of("message", "Error: bad request"));
                res.body(body);
                return body;
            }
            if (!userService.isUser(loginRequest.username())) {
                res.status(401);
                var body = serializer.toJson(Map.of("message", "Error: unauthorized"));
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
                var body = serializer.toJson(Map.of("message", "Error: unauthorized" + e.getMessage()));
                res.body(body);
                return body;
            } catch (DataAccessException e) {
                res.status(500);
                var body = serializer.toJson(Map.of("message", "Error: Unclear but failed on data access."));
                res.body(body);
                return body;
            } catch (Exception e) {
                // Addresses internal errors for login
                return internalError(res, serializer, e);
            }
        } catch (Exception e) {
            return internalError(res, serializer, e);
        }
    }

    private Object logout(Request req, Response res, ClearService clearService, GameService gameService,
                          UserService userService) {
        var serializer = new Gson();
        try {
            String authToken = req.headers("authorization");
            if (authToken == null) {
                res.status(401);
                var body = serializer.toJson(Map.of("message", "Error: unauthorized"));
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
                var body = serializer.toJson(Map.of("message", "Error: unauthorized"));
                res.body(body);
                return body;
            } catch (Exception e) {
                return internalError(res, serializer, e);
            }
        } catch (Exception e) {
            return internalError(res, serializer, e);
        }
    }

    private Object joinGame(Request req, Response res, ClearService clearService, GameService gameService,
                          UserService userService) {
        var serializer = new Gson();
        try {
            record ColorID(String playerColor, int gameID) {
            }
            ColorID colorID = serializer.fromJson(req.body(), ColorID.class);
            String authToken = req.headers("authorization");
            if (authToken == null) {
                res.status(401);
                var body = serializer.toJson(Map.of("message", "Error: unauthorized"));
                res.body(body);
                return body;
            }

            if (userService.getAuth(authToken) == null) {
                res.status(401);
                var body = serializer.toJson(Map.of("message", "Error: unauthorized"));
                res.body(body);
                return body;
            }

            if ((colorID.playerColor() == null) || (colorID.playerColor().equals(""))) {
                res.status(400);
                var body = serializer.toJson(Map.of("message", "Error: Bad Request"));
                res.body(body);
                return body;
            }

            if ((!colorID.playerColor().equals("WHITE")) && ((!colorID.playerColor().equals("BLACK")))) {
                res.status(400);
                var body = serializer.toJson(Map.of("message", "Error: Bad Request"));
                res.body(body);
                return body;
            }

            if (gameService.getGame(colorID.gameID()) == null) {
                res.status(400);
                var body = serializer.toJson(Map.of("message", "Error: Bad Request"));
                res.body(body);
                return body;
            }

            if (colorID.playerColor().equals("BLACK")) {
                if (gameService.getGame(colorID.gameID()).blackUsername() != null) {
                    res.status(403);
                    var body = serializer.toJson(Map.of("message", "Error: already taken"));
                    res.body(body);
                    return body;
                }
            }

            if (colorID.playerColor().equals("WHITE")) {
                if (gameService.getGame(colorID.gameID()).whiteUsername() != null) {
                    res.status(403);
                    var body = serializer.toJson(Map.of("message", "Error: already taken"));
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
                return internalError(res, serializer, e);
            }
        } catch (Exception e) {
            return internalError(res, serializer, e);
        }
    }

    private Object createGame(Request req, Response res, ClearService clearService, GameService gameService,
                            UserService userService) {
        var serializer = new Gson();
        try {
            record GameName(String gameName) {
            }
            GameName gameName = serializer.fromJson(req.body(), GameName.class);
            String authToken = req.headers("authorization");
            if (authToken == null) {
                res.status(401);
                var body = serializer.toJson(Map.of("message", "Error: unauthorized"));
                res.body(body);
                return body;
            }

            if (gameName.gameName() == null) {
                res.status(400);
                var body = serializer.toJson(Map.of("message", "Error: Bad Request"));
                res.body(body);
                return body;
            }

            if (userService.getAuth(authToken) == null) {
                res.status(401);
                var body = serializer.toJson(Map.of("message", "Error: unauthorized"));
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
                var body = serializer.toJson(Map.of("message", "Error: bad request"));
                res.body(body);
                return body;
            } catch (Exception e) {
                // Addresses internal errors for create game
                return internalError(res, serializer, e);
            }
        } catch (Exception e) {
            return internalError(res, serializer, e);
        }
    }

    private Object internalError(Response res, Gson serializer, Exception e) {
        res.status(500);
        var body = serializer.toJson(Map.of("message", "Internal Error: " + e.getClass().toString()));
        res.body(body);
        return body;
    }

    private Object clear(Request req, Response res, ClearService clearService, GameService gameService,
                              UserService userService) {
        var serializer = new Gson();
        try {
            res.status(200);
            return clearService.clear();
        } catch (Exception e) {
            return internalError(res, serializer, e);
        }
    }

    private Object listGames(Request req, Response res, ClearService clearService, GameService gameService,
                         UserService userService) {
        var serializer = new Gson();
        try {
            String authToken = req.headers("authorization");
            if (authToken == null) {
                res.status(401);
                var body = serializer.toJson(Map.of("message", "Error: unauthorized"));
                res.body(body);
                return body;
            }

            if (userService.getAuth(authToken) == null) {
                res.status(401);
                var body = serializer.toJson(Map.of("message", "Error: unauthorized"));
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
                return internalError(res, serializer, e);
            }
        } catch (Exception e) {
            return internalError(res, serializer, e);
        }
    }

}

