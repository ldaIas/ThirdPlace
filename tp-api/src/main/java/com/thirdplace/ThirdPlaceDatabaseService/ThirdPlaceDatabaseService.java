package com.thirdplace.thirdplacedatabaseservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdplace.thirdplacedatabaseservice.DatabaseServiceResults.DeleteResult;
import com.thirdplace.thirdplacedatabaseservice.DatabaseServiceResults.InsertResult;
import com.thirdplace.thirdplacedatabaseservice.DatabaseServiceResults.QueryResult;
import com.thirdplace.thirdplacedatabaseservice.DatabaseServiceResults.UpdateResult;
import com.thirdplace.usertabledriver.UserTableDriver;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ThirdPlaceDatabaseService implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThirdPlaceDatabaseService.class);

    // Database connection configuration
    private Connection connection;
    static final String DB_URL = "jdbc:postgresql://localhost:5432/thirdplace";
    static final String DB_USER = "postgres";
    static final String DB_PASSWORD = "3310";

    private static final String COMMA_SEPARATOR = ", ";
    private static final String QUESTION = "?";

    private static final String FILTER_FORMAT = "( %s )";
    private static final String WHERE_FORMAT = "%s::text %s ?";

    private static final String CREATE_TABLE_FORMATTER = "CREATE TABLE IF NOT EXISTS %s.%s (%s)";

    private static final String QUERY_FORMATTER = "SELECT %s FROM %s.%s %s";
    private static final String INSERT_FORMATTER = "INSERT INTO %s.%s (%s) VALUES (%s) RETURNING *";

    // UPDATE schema.table SET columns values RETURNING *
    private static final String UPDATE_FORMATTER = "UPDATE %s.%s SET %s %s %s";
    private static final String RETURNING = " RETURNING *";
    private static final String DELETE_FORMATTER = "DELETE FROM %s.%s WHERE %s";

    private static final String DEFAULT_SCHEMA = "prod";

    private static AtomicInteger refCount = new AtomicInteger(0);

    private static ThirdPlaceDatabaseService instance;

    public static ThirdPlaceDatabaseService getInstance() {

        if (instance == null) {
            instance = new ThirdPlaceDatabaseService();
        }

        refCount.incrementAndGet();
        return instance;
    }

    // Constructor to initialize database connection
    protected ThirdPlaceDatabaseService() {
        try {
            startPostgresServer();
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            initializeSchema();
        } catch (SQLException e) {
            LOGGER.error("Error acquiring connection to database", e);
            throw new ThirdPlaceDatabaseServiceRuntimeError(
                    ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_GETTING_CONNECTION,
                    "Error acquiring connection to database", e);
        }
    }

    private void initializeSchema() {
        final String sql = "CREATE SCHEMA IF NOT EXISTS " + getSchemaName();
        try (final Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.error("Error creating table", e);
            throw new ThirdPlaceDatabaseServiceRuntimeError(
                    ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_CREATING_TABLE, "Error creating table", e);
        }
    }

    protected String getSchemaName() {
        return DEFAULT_SCHEMA;
    }

    // Start the PostgreSQL server
    protected static void startPostgresServer() {
        try {
            // Detect the OS
            final String os = System.getProperty("os.name").toLowerCase();
            final String checkCommand, startCommand;

            if (os.contains("win")) {
                // Windows commands
                checkCommand = "pg_ctl status -D \"C:\\Program Files\\PostgreSQL\\17\\data\"";
                startCommand = "pg_ctl start -D \"C:\\Program Files\\PostgreSQL\\17\\data\" -l \"C:\\Program Files\\PostgreSQL\\17\\data\\log\\logfile.log\"";
            } else {
                // Linux/Unix commands
                checkCommand = "pg_ctl status -D /path/to/data";
                startCommand = "pg_ctl start -D /path/to/data -l /path/to/logfile.log";
            }

            final String[] commandCheckString = { "server is running" };
            if (runCommand(checkCommand, commandCheckString)) {
                LOGGER.debug("PostgreSQL is already running.");
            } else {
                LOGGER.debug("PostgreSQL is not running. Starting the server...");
                if (runCommand(startCommand, null)) {
                    LOGGER.info("PostgreSQL server started successfully.");
                } else {
                    LOGGER.error("Failed to start PostgreSQL server. Check the logs for more details.");
                }
            }

            // Ensure the ThirdPlace database exists
            ensureThirdPlaceDatabase();

        } catch (Exception e) {
            LOGGER.error("Error starting PostgreSQL server", e);
            throw new ThirdPlaceDatabaseServiceRuntimeError(
                    ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_STARTING_DB_SERVER,
                    "Error starting PostgreSQL server", e);
        }
    }

    private static void ensureThirdPlaceDatabase() {

        LOGGER.info("Ensuring ThirdPlace database exists");

        // Connect to default postgres database first
        try (final Connection tempConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
                DB_USER, DB_PASSWORD);
                final PreparedStatement checkStmt = tempConnection
                        .prepareStatement("SELECT 1 FROM pg_database WHERE datname = 'thirdplace'")) {

            if (!checkStmt.executeQuery().next()) {
                // Database doesn't exist, create it
                try (final Statement stmt = tempConnection.createStatement()) {
                    stmt.execute("CREATE DATABASE thirdplace");
                    LOGGER.info("ThirdPlace database created successfully");
                }
            } else {
                LOGGER.debug("ThirdPlace database already exists");
            }

        } catch (SQLException e) {
            LOGGER.error("Error ensuring ThirdPlace database exists", e);
            throw new ThirdPlaceDatabaseServiceRuntimeError(
                    ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_CREATING_DATABASE,
                    "Error ensuring ThirdPlace database exists", e);
        }
    }

    protected int getRefCount() {
        return refCount.get();
    }

    /**
     * Close the database service and connection when the last reference is closed
     */
    @Override
    public void close() throws Exception {
        refCount.decrementAndGet();
        if (refCount.get() > 0) {
            return;
        }
        if (connection != null) {
            connection.close();
            connection = null;
        }
        instance = null;
    }

    /**
     * Run a command and check if the output contains the search strings, or return
     * whether the command was successful (exit 0)
     * 
     * @param command    The command to run
     * @param searchStrs The search strings to look for in the output (null for
     *                   none)
     * @return True if the command was successful (exit 0) or the output contains
     *         the search strings. False if the command had a non-zero exit code or
     *         the process did not finish within the timeout
     * @implNote Process timeout is {@link #PROCESS_TIMEOUT}
     * @throws IOException
     * @throws InterruptedException
     */
    private static boolean runCommand(final String command, @Nullable final String[] searchStrs)
            throws IOException, InterruptedException {
        final ProcessBuilder processBuilder;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        }

        processBuilder.redirectErrorStream(true);

        final Process process = processBuilder.start();
        final int finished = process.waitFor();

        final List<String> lines = new ArrayList<>();
        try (final BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                final BufferedReader stderrReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {

            // Read stdout
            char[] buff = new char[1024];
            stdoutReader.read(buff, 0, 1024);
            final String output = new String(buff);
            lines.add(output);

            // Read stderr
            buff = new char[1024];
            stderrReader.read(buff, 0, 1024);
            final String errOutput = new String(buff);
            lines.add(errOutput);
        }

        if (searchStrs != null) {
            for (final String searchStr : searchStrs) {
                if (lines.stream().anyMatch(l -> StringUtils.containsIgnoreCase(l, searchStr))) {
                    return true;
                }
            }
        }

        return finished == 0;
    }

    /**
     * Create a new table with the given name and columns
     * 
     * @param tableName The name of the table to create
     * @param columns   The columns of the table to create
     */
    public void createTable(final String tableName, final List<TableColumnType> columns) {

        final List<String> columnDefinitions = columns.stream()
                .map(c -> c.columnName() + StringUtils.SPACE + c.columnType()).toList();
        final String columnDefinitionsString = getCommaSeparatedValues(columnDefinitions);
        final String sql = String.format(CREATE_TABLE_FORMATTER, getSchemaName(), tableName, columnDefinitionsString);

        try (final Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.error("Error creating table", e);
            throw new ThirdPlaceDatabaseServiceRuntimeError(
                    ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_CREATING_TABLE, "Error creating table", e);
        }
    }

    private static String getCommaSeparatedValues(final List<String> values) {
        return values.stream().collect(Collectors.joining(COMMA_SEPARATOR));
    }

    /**
     * Insert a record into a table
     * 
     * @param tableName The name of the table to insert into
     * @param columns   The list of columns to insert into (the rest are defaulted
     *                  null)
     * @param values    The list of values to insert. Must be in the same order as
     *                  the columns
     * @return A {@link DatabaseServiceResults} result of the insert
     */
    public DatabaseServiceResults<InsertResult> insertRecord(final String tableName,
            final List<ColumnSetter> columnSetters) {

        final String columnsString = columnSetters.stream().map(c -> c.column())
                .collect(Collectors.joining(COMMA_SEPARATOR));
        final String valuesString = columnSetters.stream().map(c -> QUESTION)
                .collect(Collectors.joining(COMMA_SEPARATOR));

        final String sql = String.format(INSERT_FORMATTER, getSchemaName(), tableName, columnsString, valuesString);
        int insertedId = -1;

        final List<String> values = columnSetters.stream().map(ColumnSetter::value).toList();
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                pstmt.setString(i + 1, values.get(i));
            }

            final Map<String, Object> record = new HashMap<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    insertedId = rs.getInt(UserTableDriver.ID_COLUMN);
                    // Add each column from the result to the return ecord
                    final int colCount = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= colCount; i++) {
                        final String columnName = rs.getMetaData().getColumnName(i);
                        final Object columnValue = rs.getObject(i);
                        record.put(columnName, columnValue);
                    }
                    LOGGER.debug("Inserted record to table " + tableName + " with id " + insertedId);
                }
            }
            return new DatabaseServiceResults<>(pstmt.toString(), QueryOperation.INSERT, null, true,
                    new InsertResult(record, 1));

        } catch (final SQLException e) {
            LOGGER.error("Error inserting record", e);
            return new DatabaseServiceResults<>(sql, QueryOperation.INSERT, e, false, new InsertResult(Map.of(), 0));
        }
    }

    /**
     * Update a record in a table
     * 
     * @param tableName     The name of the table to update
     * @param columns       The list of columns to update
     * @param values        The list of values to update. Must be in the same order
     *                      as the columns
     * @param whereClauses  The where clauses to use for the update
     * @param returnUpdated Whether to return the updated record(s)
     * @return A {@link DatabaseServiceResults} result of the update
     */
    public DatabaseServiceResults<UpdateResult> updateRecord(final String tableName,
            final List<ColumnSetter> columnSetters, @Nonnull final List<WhereFilter> whereClauses,
            final boolean returnUpdated) {

        final String columnSettersString = columnSetters.stream().map(c -> c.bindColumn())
                .collect(Collectors.joining(COMMA_SEPARATOR));

        final String filterString = whereClauses.size() > 0 ? "WHERE " + buildValuesToBind(whereClauses)
                : StringUtils.EMPTY;

        final String returningString = returnUpdated ? RETURNING : StringUtils.EMPTY;

        final String sqlString = String.format(UPDATE_FORMATTER, getSchemaName(), tableName, columnSettersString,
                filterString, returningString);

        try (final PreparedStatement pstmt = connection.prepareStatement(sqlString)) {

            // Bind columns then filters
            int bindCount = 0;
            for (int i = 0; i < columnSetters.size(); i++) {
                pstmt.setString(i + 1, columnSetters.get(i).value());
                bindCount++;
            }
            for (int i = 0; i < whereClauses.size(); i++) {
                pstmt.setString(bindCount + i + 1, whereClauses.get(i).rightHandSide());
            }

            // If return data is true, execute as query to loop through results
            // Otherwise, just execute the update and return the empty success result
            if (returnUpdated) {
                try (final ResultSet updateResultSet = pstmt.executeQuery()) {
                    final List<Map<String, Object>> updatedRecords = new ArrayList<>();
                    while (updateResultSet.next()) {
                        final Map<String, Object> record = new HashMap<>();
                        final ResultSetMetaData metaData = updateResultSet.getMetaData();
                        final int columnCount = metaData.getColumnCount();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            record.put(columnName, updateResultSet.getObject(i));
                        }
                        updatedRecords.add(record);
                    }

                    final UpdateResult updateResult = new UpdateResult(Collections.unmodifiableList(updatedRecords),
                            updatedRecords.size());
                    return new DatabaseServiceResults<>(pstmt.toString(), QueryOperation.UPDATE, null, true,
                            updateResult);
                }
            } else {
                pstmt.executeUpdate();
                return new DatabaseServiceResults<>(pstmt.toString(), QueryOperation.UPDATE, null, true,
                        new UpdateResult(Collections.emptyList(), pstmt.getUpdateCount()));
            }

        } catch (final SQLException e) {

            final ThirdPlaceDatabaseServiceRuntimeError ex = new ThirdPlaceDatabaseServiceRuntimeError(
                    ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_RUNNING_UPDATE, "Error updating record", e);
            LOGGER.error("Error updating record", ex);
            return new DatabaseServiceResults<>(sqlString, QueryOperation.UPDATE, e, false,
                    new UpdateResult(List.of(), 0));

        }
    }

    /**
     * Build a string with the appropriate amount of bind values for the where
     * filter
     * 
     * @param whereClauses The where clauses to use for the update
     * @return A string with the appropriate amount of bind values for the where
     *         filter
     */
    private static String buildValuesToBind(List<WhereFilter> whereClauses) {

        return String.format(FILTER_FORMAT,
                whereClauses.stream().map(w -> String.format(WHERE_FORMAT, w.leftHandSide(), w.operator().getValue()))
                        .collect(Collectors.joining(" AND ")));

    }

    /**
     * Delete a record from a table using the given where clause
     *
     * @param tableName    The name of the table to delete from
     * @param whereClauses The where clauses to use for the delete. Most not be
     *                     empty
     * @return A {@link DatabaseServiceResults} result of the delete
     */
    public DatabaseServiceResults<DeleteResult> deleteRecord(final String tableName,
            @Nonnull final List<WhereFilter> whereClauses) {

        if (whereClauses == null || whereClauses.isEmpty()) {
            throw new ThirdPlaceDatabaseServiceRuntimeError(
                    ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_EMPTY_WHERE_CLAUSES,
                    "Where clauses cannot be empty for delete operation");
        }

        final String whereClauseString = buildValuesToBind(whereClauses);

        final String sql = String.format(DELETE_FORMATTER, getSchemaName(), tableName, whereClauseString);

        try {

            try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {

                // Bind values
                for (int i = 0; i < whereClauses.size(); i++) {
                    pstmt.setString(i + 1, whereClauses.get(i).rightHandSide());
                }

                pstmt.executeUpdate();

                final DeleteResult deleteResult = new DeleteResult(pstmt.getUpdateCount());
                return new DatabaseServiceResults<DeleteResult>(pstmt.toString(), QueryOperation.DELETE, null, true,
                        deleteResult);
            }

        } catch (final SQLException e) {
            final ThirdPlaceDatabaseServiceRuntimeError ex = new ThirdPlaceDatabaseServiceRuntimeError(
                    ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_RUNNING_DELETE, "Error deleting record", e);
            LOGGER.error("Error deleting record", ex);
            return new DatabaseServiceResults<DeleteResult>(sql, QueryOperation.DELETE, e, false, new DeleteResult(0));
        }
    }

    /**
     * Query a table using the given where clause
     * 
     * @param tableName    The name of the table to query
     * @param columns      The columns to query
     * @param whereClauses The where clauses to use for the query
     * @return A {@link DatabaseServiceResults} result of the query
     */
    public DatabaseServiceResults<QueryResult> queryRecord(final String tableName, final List<String> columns,
            final List<WhereFilter> whereClauses) {

        final int filterCount = whereClauses.size();
        final String filterString = filterCount > 0 ? "WHERE " + buildValuesToBind(whereClauses) : StringUtils.EMPTY;
        final String sql = String.format(QUERY_FORMATTER, getCommaSeparatedValues(columns), getSchemaName(), tableName,
                filterString);

        LOGGER.debug("Prepared statement for query: " + sql);
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {

            // Bind values
            for (int i = 0; i < whereClauses.size(); i++) {
                pstmt.setString(i + 1, whereClauses.get(i).rightHandSide());
            }

            try (final ResultSet rs = pstmt.executeQuery()) {

                final List<Map<String, Object>> results = getResults(rs);
                final QueryResult queryResult = new QueryResult(Collections.unmodifiableList(results), results.size());
                return new DatabaseServiceResults<QueryResult>(pstmt.toString(), QueryOperation.SELECT, null, true,
                        queryResult);
            }

        } catch (final SQLException e) {
            final ThirdPlaceDatabaseServiceRuntimeError ex = new ThirdPlaceDatabaseServiceRuntimeError(
                    ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_RUNNING_QUERY, "Error querying record", e);
            LOGGER.error("Error querying record", ex);
            return new DatabaseServiceResults<QueryResult>(sql, QueryOperation.SELECT, e, false,
                    new QueryResult(List.of(Map.of()), 0));
        }
    }

    private static List<Map<String, Object>> getResults(final ResultSet rs) throws SQLException {
        final List<Map<String, Object>> results = new ArrayList<>();
        while (rs.next()) {
            final Map<String, Object> record = new HashMap<>();
            final ResultSetMetaData metaData = rs.getMetaData();
            final int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                record.put(columnName, rs.getObject(i));
            }
            results.add(record);
        }
        return results;
    }

    /**
     * Check if a table exists in the database
     * 
     * @param tableName The name of the table to check
     * @return True if the table exists, false otherwise
     */
    public boolean tableExists(final String tableName) {
        try {
            final DatabaseMetaData metaData = connection.getMetaData();
            try (final ResultSet rs = metaData.getTables(null, getSchemaName(), tableName, null)) {
                return rs.next();
            }
        } catch (final SQLException e) {
            LOGGER.error("Error checking if table exists", e);
            throw new ThirdPlaceDatabaseServiceRuntimeError(
                    ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_CHECKING_IF_TABLE_EXISTS,
                    "Error checking if table exists", e);
        }
    }

    protected Connection getConnection() {
        return connection;
    }
}
