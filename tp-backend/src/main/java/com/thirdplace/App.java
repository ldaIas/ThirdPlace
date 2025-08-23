package com.thirdplace;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import com.thirdplace.db.DatabaseManager;
import com.thirdplace.db.PostsTableManager;
import com.thirdplace.endpoints.PostsEndpoints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    
    public static void main(String[] args) throws Exception {
        DatabaseManager.testConnection();
        
        PostsTableManager postsManager = PostsTableManager.getInstance();
        postsManager.createTable();
        System.out.println("Database tables initialized");
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(mapper);
        
        ResourceConfig config = new ResourceConfig();
        config.register(HelloWorldResource.class);
        config.register(PostsEndpoints.class);
        config.register(provider);
        
        Server server = JettyHttpContainerFactory.createServer(URI.create("http://localhost:8080/"), config);
        
        LOGGER.info("Server started on http://localhost:8080");
        server.join();
    }
}