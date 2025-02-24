package com.thirdplace.thirdplacedatabaseservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.thirdplace.testutils.ThirdPlaceDatabaseServiceTestExt;
import com.thirdplace.thirdplacedatabaseservice.DatabaseServiceResults.DeleteResult;
import com.thirdplace.thirdplacedatabaseservice.DatabaseServiceResults.InsertResult;
import com.thirdplace.thirdplacedatabaseservice.DatabaseServiceResults.QueryResult;
import com.thirdplace.thirdplacedatabaseservice.DatabaseServiceResults.UpdateResult;
import com.thirdplace.thirdplacedatabaseservice.WhereFilter.Operator;

public class ThirdPlaceDatabaseServiceTests {

    private static final String TEST_TABLE_NAME = "testt";

    private static final String TESTC_ID = "id";
    private static final String TESTC_NAME = "name";
    private static final List<TableColumnType> TEST_COLUMNS = List.of(
            new TableColumnType(TESTC_ID, "INTEGER PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY UNIQUE"),
            new TableColumnType(TESTC_NAME, "VARCHAR(100)"));

    private static final String TEST_NAME = "joe test";

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
     * Test that verifies that we can start and close out the database service. This
     * test also verifies that getting an instance of the database service properly
     * ref counts, as, if it didn't, we would get an exception when trying to close
     * the service
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
     * Test that verifies that we can insert data into a table
     */
    @Test
    void testInsertData_Success() {
        final String tableName = getTableName();

        staticDbService.createTable(tableName, TEST_COLUMNS);

        final DatabaseServiceResults<InsertResult> insertRes = staticDbService.insertRecord(tableName,
                List.of(TESTC_NAME), List.of(TEST_NAME));

        Assertions.assertEquals(1, insertRes.result().rowsInserted(), "Expected insert result to have 1 result");
        Assertions.assertEquals(QueryOperation.INSERT, insertRes.operation(),
                "Expected operation to be an insert operation");
        Assertions.assertTrue(insertRes.successful(), "Expected insert operation to be successful");

        final WhereFilter filter = new WhereFilter(TESTC_NAME, Operator.EQUAL, TEST_NAME);
        final DatabaseServiceResults<QueryResult> queryRes = staticDbService.queryRecord(tableName,
                List.of(TESTC_ID, TESTC_NAME), List.of(filter));

        // Should find a result with an id and name key
        Assertions.assertEquals(1, queryRes.result().count(), "Expected query result to have 1 result");

        final Map<String, Object> resultMap = queryRes.result().results().getFirst();
        Assertions.assertTrue(resultMap.containsKey(TESTC_ID), "Expected query result to have an id key");
        Assertions.assertEquals(insertRes.result().inserted().get("id"), resultMap.get(TESTC_ID),
                "Expected to have the same id as the insert");

        Assertions.assertTrue(resultMap.containsKey(TESTC_NAME), "Expected query result to have a name key");
        Assertions.assertEquals(TEST_NAME, resultMap.get(TESTC_NAME), "Expected to have name " + TEST_NAME);

    }

    /**
     * Test that verifies that if we insert data into a table that doesn't exist, we
     * get the correct exception in the insert result object
     */
    @Test
    void testInsertData_TableDoesNotExist() {

        final DatabaseServiceResults<InsertResult> insertRes = staticDbService.insertRecord("thistabledne",
                List.of(TESTC_NAME), List.of(TEST_NAME));

        final SQLException insertEx = insertRes.exception();
        Assertions.assertNotNull(insertEx, "Expected insert exception to be non-null");

        // The SQLState for table not found is 42P01
        Assertions.assertEquals("42P01", insertEx.getSQLState(), "Expected insert exception to have SQLState 42P01");

    }

    /**
     * Test that verifies we can delete records from a table
     */
    @Test
    void testDeleteData_Success() {
        final String tableName = getTableName();

        staticDbService.createTable(tableName, TEST_COLUMNS);

        staticDbService.insertRecord(tableName, List.of(TESTC_NAME), List.of(TEST_NAME));

        final WhereFilter filter = new WhereFilter(TESTC_NAME, Operator.EQUAL, TEST_NAME);
        final DatabaseServiceResults<QueryResult> queryRes = staticDbService.queryRecord(tableName,
                List.of(TESTC_ID, TESTC_NAME), List.of(filter));

        // Should find a result with an id and name key
        Assertions.assertEquals(1, queryRes.result().count(), "Expected query result to have 1 result");

        final DatabaseServiceResults<DeleteResult> deleteRes = staticDbService.deleteRecord(tableName, List.of(filter));

        Assertions.assertEquals(1, deleteRes.result().rowsDeleted(), "Expected delete result to have 1 result");
        Assertions.assertEquals(QueryOperation.DELETE, deleteRes.operation(),
                "Expected operation to be a delete operation");
        Assertions.assertTrue(deleteRes.successful(), "Expected delete operation to be successful");

        // Should find nothing now
        final DatabaseServiceResults<QueryResult> queryRes2 = staticDbService.queryRecord(tableName,
                List.of(TESTC_ID, TESTC_NAME), List.of(filter));
        Assertions.assertEquals(0, queryRes2.result().count(), "Expected query result to have 0 results");

    }

