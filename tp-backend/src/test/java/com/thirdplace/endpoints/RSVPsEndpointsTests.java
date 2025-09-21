package com.thirdplace.endpoints;

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
import com.thirdplace.db.RSVPsTableManager;
import com.thirdplace.services.PostsService.CreatePostRequest;
import com.thirdplace.services.PostsService.CreatePostResponse;
import com.thirdplace.services.RSVPsService.CreateRSVPRequest;
import com.thirdplace.services.RSVPsService.UpdateRSVPStatusRequest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import static com.thirdplace.endpoints.EndpointTestUtils.assertResponseStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RSVPsEndpointsTests extends JerseyTest {

    private static final DataSourceCacheKey TEST_DATASOURCE_KEY = new DataSourceCacheKey("test_rsvps_endpoints_schema");
    private ObjectMapper objectMapper;

    @Override
    protected Application configure() {
        return new ResourceConfig(RSVPsEndpoints.class, PostsEndpoints.class);
    }

    @BeforeAll
    static void setUpClass() throws SQLException {
        AppDataSource.setAppDatasource(TEST_DATASOURCE_KEY);
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS test_rsvps_endpoints_schema");
        }
        PostsTableManager.getInstance().createTable();
        RSVPsTableManager.getInstance().createTable();
    }

    @AfterAll
    static void tearDownClass() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("DROP SCHEMA IF EXISTS test_rsvps_endpoints_schema CASCADE");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("DELETE FROM rsvps");
            conn.createStatement().execute("DELETE FROM posts");
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private String createTestPost(String author) throws Exception {
        CreatePostRequest postRequest = new CreatePostRequest(
                "Test Event",
                author,
                "Test Description",
                Instant.now().plusSeconds(3600).toString(),
                3,
                new String[]{"tag1"},
                "Test Location",
                40.7128,
                -74.0060,
                Instant.now().plusSeconds(1800).toString(),
                "mixed",
                "social");

        Response response = target("api/Posts:create")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(postRequest));

        assertResponseStatus(response, Status.OK);
        String responseBody = response.readEntity(String.class);
        CreatePostResponse createResponse = objectMapper.readValue(responseBody, CreatePostResponse.class);
        return createResponse.createdPost().id();
    }

    @Test
    void testCreateRSVPEndpoint() throws Exception {
        String postId = createTestPost("host_user");
        
        CreateRSVPRequest request = new CreateRSVPRequest("guest_user", postId);

        Response response = target("api/RSVPs:create")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertResponseStatus(response, Status.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());

        String responseBody = response.readEntity(String.class);
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("guest_user"));
        assertTrue(responseBody.contains(postId));
        assertTrue(responseBody.contains("PENDING"));
    }

    @Test
    void testCreateRSVP_CannotRSVPToOwnPost() throws Exception {
        String postId = createTestPost("host_user");
        
        CreateRSVPRequest request = new CreateRSVPRequest("host_user", postId);

        Response response = target("api/RSVPs:create")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertResponseStatus(response, Status.BAD_REQUEST);
        
        String responseBody = response.readEntity(String.class);
        assertTrue(responseBody.contains("Cannot RSVP to your own post"));
    }

    @Test
    void testCreateRSVP_CannotRSVPTwice() throws Exception {
        String postId = createTestPost("host_user");
        
        CreateRSVPRequest request = new CreateRSVPRequest("guest_user", postId);

        // First RSVP should succeed
        Response response1 = target("api/RSVPs:create")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));
        assertResponseStatus(response1, Status.OK);

        // Second RSVP should fail
        Response response2 = target("api/RSVPs:create")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertResponseStatus(response2, Status.BAD_REQUEST);
        
        String responseBody = response2.readEntity(String.class);
        assertTrue(responseBody.contains("User has already RSVPed to this post"));
    }

    @Test
    void testGetRSVPsByPostEndpoint() throws Exception {
        String postId = createTestPost("host_user");
        
        // Create two RSVPs
        CreateRSVPRequest request1 = new CreateRSVPRequest("guest1", postId);
        CreateRSVPRequest request2 = new CreateRSVPRequest("guest2", postId);
        
        target("api/RSVPs:create").request(MediaType.APPLICATION_JSON).post(Entity.json(request1));
        target("api/RSVPs:create").request(MediaType.APPLICATION_JSON).post(Entity.json(request2));

        Response response = target("api/RSVPs:getByPost/" + postId)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertResponseStatus(response, Status.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());

        String responseBody = response.readEntity(String.class);
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("guest1"));
        assertTrue(responseBody.contains("guest2"));
        assertTrue(responseBody.contains("rsvps"));
    }

    @Test
    void testGetRSVPsByUserEndpoint() throws Exception {
        String postId1 = createTestPost("host1");
        String postId2 = createTestPost("host2");
        
        // Create two RSVPs for the same user
        CreateRSVPRequest request1 = new CreateRSVPRequest("guest_user", postId1);
        CreateRSVPRequest request2 = new CreateRSVPRequest("guest_user", postId2);
        
        target("api/RSVPs:create").request(MediaType.APPLICATION_JSON).post(Entity.json(request1));
        target("api/RSVPs:create").request(MediaType.APPLICATION_JSON).post(Entity.json(request2));

        Response response = target("api/RSVPs:getByUser/guest_user")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertResponseStatus(response, Status.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());

        String responseBody = response.readEntity(String.class);
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("guest_user"));
        assertTrue(responseBody.contains("rsvps"));
    }

    @Test
    void testUpdateRSVPStatusEndpoint() throws Exception {
        String postId = createTestPost("host_user");
        
        CreateRSVPRequest createRequest = new CreateRSVPRequest("guest_user", postId);
        Response createResponse = target("api/RSVPs:create")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(createRequest));
        
        assertResponseStatus(createResponse, Status.OK);
        String createResponseBody = createResponse.readEntity(String.class);
        
        // Extract RSVP ID from response (simplified - in real test might use JSON parsing)
        String rsvpId = extractRSVPId(createResponseBody);
        
        UpdateRSVPStatusRequest updateRequest = new UpdateRSVPStatusRequest(rsvpId, "ACCEPTED");

        Response response = target("api/RSVPs:updateStatus")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(updateRequest));

        assertResponseStatus(response, Status.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());

        String responseBody = response.readEntity(String.class);
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("ACCEPTED"));
    }

    @Test
    void testUpdateRSVPStatus_InvalidStatus() throws Exception {
        String postId = createTestPost("host_user");
        
        CreateRSVPRequest createRequest = new CreateRSVPRequest("guest_user", postId);
        Response createResponse = target("api/RSVPs:create")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(createRequest));
        
        String createResponseBody = createResponse.readEntity(String.class);
        String rsvpId = extractRSVPId(createResponseBody);
        
        UpdateRSVPStatusRequest updateRequest = new UpdateRSVPStatusRequest(rsvpId, "INVALID");

        Response response = target("api/RSVPs:updateStatus")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(updateRequest));

        assertResponseStatus(response, Status.BAD_REQUEST);
        
        String responseBody = response.readEntity(String.class);
        assertTrue(responseBody.contains("Invalid status"));
    }

    @Test
    void testDeleteRSVPEndpoint() throws Exception {
        String postId = createTestPost("host_user");
        
        CreateRSVPRequest createRequest = new CreateRSVPRequest("guest_user", postId);
        Response createResponse = target("api/RSVPs:create")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(createRequest));
        
        String createResponseBody = createResponse.readEntity(String.class);
        String rsvpId = extractRSVPId(createResponseBody);

        Response response = target("api/RSVPs:delete/" + rsvpId)
                .request(MediaType.APPLICATION_JSON)
                .delete();

        assertResponseStatus(response, Status.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());

        String responseBody = response.readEntity(String.class);
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("success"));
        assertTrue(responseBody.contains("true"));
    }

    @Test
    void testGetRSVPsByPost_EmptyResponse() throws Exception {
        String postId = createTestPost("host_user");

        Response response = target("api/RSVPs:getByPost/" + postId)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertResponseStatus(response, Status.OK);

        String responseBody = response.readEntity(String.class);
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("rsvps"));
        assertTrue(responseBody.contains("[]"));
    }

    @Test
    void testGetRSVPsByUser_EmptyResponse() throws Exception {
        Response response = target("api/RSVPs:getByUser/nonexistent_user")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertResponseStatus(response, Status.OK);

        String responseBody = response.readEntity(String.class);
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("rsvps"));
        assertTrue(responseBody.contains("[]"));
    }

    private String extractRSVPId(String responseBody) {
        // Simple extraction - in production would use proper JSON parsing
        int idStart = responseBody.indexOf("\"id\":\"") + 6;
        int idEnd = responseBody.indexOf("\"", idStart);
        return responseBody.substring(idStart, idEnd);
    }
}