package com.thirdplace.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.thirdplace.AppDataSource;
import com.thirdplace.db.DatabaseConfig.DataSourceCacheKey;
import com.thirdplace.db.schemas.Post;

class PostsTableManagerTest {

    private static final DataSourceCacheKey TEST_DATASOURCE_KEY = new DataSourceCacheKey("test_posts_schema");
    private static final PostsTableManager postsTableManager = PostsTableManager.getInstance();

    @BeforeAll
    static void setUpClass() throws SQLException {
        AppDataSource.setAppDatasource(TEST_DATASOURCE_KEY);
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS test_posts_schema");
            postsTableManager.createTable();
        }
    }

    @AfterAll
    static void tearDownClass() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.createStatement().execute("DROP SCHEMA IF EXISTS test_posts_schema CASCADE");
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
    void testCreateTable() throws SQLException {
        // Table creation is already tested in setUp, just verify it exists
        try (Connection conn = DatabaseManager.getConnection()) {
            var rs = conn.getMetaData().getTables(null, "test_posts_schema", "posts", null);
            assertTrue(rs.next());
        }
    }

    @Test
    void testInsert() throws SQLException {
        Post testPost = createTestPost();

        String result = postsTableManager.insert(testPost);

        assertEquals(testPost.id(), result);

        // Verify the post was actually inserted
        Optional<Post> retrieved = postsTableManager.fetchById(testPost.id());
        assertTrue(retrieved.isPresent());
        assertEquals(testPost.title(), retrieved.get().title());
    }

    @Test
    void testFetchById() throws SQLException {
        Post testPost = createTestPost();
        postsTableManager.insert(testPost);

        Optional<Post> result = postsTableManager.fetchById(testPost.id());

        assertTrue(result.isPresent());
        assertEquals(testPost.id(), result.get().id());
        assertEquals(testPost.title(), result.get().title());
    }

    @Test
    void testFetchByIdNotFound() throws SQLException {
        Optional<Post> result = postsTableManager.fetchById("nonexistent-id");

        assertFalse(result.isPresent());
    }

    @Test
    void testFetchAll() throws SQLException {
        Post testPost = createTestPost();
        postsTableManager.insert(testPost);

        List<Post> result = postsTableManager.fetchAll();

        assertEquals(1, result.size());
        assertEquals(testPost.id(), result.get(0).id());
    }

    @Test
    void testUpdate() throws SQLException {
        Post testPost = createTestPost();
        postsTableManager.insert(testPost);

        Post updatedPost = new Post(
                testPost.id(),
                "Updated Title",
                testPost.author(),
                testPost.description(),
                testPost.createdAt(),
                testPost.endDate(),
                testPost.groupSize(),
                testPost.tags(),
                testPost.location(),
                testPost.latitude(),
                testPost.longitude(),
                testPost.proposedTime(),
                testPost.isDateActivity(),
                testPost.status(),
                testPost.genderBalance(),
                testPost.category());

        boolean result = postsTableManager.update(updatedPost);

        assertTrue(result);

        // Verify the update
        Optional<Post> retrieved = postsTableManager.fetchById(testPost.id());
        assertTrue(retrieved.isPresent());
        assertEquals("Updated Title", retrieved.get().title());
    }

    @Test
    void testDelete() throws SQLException {
        Post testPost = createTestPost();
        postsTableManager.insert(testPost);

        boolean result = postsTableManager.delete(testPost.id());

        assertTrue(result);

        // Verify the deletion
        Optional<Post> retrieved = postsTableManager.fetchById(testPost.id());
        assertFalse(retrieved.isPresent());
    }

    @Test
    void testFetchByFilter() throws SQLException {
        Post testPost = createTestPost();
        postsTableManager.insert(testPost);

        List<WhereFilter> filters = List.of(
                new WhereFilter(Post.PostFieldReference.STATUS, WhereFilter.FilterOperator.EQUALS, "active"));

        List<Post> result = postsTableManager.fetchByFilter(filters);

        assertEquals(1, result.size());
        assertEquals(testPost.id(), result.get(0).id());
    }

    private Post createTestPost() {
        return new Post(
                "test-id-" + System.currentTimeMillis(),
                "Test Title",
                "Test Author",
                "Test Description",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                5,
                new String[] { "tag1", "tag2" },
                "Test Location",
                40.7128,
                -74.0060,
                Instant.now().plusSeconds(1800),
                false,
                "active",
                "mixed",
                "social");
    }
}