    /**
     * Test that verifies that if we delete data from a table that doesn't exist, we
     * get the correct exception in the delete result object
     */
    @Test
    void testDeleteData_TableDoesNotExist() {

        final DatabaseServiceResults<DeleteResult> deleteRes = staticDbService.deleteRecord("thistabledne",
                List.of(new WhereFilter("x", Operator.EQUAL, "1")));

        final SQLException deleteEx = deleteRes.exception();
        Assertions.assertNotNull(deleteEx, "Expected delete exception to be non-null");

        // The SQLState for table not found is 42P01
        Assertions.assertEquals("42P01", deleteEx.getSQLState(), "Expected delete exception to have SQLState 42P01");

    }

    /**
     * Test to ensure that if an empty filter is given to the delete method, an
     * exception is thrown
     */
    @Test
    void testDeleteData_EmptyFilter() {

        try {
            staticDbService.deleteRecord("thistabledne", List.of());
            Assertions.fail("Expected exception to be thrown");
        } catch (ThirdPlaceDatabaseServiceRuntimeError e) {
            Assertions.assertEquals(ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_EMPTY_WHERE_CLAUSES,
                    e.getErrorCode(), "Expected error code to be for empty filter");
        }

    }

    /**
     * Test to ensure that querying records works as expected: - Insert 5 records -
     * Use filters to find specific ones - Use no filters to find all
     *
     */
    @Test
    void testQueryRecords() {

        final String tableName = getTableName();

        staticDbService.createTable(tableName, TEST_COLUMNS);

        // Insert 5 records
        for (int i = 0; i < 5; i++) {
            staticDbService.insertRecord(tableName, List.of(TESTC_NAME), List.of(TEST_NAME + (i + 1)));
        }

        // Find all records
        final DatabaseServiceResults<QueryResult> queryRes = staticDbService.queryRecord(tableName,
                List.of(TESTC_ID, TESTC_NAME), List.of());

        Assertions.assertEquals(5, queryRes.result().count(), "Expected query result to have 5 results");

        // Find records with name = TEST_NAME + 3
        final WhereFilter filter = new WhereFilter(TESTC_NAME, Operator.EQUAL, TEST_NAME + "3");
        final DatabaseServiceResults<QueryResult> queryRes2 = staticDbService.queryRecord(tableName,
                List.of(TESTC_ID, TESTC_NAME), List.of(filter));

        Assertions.assertEquals(1, queryRes2.result().count(), "Expected query result to have 1 result");

        final Map<String, Object> resultMap = queryRes2.result().results().getFirst();
        Assertions.assertTrue(resultMap.containsKey(TESTC_ID), "Expected query result to have an id key");
        Assertions.assertEquals(3, resultMap.get(TESTC_ID), "Expected to have id 3");

        Assertions.assertTrue(resultMap.containsKey(TESTC_NAME), "Expected query result to have a name key");
        Assertions.assertEquals(TEST_NAME + "3", resultMap.get(TESTC_NAME), "Expected to have name " + TEST_NAME + "3");

    }

    /**
     * Test that verifies that if we query data from a table that doesn't exist, we
     * get the correct exception in the query result object
     * 
     */
    @Test
    void testQueryData_TableDoesNotExist() {

        final DatabaseServiceResults<QueryResult> queryRes = staticDbService.queryRecord("thistabledne",
                List.of(TESTC_ID, TESTC_NAME), List.of());

        final SQLException queryEx = queryRes.exception();
        Assertions.assertNotNull(queryEx, "Expected query exception to be non-null");

        // The SQLState for table not found is 42P01
        Assertions.assertEquals("42P01", queryEx.getSQLState(), "Expected query exception to have SQLState 42P01");

    }

    /**
     * Test ensuring that users cannot perform sql injection with filters
     */
    @Test
    void testQueryData_SqlInjection() {

        final String tableName = getTableName();

        staticDbService.createTable(tableName, TEST_COLUMNS);

        // Insert 5 records
        for (int i = 0; i < 5; i++) {
            staticDbService.insertRecord(tableName, List.of(TESTC_NAME), List.of(TEST_NAME + (i + 1)));
        }

        // Try to find all records via injection
        final WhereFilter filter = new WhereFilter(TESTC_NAME, Operator.EQUAL, "'' OR 1=1;");
        final DatabaseServiceResults<QueryResult> queryRes = staticDbService.queryRecord(tableName,
                List.of(TESTC_ID, TESTC_NAME), List.of(filter));

        Assertions.assertEquals(0, queryRes.result().count(), "Expected query result to have 0 results");
        Assertions.assertNull(queryRes.exception(), "Expected query exception to be null");

    }

