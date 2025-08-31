package com.thirdplace.endpoints;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.thirdplace.db.DatabaseConfig.DataSourceCacheKey;
import com.thirdplace.AppDataSource;
import com.thirdplace.db.DatabaseManager;
import com.thirdplace.db.PostsTableManager;
import com.thirdplace.services.PostsService.CreatePostRequest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import static com.thirdplace.endpoints.EndpointTestUtils.assertResponseStatus;

class PostsEndpointsTest extends JerseyTest {

    private static final DataSourceCacheKey TEST_DATASOURCE_KEY = new DataSourceCacheKey("test_posts_endpoints_schema");
    private ObjectMapper objectMapper;

    @Override
    protected Application configure() {
        return new ResourceConfig(PostsEndpoints.class);
    }

    @BeforeAll
    static void setUpClass() throws SQLException {
        AppDataSource.setAppDatasource(TEST_DATASOURCE_KEY);
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS test_posts_endpoints_schema");
        }
        PostsTableManager.getInstance().createTable();

    }

    @AfterAll
    static void tearDownClass() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("DROP SCHEMA IF EXISTS test_posts_endpoints_schema CASCADE");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("DELETE FROM posts");
        }

    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void testCreatePostEndpoint() throws Exception {
        CreatePostRequest request = new CreatePostRequest(
                "Test Title",
                "Test Author",
                "Test Description",
                Instant.now().plusSeconds(3600).toString(),
                5,
                new String[] { "tag1", "tag2" },
                "Test Location",
                40.7128,
                -74.0060,
                Instant.now().plusSeconds(1800).toString(),
                "mixed",
                "social");

        Response response = target("api/Posts:create")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertResponseStatus(response, Status.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());

        String responseBody = response.readEntity(String.class);
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("Test Title"));
        assertTrue(responseBody.contains("Test Author"));
        assertTrue(responseBody.contains("active"));
    }

    @Test
    void testGetAllPostsEndpoint() throws Exception {
        // First create a post
        CreatePostRequest request = new CreatePostRequest(
                "Test Title",
                "Test Author",
                "Test Description",
                Instant.now().plusSeconds(3600).toString(),
                5,
                new String[] { "tag1" },
                "Test Location",
                40.7128,
                -74.0060,
                Instant.now().plusSeconds(1800).toString(),
                "mixed",
                "social");

        target("api/Posts:create")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        // Then get all posts
        Response response = target("api/Posts:getAll")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertResponseStatus(response, Status.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());

        String responseBody = response.readEntity(String.class);
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("Test Title"));
        assertTrue(responseBody.contains("posts"));
    }

    @Test
    void testGetAllPostsEmptyResponse() throws Exception {
        Response response = target("api/Posts:getAll")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertResponseStatus(response, Status.OK);

        String responseBody = response.readEntity(String.class);
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("posts"));
        assertTrue(responseBody.contains("[]"));
    }
}