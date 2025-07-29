package client;

import org.junit.jupiter.api.*;
import response.BlanketResponse;
import server.Server;
import ui.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade client;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        client = new ServerFacade("http://localhost:" + port);
        
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void clearTestTrue() {
        client.clear();
        client.register("me", "my", "mo");
        assert ((client.listGames().games() == null) || client.listGames().games().isEmpty());
    }

    @Test
    public void clearTestFalse() {
        client.clear();
        client.register("andy", "p", "acrid");
        client.clear();
        boolean success = false;
        try{
            client.login("andy", "p");
        } catch (Exception e) {
            success = true;
        }
        assert success;
    }

    @Test
    public void registerTestTrue() {
        
        client.clear();
        BlanketResponse response = client.register("dan", "brown", "dan.brown@byu.edu");
        assert (response != null);
        assert (response.authToken() != null);

    }

    @Test
    public void registerTestFalse() {
        
        client.clear();
        client.register("dan", "brown", "dan.brown@byu.edu");
        boolean success = false;
        try{
            client.register("dan", "brown", "dan.brown@byu.edu");
        } catch (Exception e) {
            success = true;
        }
        assert success;
    }

    @Test
    public void loginTestTrue() {
        
        client.clear();
        client.register("dan", "brown", "dan.brown@byu.edu");
        client.logout();
        BlanketResponse response = client.login("dan", "brown");
        assert (response != null);
        assert (response.authToken() != null);
    }

    @Test
    public void loginTestFalse() {
        
        client.clear();
        client.register("dan", "brown", "dan.brown@byu.edu");
        client.logout();
        boolean success = false;
        try{
            client.login("sam", "brown");
        } catch (Exception e) {
            success = true;
        }
        assert success;
    }

    @Test
    public void logoutTestTrue() {
        
        client.clear();
        BlanketResponse response = client.register("dan", "brown", "dan.brown@byu.edu");
        client.logout();
        assert true;
    }

    @Test
    public void logoutTestFalse() {
        
        client.clear();
        boolean success = false;
        try{
            client.logout();
        } catch (Exception e) {
            success = true;
        }
        assert success;
    }

    @Test
    public void listGamesTestTrue() {
        
        client.clear();
        client.register("dan", "brown", "dan.brown@byu.edu");
        client.createGame("game1");
        BlanketResponse response = client.listGames();
        assert (response != null);
        assert (response.message() == null);
    }

    @Test
    public void listGamesTestFalse() {
        
        client.clear();
        client.register("dan", "brown", "dan.brown@byu.edu");
        BlanketResponse response = client.listGames();
        assert (response != null);
        assert (response.games() != null);
        assert (response.games().isEmpty());
    }

    @Test
    public void createGameTestTrue() {
        
        client.clear();
        client.register("dan", "brown", "dan.brown@byu.edu");
        BlanketResponse response = client.createGame("game1");
        assert (response != null);
        assert (response.message() == null);
    }

    @Test
    public void createGameTestFalse() {
        
        client.clear();
        client.register("dan", "brown", "dan.brown@byu.edu");
        client.createGame("game1");
        boolean success = false;
        try{
            client.createGame("game1");
        } catch (Exception e) {
            success = true;
        }
        assert success;
    }

    @Test
    public void joinGameTestTrue() {
        
        client.clear();
        client.register("dan", "brown", "dan.brown@byu.edu");
        BlanketResponse response = client.createGame("game1");
        client.listGames();
        client.joinGame("BLACK", response.gameID());
        assert true;
    }

    @Test
    public void joinGameTestFalse() {
        
        client.clear();
        client.register("dan", "brown", "dan.brown@byu.edu");
        BlanketResponse response = client.createGame("game1");
        client.listGames();
        client.joinGame("BLACK", response.gameID());
        client.logout();
        client.register("sam", "brown", "sam.brown@byu.edu");
        client.listGames();
        boolean success = false;
        try{
            client.joinGame("BLACK", response.gameID());
        } catch (Exception e) {
            success = true;
        }
        assert success;
    }

}
