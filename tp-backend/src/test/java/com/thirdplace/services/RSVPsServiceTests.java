package com.thirdplace.services;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.thirdplace.AppDataSource;
import com.thirdplace.db.DatabaseConfig.DataSourceCacheKey;
import com.thirdplace.db.DatabaseManager;
import com.thirdplace.db.PostsTableManager;
import com.thirdplace.db.RSVPsTableManager;
import com.thirdplace.services.PostsService.CreatePostRequest;
import com.thirdplace.services.RSVPsService.CreateRSVPRequest;
import com.thirdplace.services.RSVPsService.CreateRSVPResponse;
import com.thirdplace.services.RSVPsService.GetRSVPsByPostResponse;
import com.thirdplace.services.RSVPsService.GetRSVPsByUserResponse;
import com.thirdplace.services.RSVPsService.UpdateRSVPStatusRequest;
import com.thirdplace.services.RSVPsService.UpdateRSVPStatusResponse;
import com.thirdplace.services.RSVPsService.DeleteRSVPResponse;
import com.thirdplace.testutils.AppTestUtils;
import com.thirdplace.utils.result.Result;
import com.thirdplace.services.RSVPsService.RSVPsErrorCode;

class RSVPsServiceTests {

    private static final DataSourceCacheKey TEST_DATASOURCE_KEY = new DataSourceCacheKey("test_rsvp_service_schema");

    @BeforeAll
    static void setUpClass() throws SQLException {
        AppDataSource.setAppDatasource(TEST_DATASOURCE_KEY);
        try (final Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS test_rsvp_service_schema");
        }
        PostsTableManager.getInstance().createTable();
        RSVPsTableManager.getInstance().createTable();
    }

