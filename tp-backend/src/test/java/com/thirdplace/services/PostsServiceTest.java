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

class PostsServiceTest {

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

    @Test
    void testCreatePost() throws SQLException {
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

        CreatePostResponse response = PostsService.createPost(request);

        assertNotNull(response);
        assertNotNull(response.createdPost().id());
        assertEquals("Test Title", response.createdPost().title());
        assertEquals("Test Author", response.createdPost().author());
        assertEquals("active", response.createdPost().status());

    }

    @Test
    void testGetAllPosts() throws SQLException {
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

        GetAllPostsResponse response = PostsService.getAllPosts();

        assertNotNull(response);
        assertEquals(1, response.posts().size());
        assertEquals("Test Title", response.posts().get(0).title());

    }
}