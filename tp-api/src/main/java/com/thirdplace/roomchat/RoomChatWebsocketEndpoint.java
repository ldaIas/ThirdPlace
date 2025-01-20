package com.thirdplace.roomchat;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/chat")
public class RoomChatWebsocketEndpoint {

    static final Logger LOGGER = LoggerFactory.getLogger(RoomChatWebsocketEndpoint.class);

    @OnOpen
    public void onOpen(final Session session) {
        try {
            LOGGER.info("WebSocket connection opened: " + session.getId());
            // Send a welcome message to the client
            session.getBasicRemote().sendText("Welcome to the WebSocket server!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(final String message, final Session session) {
        try {
            LOGGER.debug("Message recieved " + message);
            session.getBasicRemote().sendText("Message received: " + message);
        } catch (IOException e) {
            LOGGER.error("Error sending message", e);
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(final Session session) {
        LOGGER.info("WebSocket connection closed: " + session.getId());
    }

    @OnError
    public void onError(final Session session, final Throwable throwable) {
        LOGGER.error("WebSocket error: " + session.getId(), throwable);
        LOGGER.error("throwable.printStackTrace()");
    }

}
