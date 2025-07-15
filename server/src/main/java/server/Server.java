package server;

import dataaccess.*;
import spark.*;
import service.*;

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

        Spark.delete("/db", new Route() {
            ClearService service = new ClearService();
            public Object handle(Request req, Response res) {

                return service.clear(req, res);
            }
        });

        Spark.post("/user", new Route() {
            UserService service = new UserService();
            public Object handle(Request req, Response res) {
                return service.register(req, res);
            }
        });

        Spark.post("/session", new Route() {
            UserService service = new UserService();
            public Object handle(Request req, Response res) {
                return service.login(req, res);
            }
        });

        Spark.delete("/session", new Route() {
            UserService service = new UserService();
            public Object handle(Request req, Response res) {
                return service.logout(req, res);
            }
        });

        Spark.get("/game", new Route() {
            GameService service = new GameService();
            public Object handle(Request req, Response res) {
                return service.listGames(req, res);
            }
        });

        Spark.post("/game", new Route() {
            GameService service = new GameService();
            public Object handle(Request req, Response res) {
                return service.createGame(req, res);
            }
        });

        Spark.put("/game", new Route() {
            GameService service = new GameService();
            public Object handle(Request req, Response res) {
                return service.joinGame(req, res);
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