    @AfterAll
    static void tearDownClass() throws SQLException {
        try (final Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("DROP SCHEMA IF EXISTS test_rsvp_service_schema CASCADE");
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (final Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("DELETE FROM rsvps");
            conn.createStatement().execute("DELETE FROM posts");
        }
    }

    private String createTestPost(final String author) {
        final CreatePostRequest postRequest = new CreatePostRequest(
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
        
        return AppTestUtils.assertOkResult(PostsService.createPost(postRequest)).createdPost().id();
    }

    @Test
    void testCreateRSVP() {
        final String postId = createTestPost("host_user");
        
        final CreateRSVPRequest request = new CreateRSVPRequest("guest_user", postId);
        final CreateRSVPResponse response = AppTestUtils.assertOkResult(RSVPsService.createRSVP(request));

        assertNotNull(response, "Expected non null response");
        assertNotNull(response.createdRSVP().id(), "Expected created RSVP id to be non null");
        assertEquals("guest_user", response.createdRSVP().userId(), "Expected user id to match");
        assertEquals(postId, response.createdRSVP().postId(), "Expected post id to match");
        assertEquals("PENDING", response.createdRSVP().status(), "Expected status to be PENDING");
    }

    @Test
    void testCreateRSVP_CannotRSVPToOwnPost() {
        final String postId = createTestPost("host_user");
        
        final CreateRSVPRequest request = new CreateRSVPRequest("host_user", postId);
        final Result<?> result = RSVPsService.createRSVP(request);

        AppTestUtils.assertErrorResult(result, RSVPsErrorCode.CANNOT_RSVP_OWN_POST);
    }

    @Test
    void testCreateRSVP_CannotRSVPTwice() {
        final String postId = createTestPost("host_user");
        
        final CreateRSVPRequest request = new CreateRSVPRequest("guest_user", postId);
        AppTestUtils.assertOkResult(RSVPsService.createRSVP(request));
        
        final Result<?> result = RSVPsService.createRSVP(request);
        AppTestUtils.assertErrorResult(result, RSVPsErrorCode.ALREADY_RSVPED);
    }

    @Test
    void testGetRSVPsByPost() {
        final String postId = createTestPost("host_user");
        
        final CreateRSVPRequest request1 = new CreateRSVPRequest("guest1", postId);
        final CreateRSVPRequest request2 = new CreateRSVPRequest("guest2", postId);
        
        AppTestUtils.assertOkResult(RSVPsService.createRSVP(request1));
        AppTestUtils.assertOkResult(RSVPsService.createRSVP(request2));

        final GetRSVPsByPostResponse response = AppTestUtils.assertOkResult(RSVPsService.getRSVPsByPost(postId));

        assertNotNull(response, "Expected non null response");
        assertEquals(2, response.rsvps().size(), "Expected 2 RSVPs");
    }

    @Test
    void testGetRSVPsByUser() {
        final String postId1 = createTestPost("host1");
        final String postId2 = createTestPost("host2");
        
        final CreateRSVPRequest request1 = new CreateRSVPRequest("guest_user", postId1);
        final CreateRSVPRequest request2 = new CreateRSVPRequest("guest_user", postId2);
        
        AppTestUtils.assertOkResult(RSVPsService.createRSVP(request1));
        AppTestUtils.assertOkResult(RSVPsService.createRSVP(request2));

        final GetRSVPsByUserResponse response = AppTestUtils.assertOkResult(RSVPsService.getRSVPsByUser("guest_user"));

        assertNotNull(response, "Expected non null response");
        assertEquals(2, response.rsvps().size(), "Expected 2 RSVPs");
    }

    @Test
    void testUpdateRSVPStatus() {
        final String postId = createTestPost("host_user");
        
        final CreateRSVPRequest createRequest = new CreateRSVPRequest("guest_user", postId);
        final CreateRSVPResponse createResponse = AppTestUtils.assertOkResult(RSVPsService.createRSVP(createRequest));
        
        final UpdateRSVPStatusRequest updateRequest = new UpdateRSVPStatusRequest(
                createResponse.createdRSVP().id(), "ACCEPTED");
        final UpdateRSVPStatusResponse updateResponse = AppTestUtils.assertOkResult(RSVPsService.updateRSVPStatus(updateRequest));

        assertNotNull(updateResponse, "Expected non null response");
        assertEquals("ACCEPTED", updateResponse.updatedRSVP().status(), "Expected status to be ACCEPTED");
    }

    @Test
    void testUpdateRSVPStatus_InvalidStatus() {
        final String postId = createTestPost("host_user");
        
        final CreateRSVPRequest createRequest = new CreateRSVPRequest("guest_user", postId);
        final CreateRSVPResponse createResponse = AppTestUtils.assertOkResult(RSVPsService.createRSVP(createRequest));
        
        final UpdateRSVPStatusRequest updateRequest = new UpdateRSVPStatusRequest(
                createResponse.createdRSVP().id(), "INVALID");
        
        final Result<?> result = RSVPsService.updateRSVPStatus(updateRequest);
        AppTestUtils.assertErrorResult(result, RSVPsErrorCode.INVALID_STATUS);
    }

    @Test
    void testUpdateRSVPStatus_GroupSizeLimit() {
        final String postId = createTestPost("host_user"); // Group size is 3
        
        // Create and accept 3 RSVPs
        for (int i = 1; i <= 3; i++) {
            final CreateRSVPRequest createRequest = new CreateRSVPRequest("guest" + i, postId);
            final CreateRSVPResponse createResponse = AppTestUtils.assertOkResult(RSVPsService.createRSVP(createRequest));
            
            final UpdateRSVPStatusRequest updateRequest = new UpdateRSVPStatusRequest(
                    createResponse.createdRSVP().id(), "ACCEPTED");
            AppTestUtils.assertOkResult(RSVPsService.updateRSVPStatus(updateRequest));
        }
        
        // Try to accept a 4th RSVP
        final CreateRSVPRequest createRequest = new CreateRSVPRequest("guest4", postId);
        final CreateRSVPResponse createResponse = AppTestUtils.assertOkResult(RSVPsService.createRSVP(createRequest));
        
        final UpdateRSVPStatusRequest updateRequest = new UpdateRSVPStatusRequest(
                createResponse.createdRSVP().id(), "ACCEPTED");
        
        final Result<?> result = RSVPsService.updateRSVPStatus(updateRequest);
        AppTestUtils.assertErrorResult(result, RSVPsErrorCode.GROUP_SIZE_LIMIT_REACHED);
    }

    @Test
    void testDeleteRSVP() {
        final String postId = createTestPost("host_user");
        
        final CreateRSVPRequest createRequest = new CreateRSVPRequest("guest_user", postId);
        final CreateRSVPResponse createResponse = AppTestUtils.assertOkResult(RSVPsService.createRSVP(createRequest));
        
        final DeleteRSVPResponse deleteResponse = AppTestUtils.assertOkResult(RSVPsService.deleteRSVP(createResponse.createdRSVP().id()));

        assertNotNull(deleteResponse, "Expected non null response");
        assertTrue(deleteResponse.success(), "Expected delete to be successful");
    }
}