package com.thirdplace.roomchat;

import java.io.IOException;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/chat")
public class RoomChatWebsocketEndpoint {

    @OnOpen
    public void onOpen(final Session session) {
        try {
            // Send a welcome message to the client
            session.getBasicRemote().sendText("Welcome to the WebSocket server!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(final String message, final Session session) {
        try {
            session.getBasicRemote().sendText("Message received: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(final Session session) {
    }

    @OnError
    public void onError(final Session session, final Throwable throwable) {
        throwable.printStackTrace();
    }

}
