package com.thirdplace.ThirdPlaceDatabaseService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 10, unit = TimeUnit.SECONDS)
public class ThirdPlaceDatabaseServiceTests {

    private static final List<TableColumnType> TEST_COLUMNS = List.of(new TableColumnType("id", "INT"),
            new TableColumnType("name", "VARCHAR(100)"));

    private static ThirdPlaceDatabaseServiceTestExt staticDbService;

    @BeforeAll
    static void setup() {
        staticDbService = new ThirdPlaceDatabaseServiceTestExt();
    }

    @AfterAll
    static void tearDown() throws Exception {
        staticDbService.close();
    }

    /**
     * Test that verifies that we can start and close out the database service.
     * This test also verifies that getting an instance of the database service
     * properly ref counts, as, if it didn't, we would get an exception when trying
     * to close the service
     */
    @Test
    void testInitializeAndCloseService() {
        try (final ThirdPlaceDatabaseService dbService = ThirdPlaceDatabaseService.getInstance()) {
            // If we make it here without an exception, the service was started successfully
        } catch (Exception e) {
            // If an exception is thrown, fail the test
            Assertions.fail("Database service initialization or closure failed", e);
        }
    }

    /**
     * Test that verifies that when the pg_ctl server is already running we can
     * correctly start and stop the database service This test is repeated as there
     * was a bug in the DBService code with tests being ran one after another. That
     * has been fixed but want to make sure we don't regress
     */
    @RepeatedTest(value = 10)
    void testStopStartDatabaseServer_ServerAlreadyRunning() {

        ThirdPlaceDatabaseService.startPostgresServer();

        try (final ThirdPlaceDatabaseService dbService = new ThirdPlaceDatabaseServiceTestExt()) {
            // If we make it here without an exception, the service was started successfully

        } catch (Exception e) {
            // If an exception is thrown, fail the test
            Assertions.fail("Database service initialization or closure failed", e);
        }

    }

    /**
     * Test that verifies that we can create a table in the database
     */
    @Test
    void testCreateTable_Success() {
        final String tableName = getTableName();

        assertDoesNotThrow(() -> {
            staticDbService.createTable(tableName, TEST_COLUMNS);
        }, "Expected create table to succeed");

        Assertions.assertTrue(staticDbService.tableExists(tableName), "Expected create table to exist");

    }

    /**
     * Test that verifies that if we create a table that already exists, nothing
     * happens
     */
    @Test
    void testCreateTable_TableAlreadyExists() {
        final String tableName = getTableName();
        // Create the table first
        staticDbService.createTable(tableName, TEST_COLUMNS);

        // Now try to create it again
        staticDbService.createTable(tableName, TEST_COLUMNS);

    }

    /**
     * Test that verifies that if we create a table with an invalid definition, an
     * exception is thrown
     */
    @Test
    void testCreateTable_InvalidSchemaDefinition() {
        final String tableName = getTableName();

        final List<TableColumnType> invalidColumns = List.of(new TableColumnType("id", ""),
                new TableColumnType("name", ""));

        assertThrows(ThirdPlaceDatabaseServiceRuntimeError.class, () -> {
            staticDbService.createTable(tableName, invalidColumns);
        });
    }

    /**
     * Helper method to get a table name for testing purposes.
     * 
     *
     * @return "testt" + Current time in milliseconds to the 6th digit
     */
    private static String getTableName() {
        return "testt" + System.currentTimeMillis();
    }
}
