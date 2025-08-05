package server.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.*;

public class ConnectionManager {
    public ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    public void add(String username, String authToken, Session session) {
        Connection connection = new Connection(username, authToken, session);
        connections.put(username, connection);
    }

    public void remove(String username) {
        connections.remove(username);
    }

    public void broadcast(ServerMessage serverMessage) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                c.send(serverMessage.toString());
            } else {
                removeList.add(c);
            }
        }
        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.username);
        }
    }
}
