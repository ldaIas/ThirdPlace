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
import com.thirdplace.services.PostsService.CreatePostRequest;
import com.thirdplace.services.PostsService.CreatePostResponse;
import com.thirdplace.services.PostsService.GetAllPostsResponse;
import com.thirdplace.testutils.AppTestUtils;

class PostsServiceTests {

    private static final DataSourceCacheKey TEST_DATASOURCE_KEY = new DataSourceCacheKey("test_service_schema");

    @BeforeAll
    static void setUpClass() throws SQLException {
        AppDataSource.setAppDatasource(TEST_DATASOURCE_KEY);
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS test_service_schema");
        }
        PostsTableManager.getInstance().createTable();
    }

    @AfterAll
    static void tearDownClass() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("DROP SCHEMA IF EXISTS test_service_schema CASCADE");
        }
    }

    @BeforeEach
    void setUp() throws SQLException {

        // Clean up any existing test data
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("DELETE FROM posts");
        }
    }

    /**
     * Test to ensure that we can create a post, and it is returned correctly.
     *
     */
    @Test
    void testCreatePost() {
        final CreatePostRequest request = new CreatePostRequest(
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

        final CreatePostResponse response = AppTestUtils.assertOkResult(PostsService.createPost(request));

        assertNotNull(response, "Expected non null response");
        assertNotNull(response.createdPost().id(), "Expected created post id to equal response id");
        assertEquals("Test Title", response.createdPost().title(), "Expected title to match");
        assertEquals("Test Author", response.createdPost().author(), "Expected author to match");
        assertEquals("active", response.createdPost().status(), "Expected status to match");

    }

    /**
     * Test to ensure that we can get all posts.
     */
    @Test
    void testGetAllPosts() {
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

        PostsService.createPost(request);

        final GetAllPostsResponse response = AppTestUtils.assertOkResult(PostsService.getAllPosts());

        assertNotNull(response, "Expected to get a response");
        assertEquals(1, response.posts().size(), "Expected to have a post");
        assertEquals("Test Title", response.posts().getFirst().title(), "Expected matching title");

    }
}