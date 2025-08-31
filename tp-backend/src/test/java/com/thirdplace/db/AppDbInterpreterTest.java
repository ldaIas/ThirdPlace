package com.thirdplace.db;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.thirdplace.db.DatabaseConfig.DataSourceCacheKey;
import com.thirdplace.db.schemas.RSVP;

class AppDbInterpreterTest {

    private static final DataSourceCacheKey TEST_DATASOURCE_KEY = new DataSourceCacheKey("test_schema");
    
    @BeforeAll
    static void setUp() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(TEST_DATASOURCE_KEY)) {
            conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS test_schema");
        }
    }
    
    @AfterAll
    static void tearDown() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(TEST_DATASOURCE_KEY)) {
            conn.createStatement().execute("DROP SCHEMA IF EXISTS test_schema CASCADE");
        }
    }

    @Test
    void testGenerateTableDdl() {
        String ddl = AppDbInterpreter.generateTableDdl(TestEntity.TABLE_NAME, List.of(TestEntity.TestEntityFieldReference.values()));
        
        assertNotNull(ddl, "Generated DDL should not be null");
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS test_entities"), "DDL should contain CREATE TABLE statement for test_entities");
        assertTrue(ddl.contains("id VARCHAR(255) PRIMARY KEY"), "DDL should contain primary key definition for id field");
        assertTrue(ddl.contains("name VARCHAR(255) NOT NULL"), "DDL should contain NOT NULL constraint for name field");
    }

    @Test
    void testGenerateInsertSql() {
        TestEntity testEntity = createTestEntity();
        String sql = AppDbInterpreter.generateInsertSql(testEntity);
        
        assertNotNull(sql, "Generated INSERT SQL should not be null");
        assertTrue(sql.startsWith("INSERT INTO test_entities"), "SQL should start with INSERT INTO test_entities");
        assertTrue(sql.contains("VALUES"), "SQL should contain VALUES clause");
        assertEquals(4, sql.split("\\?").length - 1, "SQL should have 4 parameter placeholders for TestEntity fields"); // Should have 4 placeholders for TestEntity fields
    }

    @Test
    void testGenerateUpdateSql() {
        TestEntity testEntity = createTestEntity();
        List<WhereFilter> whereClause = List.of(
            new WhereFilter(TestEntity.TestEntityFieldReference.ID, WhereFilter.FilterOperator.EQUALS, "test-id")
        );
        
        String sql = AppDbInterpreter.generateUpdateSql(testEntity, whereClause);
        
        assertNotNull(sql, "Generated UPDATE SQL should not be null");
        assertTrue(sql.startsWith("UPDATE test_entities SET"), "SQL should start with UPDATE test_entities SET");
        assertTrue(sql.contains("WHERE"), "SQL should contain WHERE clause");
        assertTrue(sql.contains("id = ?"), "SQL should contain parameterized WHERE condition for id field");
    }

    @Test
    void testGenerateSelectSql() {
        List<WhereFilter> whereClause = List.of(
            new WhereFilter(TestEntity.TestEntityFieldReference.ID, WhereFilter.FilterOperator.EQUALS, "test-id")
        );
        
        String sql = AppDbInterpreter.generateSelectSql(TestEntity.TABLE_NAME, whereClause);
        
        assertNotNull(sql, "Generated SELECT SQL should not be null");
        assertTrue(sql.startsWith("SELECT * FROM test_entities WHERE"), "SQL should start with SELECT * FROM test_entities WHERE");
        assertTrue(sql.contains("id = ?"), "SQL should contain parameterized WHERE condition for id field");
    }

    @Test
    void testGenerateDeleteSql() {
        List<WhereFilter> whereClause = List.of(
            new WhereFilter(TestEntity.TestEntityFieldReference.ID, WhereFilter.FilterOperator.EQUALS, "test-id")
        );
        
        String sql = AppDbInterpreter.generateDeleteSql(TestEntity.TABLE_NAME, whereClause);
        
        assertNotNull(sql, "Generated DELETE SQL should not be null");
        assertTrue(sql.startsWith("DELETE FROM test_entities WHERE"), "SQL should start with DELETE FROM test_entities WHERE");
        assertTrue(sql.contains("id = ?"), "SQL should contain parameterized WHERE condition for id field");
    }

    @Test
    void testPrepareInsertStatement() throws SQLException {
        final TestEntity testEntity = createTestEntity();
        
        try (final Connection conn = DatabaseManager.getConnection(TEST_DATASOURCE_KEY)) {
            conn.createStatement().execute(AppDbInterpreter.generateTableDdl(TestEntity.TABLE_NAME, List.of(TestEntity.TestEntityFieldReference.values())));
            
            final PreparedStatement result = AppDbInterpreter.prepareInsertStatement(testEntity, conn);
            
            assertNotNull(result, "Prepared INSERT statement should not be null");
            // Verify the statement can be executed (will fail if SQL is malformed)
            result.executeUpdate();
        }
    }

    @Test
    void testMapResultSetToSchema() throws SQLException {
        final TestEntity testEntity = createTestEntity();
        
        try (Connection conn = DatabaseManager.getConnection(TEST_DATASOURCE_KEY)) {
            // Create table and insert test data
            conn.createStatement().execute(AppDbInterpreter.generateTableDdl(TestEntity.TABLE_NAME, List.of(RSVP.RSVPFieldReference.values())));
            
            final PreparedStatement insertStmt = AppDbInterpreter.prepareInsertStatement(testEntity, conn);
            insertStmt.executeUpdate();
            
            // Query and map result
            try (final PreparedStatement selectStmt = conn.prepareStatement("SELECT * FROM rsvps WHERE id = ?")) {
                selectStmt.setString(1, testEntity.id());
                try (final ResultSet rs = selectStmt.executeQuery()) {
                    assertTrue(rs.next(), "ResultSet should contain at least one row after insert");
                    
                    TestEntity result = AppDbInterpreter.mapResultSetToSchema(TestEntity.class, rs);
                    
                    assertNotNull(result, "Mapped TestEntity should not be null");
                    assertEquals(testEntity.id(), result.id(), "Expected to get the same id back");
                    assertEquals(testEntity.description(), result.description(), "Expected to get the same description back");
                    assertEquals(testEntity.name(), result.name(), "Expected to get the same name back");
                    assertEquals(testEntity.createdAt(), result.createdAt(), "Expected to get the same created");
                }
            }
        }
    }

    private TestEntity createTestEntity() {
        return new TestEntity(
            "test-id",
            "Test Name",
            "Test Description",
            Instant.now()
        );
    }
}