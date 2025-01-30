package com.thirdplace.ThirdPlaceDatabaseService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.thirdplace.ThirdPlaceDatabaseService.TableColumnType;
import com.thirdplace.ThirdPlaceDatabaseService.ThirdPlaceDatabaseService;
import com.thirdplace.ThirdPlaceDatabaseService.ThirdPlaceDatabaseServiceRuntimeError;

@Timeout(value = 10, unit = TimeUnit.SECONDS)
public class ThirdPlaceDatabaseServiceTests {

    private static final List<TableColumnType> TEST_COLUMNS = List.of(new TableColumnType("id", "INT"),
            new TableColumnType("name", "VARCHAR(100)"));

    /**
     * Test that verifies that we can start and close out the database service
     */
    @Test
    void testInitializeAndCloseService() {
        try (final ThirdPlaceDatabaseService dbService = new ThirdPlaceDatabaseService()) {
            // If we make it here without an exception, the service was started successfully
        } catch (Exception e) {
            // If an exception is thrown, fail the test
            Assertions.fail("Database service initialization or closure failed", e);
        }
    }

    /**
     * Test that verifies that when the pg_ctl server is not running we can correctly start and stop the database service
     */
    @Test
    void testStopStartDatabaseServer() {

        // Runs the pg_ctl stop command
        ThirdPlaceDatabaseService.stopPostgresServer();

        try (final ThirdPlaceDatabaseService dbService = new ThirdPlaceDatabaseService()) {
            // If we make it here without an exception, the service was started successfully
        } catch (Exception e) {
            // If an exception is thrown, fail the test
            Assertions.fail("Database service initialization or closure failed", e);
        }

    }

    /**
     * Test that verifies that when the pg_ctl server is already running we can correctly start and stop the database service
     * This test is repeated as there was a bug in the DBService code with tests being ran one after another. That has been fixed but want to make sure 
     * we don't regress
     */
    @RepeatedTest(value = 10)
    void testStopStartDatabaseServer_ServerAlreadyRunning() {

        ThirdPlaceDatabaseService.startPostgresServer();

        try (final ThirdPlaceDatabaseService dbService = new ThirdPlaceDatabaseService()) {
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
        try (final ThirdPlaceDatabaseService dbService = new ThirdPlaceDatabaseService()) {

            final String tableName = getTableName();

            assertDoesNotThrow(() -> {
                dbService.createTable(tableName, TEST_COLUMNS);
            }, "Expected create table to succeed");

            Assertions.assertTrue(dbService.tableExists(tableName), "Expected create table to exist");

        } catch (Exception e) {

            Assertions.fail("Database service initialization or closure failed", e);
        }
    }

    /**
     * Test that verifies that if we create a table that already exists, an
     * exception is thrown
     */
    @Test
    void testCreateTable_TableAlreadyExists() {
        final String tableName = getTableName();

        try (final ThirdPlaceDatabaseService dbService = new ThirdPlaceDatabaseService()) {
            // Create the table first
            dbService.createTable(tableName, TEST_COLUMNS);

            // Now try to create it again and expect an exception
            assertThrows(ThirdPlaceDatabaseServiceRuntimeError.class, () -> {
                dbService.createTable(tableName, TEST_COLUMNS);
            });
        } catch (Exception e) {
            Assertions.fail("Database service initialization or closure failed", e);
        }
    }

    /**
     * Test that verifies that if we create a table with an invalid schema, an
     * exception is thrown
     */
    @Test
    void testCreateTable_InvalidSchemaDefinition() {
        final String tableName = getTableName();

        final List<TableColumnType> invalidColumns = List.of(new TableColumnType("id", ""),
                new TableColumnType("name", ""));

        try (final ThirdPlaceDatabaseService dbService = new ThirdPlaceDatabaseService()) {
            assertThrows(ThirdPlaceDatabaseServiceRuntimeError.class, () -> {
                dbService.createTable(tableName, invalidColumns);
            });
        } catch (Exception e) {
            Assertions.fail("Database service initialization or closure failed", e);
        }
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
