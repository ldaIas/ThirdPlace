package com.thirdplace;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;

import com.thirdplace.roomchat.RoomChatWebsocketEndpoint;

public class ThirdPlaceService {

    // default websocket timeout of 5 minutes
    static final long DEFAULT_TIMEOUT = 5 * 60_000;
    
    public static void main(String[] args) throws Exception {

        final Server server = new Server(8080);
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
    }
}
