package com.thirdplace.service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdplace.roomchat.RoomChatWebsocketEndpoint;

public class ThirdPlaceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThirdPlaceService.class);

    // default websocket timeout of 5 minutes
    static final long DEFAULT_TIMEOUT = 5 * 60_000;

    static final int PORT = 8080;

    public static void main(String[] args) {

        try {
            LOGGER.info("Starting ThirdPlaceService on port " + PORT);

            final Server server = new Server(PORT);
            final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            server.setHandler(context);

            JakartaWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) -> {
                wsContainer.setDefaultMaxTextMessageBufferSize(65536);
                wsContainer.setDefaultMaxSessionIdleTimeout(DEFAULT_TIMEOUT);
                wsContainer.addEndpoint(RoomChatWebsocketEndpoint.class);
            });

            server.start();

            Thread.sleep(10000);

            server.join();
            server.stop();
        } catch (Exception e) {
            LOGGER.error("Critical error while running ThirdPlaceService", e);
            throw new ThirdPlaceServiceException(ThirdPlaceServiceException.ErrorCode.ERROR_WHILE_RUNNING,
                    "Critical error while running ThirdPlaceService", e);
        }
    }

}
