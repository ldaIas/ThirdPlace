package com.thirdplace;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import java.net.URI;

public class App {
    public static void main(String[] args) throws Exception {
        ResourceConfig config = new ResourceConfig();
        config.register(HelloWorldResource.class);
        config.register(JacksonFeature.class);
        
        Server server = JettyHttpContainerFactory.createServer(URI.create("http://localhost:8080/"), config);
        
        System.out.println("Server started on http://localhost:8080");
        server.join();
    }
}