package com.thirdplace.roomchat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdplace.utils.JsonUtils;
import com.thirdplace.utils.JsonUtilsException;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/chat")
public class RoomChatWebsocketEndpoint {

    static final Logger LOGGER = LoggerFactory.getLogger(RoomChatWebsocketEndpoint.class);

    static final String SERVER_NAME = "server";

    private static final Set<Session> sessionSet = new HashSet<>();

    @OnOpen
    public void onOpen(final Session session) {
        LOGGER.info("WebSocket connection opened: " + session.getId());

        final String convoId = UUID.randomUUID().toString();
        final ChatMessage initMessage = new ChatMessage(SERVER_NAME, "Connected to chat service", convoId);
        final String messageAsString = JsonUtils.toJson(initMessage);
        sessionSet.add(session);
        try {
            // Send a welcome message to the client
            session.getBasicRemote().sendText(messageAsString);
        } catch (IOException e) {
            LOGGER.error("Error sending welcome message", e);
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(final String message, final Session session) {

        try {
            LOGGER.debug("Message recieved " + message);

            final ChatMessage chatMessage = JsonUtils.fromJson(message, ChatMessage.class);

            final String user = chatMessage.username();
            final String respMsg = String.format("[%1$s]: %2$s", user, chatMessage.message());
            final ChatMessage response = new ChatMessage(SERVER_NAME, respMsg, chatMessage.conversationId());
            final String responseAsString = JsonUtils.toJson(response);
            session.getBasicRemote().sendText(responseAsString);

            // Broadcast the message to all connected sessions
            sessionSet.forEach(otherSession -> {
                if (otherSession.isOpen() && !session.equals(otherSession)) {
                    otherSession.getAsyncRemote().sendText(responseAsString);
                }
            });

        } catch (JsonUtilsException e) {
            try {
                final ChatMessage error = new ChatMessage(SERVER_NAME, "Error: " + e.getMessage(), null);
                final String errorAsString = JsonUtils.toJson(error);
                session.getBasicRemote().sendText(errorAsString);
            } catch (IOException e1) {
                LOGGER.error("Error sending error message after deserialization error", e1);
                e1.printStackTrace();
            }

        } catch (IOException e) {
            LOGGER.error("Error sending message", e);
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(final Session session) {
        LOGGER.info("WebSocket connection closed: " + session.getId());
        sessionSet.remove(session);
    }

    @OnError
    public void onError(final Session session, final Throwable throwable) {
        LOGGER.error("WebSocket error: " + session.getId(), throwable);

        final ChatMessage error = new ChatMessage(SERVER_NAME, "Error: " + throwable.getMessage(), null);
        final String errorAsString = JsonUtils.toJson(error);
        try {
            session.getBasicRemote().sendText(errorAsString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