    /**
     * Test ensuring that we can update records correctly: - Inserts 5 records -
     * updates record with id = 3 to have name = "updated"
     * Does not return the updated records
     */
    @Test
    void testUpdateDataNoReturn_Success() {

        final String tableName = getTableName();

        staticDbService.createTable(tableName, TEST_COLUMNS);

        // Insert 5 records
        for (int i = 0; i < 5; i++) {
            staticDbService.insertRecord(tableName, List.of(TESTC_NAME), List.of(TEST_NAME + (i + 1)));
        }

        // Update record with id = 3
        final ColumnSetter column = new ColumnSetter(TESTC_NAME, "updated");
        final WhereFilter filter = new WhereFilter(TESTC_ID, Operator.EQUAL, "3");
        final DatabaseServiceResults<UpdateResult> updateRes = staticDbService.updateRecord(tableName, List.of(column),
                List.of(filter), false);

        Assertions.assertTrue(updateRes.result().updated().isEmpty(), "Expected empty list returned in update res");
        Assertions.assertEquals(1, updateRes.result().rowsUpdated(), "Expected update result to have 1 result");
        Assertions.assertEquals(QueryOperation.UPDATE, updateRes.operation(),
                "Expected operation to be an update operation");
        Assertions.assertTrue(updateRes.successful(), "Expected update operation to be successful");

        // Find records with name = "updated"
        final WhereFilter filter2 = new WhereFilter(TESTC_NAME, Operator.EQUAL, "updated");
        final DatabaseServiceResults<QueryResult> queryRes = staticDbService.queryRecord(tableName,
                List.of(TESTC_ID, TESTC_NAME), List.of(filter2));

        Assertions.assertEquals(1, queryRes.result().count(), "Expected query result to have 1 result");

        final Map<String, Object> resultMap = queryRes.result().results().getFirst();
        Assertions.assertTrue(resultMap.containsKey(TESTC_ID), "Expected query result to have an id key");
        Assertions.assertEquals(3, resultMap.get(TESTC_ID), "Expected to have id 3");

        Assertions.assertTrue(resultMap.containsKey(TESTC_NAME), "Expected query result to have a name key");
        Assertions.assertEquals("updated", resultMap.get(TESTC_NAME), "Expected to have name updated");

    }

    /**
     * Test that inserts serveral records and asks for the updated records back.
     * Verifies the updated records have been modified correctly and been updated correctly
     */
    @Test
    void testUpdateReturnData_Success() {

        final String tableName = getTableName();

        staticDbService.createTable(tableName, TEST_COLUMNS);

        // Insert 5 records
        for (int i = 0; i < 5; i++) {
            staticDbService.insertRecord(tableName, List.of(TESTC_NAME), List.of(TEST_NAME + (i + 1)));
        }

        // Update record with id = 3
        final ColumnSetter column = new ColumnSetter(TESTC_NAME, "updated");
        final WhereFilter filter = new WhereFilter(TESTC_ID, Operator.EQUAL, "3");
        final DatabaseServiceResults<UpdateResult> updateRes = staticDbService.updateRecord(tableName, List.of(column),
                List.of(filter), true);

        Assertions.assertFalse(updateRes.result().updated().isEmpty(), "Expected to have list of results in update result");
        Assertions.assertEquals(1, updateRes.result().rowsUpdated(), "Expected update result to have 1 result");
        Assertions.assertEquals(QueryOperation.UPDATE, updateRes.operation(),
                "Expected operation to be an update operation");
        Assertions.assertTrue(updateRes.successful(), "Expected update operation to be successful");

        // Validate the updated record
        final UpdateResult updateResObj = updateRes.result();
        Assertions.assertEquals(1, updateResObj.rowsUpdated(), "Expected update result to have 1 result");

        final Map<String, Object> resultMap = updateRes.result().updated().getFirst();
        Assertions.assertTrue(resultMap.containsKey(TESTC_ID), "Expected update result to have an id key");
        Assertions.assertEquals(3, resultMap.get(TESTC_ID), "Expected to have id 3");

        Assertions.assertTrue(resultMap.containsKey(TESTC_NAME), "Expected query result to have a name key");
        Assertions.assertEquals("updated", resultMap.get(TESTC_NAME), "Expected to have name updated");
    }

    /**
     * Helper method to get a table name for testing purposes.
     * 
     *
     * @return "testt" + Current time in milliseconds from the last digit to 6
     *         digits in
     */
    private static String getTableName() {
        final String millisStr = StringUtils.reverse(Long.toString(System.currentTimeMillis())).substring(0, 6);
        return TEST_TABLE_NAME + millisStr;
    }
}
