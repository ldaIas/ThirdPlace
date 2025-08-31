package com.thirdplace.db;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.thirdplace.db.DatabaseConfig.DataSourceCacheKey;
import com.thirdplace.db.schemas.RSVP;

class RSVPsTableManagerTest {

    private static final DataSourceCacheKey TEST_DATASOURCE_KEY = new DataSourceCacheKey("test_rsvps_schema");
    private RSVPsTableManager rsvpsTableManager;

    @BeforeAll
    static void setUpClass() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(TEST_DATASOURCE_KEY)) {
            conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS test_rsvps_schema");
        }
    }
    
    @AfterAll
    static void tearDownClass() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(TEST_DATASOURCE_KEY)) {
            conn.createStatement().execute("DROP SCHEMA IF EXISTS test_rsvps_schema CASCADE");
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        rsvpsTableManager = new RSVPsTableManager(TEST_DATASOURCE_KEY);
        rsvpsTableManager.createTable();
        
        // Clean up any existing test data
        try (Connection conn = DatabaseManager.getConnection(TEST_DATASOURCE_KEY)) {
            conn.createStatement().execute("DELETE FROM rsvps");
        }
    }

    @Test
    void testCreateTable() throws SQLException {
        // Table creation is already tested in setUp, just verify it exists
        try (Connection conn = DatabaseManager.getConnection(TEST_DATASOURCE_KEY)) {
            var rs = conn.getMetaData().getTables(null, "test_rsvps_schema", "rsvps", null);
            assertTrue(rs.next());
        }
    }

    @Test
    void testInsert() throws SQLException {
        RSVP testRSVP = createTestRSVP();
        
        String result = rsvpsTableManager.insert(testRSVP);
        
        assertEquals(testRSVP.id(), result);
        
        // Verify the RSVP was actually inserted
        Optional<RSVP> retrieved = rsvpsTableManager.fetchById(testRSVP.id());
        assertTrue(retrieved.isPresent());
        assertEquals(testRSVP.status(), retrieved.get().status());
    }

    @Test
    void testFetchById() throws SQLException {
        RSVP testRSVP = createTestRSVP();
        rsvpsTableManager.insert(testRSVP);
        
        Optional<RSVP> result = rsvpsTableManager.fetchById(testRSVP.id());
        
        assertTrue(result.isPresent());
        assertEquals(testRSVP.id(), result.get().id());
        assertEquals(testRSVP.status(), result.get().status());
    }

    @Test
    void testFetchByIdNotFound() throws SQLException {
        Optional<RSVP> result = rsvpsTableManager.fetchById("nonexistent-id");
        
        assertFalse(result.isPresent());
    }

    @Test
    void testFetchAll() throws SQLException {
        RSVP testRSVP = createTestRSVP();
        rsvpsTableManager.insert(testRSVP);
        
        List<RSVP> result = rsvpsTableManager.fetchAll();
        
        assertEquals(1, result.size());
        assertEquals(testRSVP.id(), result.get(0).id());
    }

    @Test
    void testUpdate() throws SQLException {
        RSVP testRSVP = createTestRSVP();
        rsvpsTableManager.insert(testRSVP);
        
        RSVP updatedRSVP = new RSVP(
            testRSVP.id(),
            testRSVP.userId(),
            testRSVP.postId(),
            "declined",
            testRSVP.createdAt()
        );
        
        boolean result = rsvpsTableManager.update(updatedRSVP);
        
        assertTrue(result);
        
        // Verify the update
        Optional<RSVP> retrieved = rsvpsTableManager.fetchById(testRSVP.id());
        assertTrue(retrieved.isPresent());
        assertEquals("declined", retrieved.get().status());
    }

    @Test
    void testDelete() throws SQLException {
        RSVP testRSVP = createTestRSVP();
        rsvpsTableManager.insert(testRSVP);
        
        boolean result = rsvpsTableManager.delete(testRSVP.id());
        
        assertTrue(result);
        
        // Verify the deletion
        Optional<RSVP> retrieved = rsvpsTableManager.fetchById(testRSVP.id());
        assertFalse(retrieved.isPresent());
    }

    @Test
    void testFetchByFilter() throws SQLException {
        RSVP testRSVP = createTestRSVP();
        rsvpsTableManager.insert(testRSVP);
        
        List<WhereFilter> filters = List.of(
            new WhereFilter(RSVP.RSVPFieldReference.POST_ID, WhereFilter.FilterOperator.EQUALS, "post-123")
        );
        
        List<RSVP> result = rsvpsTableManager.fetchByFilter(filters);
        
        assertEquals(1, result.size());
        assertEquals(testRSVP.id(), result.get(0).id());
    }

    private RSVP createTestRSVP() {
        return new RSVP(
            "rsvp-test-id-" + System.currentTimeMillis(),
            "user-123",
            "post-123",
            "confirmed",
            Instant.now()
        );
    }
}