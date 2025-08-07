package websocketfacade;

import websocket.messages.ServerMessage;

public interface ServerMessageHandler {
    void notify(ServerMessage notification);
}