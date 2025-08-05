package server.websocket;

import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.websocket.api.Session;

public class ConnectionManager {
    public ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    public void add(String username, String authToken, Session session) {
        Connection connection = new Connection(username, authToken, session);
    }
}
