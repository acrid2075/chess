package server.websocket;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    public String username;
    public String authToken;
    public Session session;
    public int gameID;
    public Connection(String username, String authToken, Session session, int gameID) {
        this.username = username;
        this.authToken = authToken;
        this.session = session;
        this.gameID = gameID;
    }

    public void send(String message) throws IOException {
        session.getRemote().sendString(message);
    }
}
