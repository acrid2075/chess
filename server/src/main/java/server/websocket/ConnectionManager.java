package server.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.*;

public class ConnectionManager {
    public ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer, ArrayList<Connection>> gameCrews = new ConcurrentHashMap<>();
    public void add(String username, String authToken, Session session, int gameID) {
        Connection connection = new Connection(username, authToken, session, gameID);
        connections.put(username, connection);
        if (gameCrews.get(gameID) == null) {
            gameCrews.put(gameID, new ArrayList<Connection>() {
            });
        }
        gameCrews.get(gameID).add(connection);
    }

    public void remove(String username, int gameID) {
        connections.remove(username);
        gameCrews.get(gameID).remove(username);
    }

    public void msg(String username, ServerMessage serverMessage) throws IOException {
        connections.get(username).send(serverMessage.toString());
    }

    public void broadcast(String exceptThis, ServerMessage serverMessage, int gameID) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : gameCrews.get(gameID)) {
            if (c.session.isOpen() && connections.containsValue(c)) {
                if (!c.username.equals(exceptThis)) {
                    c.send(serverMessage.toString());
                }
            } else {
                removeList.add(c);
            }
        }
        // Clean up any connections that were left open.
        for (var c : removeList) {
            remove(c.username, gameID);
        }
    }
}
