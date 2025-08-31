package com.thirdplace;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import com.thirdplace.db.DatabaseManager;
import com.thirdplace.db.PostsTableManager;
import com.thirdplace.db.RSVPsTableManager;
import com.thirdplace.db.DatabaseConfig.DataSourceCacheKey;
import com.thirdplace.endpoints.CorsFilter;
import com.thirdplace.endpoints.PostsEndpoints;
import com.thirdplace.endpoints.AuthEndpoints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static final DataSourceCacheKey DEFAULT_APP_DATASOURCE = new DataSourceCacheKey("app");

    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting server...");

        // Set the app data source
        AppDataSource.setAppDatasource(DEFAULT_APP_DATASOURCE);

        DatabaseManager.testConnection();
        
        LOGGER.info("Creating tables for application");
        PostsTableManager.getInstance();
        RSVPsTableManager.getInstance();
        LOGGER.info("Database tables initialized");
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(mapper);
        
        ResourceConfig config = new ResourceConfig();
        config.register(PostsEndpoints.class);
        config.register(AuthEndpoints.class);
        config.register(provider);
        config.register(new CorsFilter());
        
        Server server = JettyHttpContainerFactory.createServer(URI.create("http://localhost:8080/"), config);
        
        LOGGER.info("Server started on http://localhost:8080");
        server.join();
    }
}