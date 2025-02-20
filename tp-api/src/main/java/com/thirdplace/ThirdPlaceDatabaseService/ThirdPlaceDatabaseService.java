package com.thirdplace.ThirdPlaceDatabaseService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdplace.ThirdPlaceDatabaseService.DatabaseServiceResults.DeleteResult;
import com.thirdplace.ThirdPlaceDatabaseService.DatabaseServiceResults.InsertResult;
import com.thirdplace.ThirdPlaceDatabaseService.DatabaseServiceResults.QueryResult;
import com.thirdplace.ThirdPlaceDatabaseService.DatabaseServiceResults.UpdateResult;

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

    private static final String SERVER_ALREADY_STOPPED = "Is server running?";

    private static final String CREATE_TABLE_FORMATTER = "CREATE TABLE IF NOT EXISTS %s.%s (%s)";

    private static final String QUERY_FORMATTER = "SELECT %s FROM %s.%s %s";
    private static final String INSERT_FORMATTER = "INSERT INTO %s.%s (%s) VALUES (%s) RETURNING id";
    private static final String UPDATE_FORMATTER = "UPDATE %s.%s SET %s %s";
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

    protected void initializeSchema() {
        final String sql = "CREATE SCHEMA IF NOT EXISTS " + getSchemaName();
        try (final Statement stmt = connection.createStatement()) {
            stmt.execute(sql.toString());
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
                DB_USER, DB_PASSWORD)) {

            // Check if database exists
            final PreparedStatement checkStmt = tempConnection
                    .prepareStatement("SELECT 1 FROM pg_database WHERE datname = 'thirdplace'");

            if (!checkStmt.executeQuery().next()) {
                // Database doesn't exist, create it
                Statement stmt = tempConnection.createStatement();
                stmt.execute("CREATE DATABASE thirdplace");
                LOGGER.info("ThirdPlace database created successfully");
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
            stopPostgresServer();
        }
        instance = null;
    }

    // Stop the PostgreSQL server
    protected static void stopPostgresServer() {
        try {
            final String command = "pg_ctl stop -D \"C:\\Program Files\\PostgreSQL\\17\\data\"";

            final boolean commandSuccess = runCommand(command, new String[] { SERVER_ALREADY_STOPPED });

            if (!commandSuccess) {
                LOGGER.error("Failed to stop PostgreSQL server");
                throw new ThirdPlaceDatabaseServiceRuntimeError(
                        ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_STOPPING_DB_SERVER,
                        "Failed to stop PostgreSQL server");
            }

            LOGGER.info("PostgreSQL server stopped successfully");
        } catch (Exception e) {
            LOGGER.error("Error stopping PostgreSQL server", e);

            if (e instanceof final ThirdPlaceDatabaseServiceRuntimeError rtErr) {
                throw rtErr;
            } else {
                throw new ThirdPlaceDatabaseServiceRuntimeError(
                        ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_STOPPING_DB_SERVER,
                        "Error stopping PostgreSQL server", e);
            }
        }
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
    public DatabaseServiceResults<InsertResult> insertRecord(final String tableName, final List<String> columns,
            final List<String> values) {

        final String columnsString = getCommaSeparatedValues(columns);

        final List<String> valueHolders = values.stream().map(v -> QUESTION).toList();
        final String valuesString = getCommaSeparatedValues(valueHolders);

        final String sql = String.format(INSERT_FORMATTER, getSchemaName(), tableName, columnsString, valuesString);
        int insertedId = -1;

        try {

            try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < values.size(); i++) {
                    pstmt.setString(i + 1, values.get(i));
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        insertedId = rs.getInt("id");
                        LOGGER.debug("Inserted record to table " + tableName + " with id " + insertedId);
                    }
                }
                return new DatabaseServiceResults<>(pstmt.toString(), QueryOperation.INSERT, null, true,
                    new InsertResult(1, insertedId));
            }

        } catch (final SQLException e) {
            LOGGER.error("Error inserting record", e);
            return new DatabaseServiceResults<>(sql, QueryOperation.INSERT, e, false,
                    new InsertResult(0, insertedId));
        }
    }

    /**
     * Update a record in a table
     * 
     * @param tableName    The name of the table to update
     * @param columns      The list of columns to update
     * @param values       The list of values to update. Must be in the same order
     *                     as the columns
     * @param whereClauses The where clauses to use for the update
     * @return A {@link DatabaseServiceResults} result of the update
     */
    public DatabaseServiceResults<UpdateResult> updateRecord(final String tableName, final List<ColumnSetter> columnSetters,
         @Nonnull final List<WhereFilter> whereClauses) {

        final String columnsString = columnSetters.stream().map(c -> c.bindColumn()).collect(Collectors.joining(COMMA_SEPARATOR));
        final String filterString = whereClauses.size() > 0
            ? "WHERE " + buildValuesToBind(whereClauses)
            : StringUtils.EMPTY;
        final String sqlString = String.format(UPDATE_FORMATTER, getSchemaName(), tableName, columnsString, filterString);

        try {
            final PreparedStatement pstmt = connection.prepareStatement(sqlString);
            // Bind columns then filters 
            int bindCount = 0;
            for (int i = 0; i < columnSetters.size(); i++) {
                pstmt.setString(i + 1, columnSetters.get(i).value());
                bindCount++;
            }
            for (int i = 0; i < whereClauses.size(); i++) {
                pstmt.setString(bindCount + i + 1, whereClauses.get(i).rightHandSide());
            }
            pstmt.executeUpdate();
            final UpdateResult updateResult = new UpdateResult(pstmt.getUpdateCount());
            return new DatabaseServiceResults<>(pstmt.toString(), QueryOperation.UPDATE, null, true, updateResult);

        } catch (final SQLException e) {

            final ThirdPlaceDatabaseServiceRuntimeError ex = new ThirdPlaceDatabaseServiceRuntimeError(
                    ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_RUNNING_UPDATE, "Error updating record", e);
            LOGGER.error("Error updating record", ex);
            return new DatabaseServiceResults<>(sqlString, QueryOperation.UPDATE, e, false,
                    new UpdateResult(0));

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
                return new DatabaseServiceResults<DeleteResult>(pstmt.toString(), QueryOperation.DELETE, null, true, deleteResult);
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

                final List<Map<String, Object>> results = new ArrayList<>();
                final AtomicInteger count = new AtomicInteger(0);

                // Add each record to the results
                while (rs.next()) {

                    // For each column asked for add it to the map at the appropriate index in the
                    // result list
                    columns.forEach(c -> {
                        try {
                            final int ind = count.get();
                            if (results.size() <= ind) {
                                results.add(new HashMap<>());
                            }
                            final Map<String, Object> resultMap = results.get(ind);
                            resultMap.put(c, rs.getObject(c));

                        } catch (final SQLException e) {
                            LOGGER.error("Error getting column value", e);
                            throw new ThirdPlaceDatabaseServiceRuntimeError(
                                    ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_GETTING_COLUMN_VALUE,
                                    "Error getting column value", e);
                        }
                    });

                    count.incrementAndGet();
                }

                final QueryResult queryResult = new QueryResult(results, count.get());
                return new DatabaseServiceResults<QueryResult>(pstmt.toString(), QueryOperation.SELECT, null, true, queryResult);
            }

        } catch (final SQLException e) {
            final ThirdPlaceDatabaseServiceRuntimeError ex = new ThirdPlaceDatabaseServiceRuntimeError(
                    ThirdPlaceDatabaseServiceRuntimeError.ErrorCode.ERROR_RUNNING_QUERY, "Error querying record", e);
            LOGGER.error("Error querying record", ex);
            return new DatabaseServiceResults<QueryResult>(sql, QueryOperation.SELECT, e, false,
                    new QueryResult(List.of(Map.of()), 0));
        }
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
