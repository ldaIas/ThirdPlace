package com.thirdplace.ThirdPlaceDatabaseService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdplace.ThirdPlaceDatabaseService.DatabaseServiceResults.DeleteResult;
import com.thirdplace.ThirdPlaceDatabaseService.DatabaseServiceResults.InsertResult;
import com.thirdplace.ThirdPlaceDatabaseService.DatabaseServiceResults.QueryResult;
import com.thirdplace.ThirdPlaceDatabaseService.DatabaseServiceResults.UpdateResult;

import jakarta.annotation.Nonnull;

public class ThirdPlaceDatabaseService implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThirdPlaceDatabaseService.class);

    // Database connection configuration
    private final Connection connection;
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/thirdplace";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "0133";

    private static final String COMMA_SEPARATOR = ", ";
    private static final String LEFT_PARAN = "(";
    private static final String RIGHT_PARAN = ")";

    final String QUERY_FORMATTER = "SELECT %s FROM %s WHERE %s";
    final String UPDATE_FORMATTER = "UPDATE %s SET %s WHERE %s";
    final String DELETE_FORMATTER = "DELETE FROM %s WHERE %s";

    // Constructor to initialize database connection
    public ThirdPlaceDatabaseService() {
        try {
            startPostgresServer();
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            LOGGER.error("Error acquiring connection to database", e);
            throw new ThirdPlaceDatabaseServiceException(
                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_GETTING_CONNECTION,
                    "Error acquiring connection to database", e);
        }
    }

    // Start the PostgreSQL server
    private void startPostgresServer() {
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

            final String commandCheckString = "server is running";
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
            throw new ThirdPlaceDatabaseServiceException(
                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_STARTING_DB_SERVER,
                    "Error starting PostgreSQL server", e);
        }
    }

    private void ensureThirdPlaceDatabase() {

        LOGGER.info("Ensuring ThirdPlace database exists");

        // Connect to default postgres database first
        try (final Connection tempConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
                DB_USER, DB_PASSWORD)) {

            // Check if database exists
            PreparedStatement checkStmt = tempConnection
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
            throw new ThirdPlaceDatabaseServiceException(
                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_CREATING_DATABASE,
                    "Error ensuring ThirdPlace database exists", e);
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
        stopPostgresServer();
    }

    // Stop the PostgreSQL server
    private void stopPostgresServer() {
        try {
            final String os = System.getProperty("os.name").toLowerCase();
            final ProcessBuilder processBuilder;

            if (os.contains("win")) {
                // Command for Windows
                processBuilder = new ProcessBuilder("cmd.exe", "/c", "pg_ctl", "stop", "-D",
                        "C:\\Program Files\\PostgreSQL\\17\\data", "-l",
                        "C:\\Program Files\\PostgreSQL\\17\\data\\log\\logfile.log");
            } else {
                // Command for Linux/Unix
                processBuilder = new ProcessBuilder("/bin/bash", "-c",
                        "pg_ctl start -D /path/to/data -l /path/to/logfile.log");
            }

            // Redirect error and output streams
            processBuilder.redirectErrorStream(true);
            processBuilder.inheritIO();

            // Start the process
            final Process process = processBuilder.start();

            // Wait for the process to complete (optional)
            final int exitCode = process.waitFor();

            if (exitCode != 0) {
                LOGGER.error("Failed to stop PostgreSQL server");
                throw new ThirdPlaceDatabaseServiceException(
                        ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_STOPPING_DB_SERVER,
                        "Failed to stop PostgreSQL server");
            }

            LOGGER.info("PostgreSQL server stopped successfully");
        } catch (Exception e) {
            LOGGER.error("Error stopping PostgreSQL server", e);
            throw new ThirdPlaceDatabaseServiceException(
                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_STOPPING_DB_SERVER,
                    "Error stopping PostgreSQL server", e);
        }
    }

    private static boolean runCommand(String command, String searchStr) throws IOException, InterruptedException {
        final ProcessBuilder processBuilder;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
        }

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (StringUtils.contains(line, searchStr)) {
                    return true;
                }
            }
        }

        int exitCode = process.waitFor();
        return exitCode == 0;
    }

    /**
     * Create a new table with the given name and columns
     * 
     * @param tableName The name of the table to create
     * @param columns   The columns of the table to create
     */
    public void createTable(final String tableName, final List<TableColumnType> columns) {
        try {
            final StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
            final List<String> columnDefinitions = columns.stream()
                    .map(c -> c.columnName() + StringUtils.SPACE + c.columnType()).toList();
            final String columnDefinitionsString = getCommaSeparatedValues(columnDefinitions);
            sql.append(columnDefinitionsString);
            sql.append(")");

            final Statement stmt = connection.createStatement();
            stmt.execute(sql.toString());
        } catch (SQLException e) {
            LOGGER.error("Error creating table", e);
            throw new ThirdPlaceDatabaseServiceException(
                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_CREATING_TABLE, "Error creating table", e);
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

        final StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        final String columnsString = getCommaSeparatedValues(columns);
        sql.append(columnsString);
        sql.append(RIGHT_PARAN);
        sql.append(" VALUES " + LEFT_PARAN);

        final String valuesString = getCommaSeparatedValues(values);
        sql.append(valuesString);
        sql.append(")");
        final String sqlString = sql.toString();

        try {

            final PreparedStatement pstmt = connection.prepareStatement(sqlString);
            for (int i = 0; i < values.size(); i++) {
                pstmt.setString(i + 1, values.get(i));
            }
            pstmt.executeUpdate();
            return new DatabaseServiceResults<InsertResult>(sqlString, QueryOperation.INSERT, null, true, new InsertResult(1));

        } catch (final SQLException e) {
            LOGGER.error("Error inserting record", e);
            return new DatabaseServiceResults<InsertResult>(sqlString, QueryOperation.INSERT, e, false, new InsertResult(0));
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
    public DatabaseServiceResults<UpdateResult> updateRecord(final String tableName, final List<String> columns,
            final List<String> values, @Nonnull final List<WhereFilter> whereClauses) {

        final List<String> columnNames = columns.stream().map(c -> c + "=?").toList();
        final String columnsString = getCommaSeparatedValues(columnNames);
        final String sql = String.format(UPDATE_FORMATTER, tableName, columnsString,
                buildWhereFilterString(whereClauses));

        final String sqlString = sql.toString();
        try {
            final PreparedStatement pstmt = connection.prepareStatement(sqlString);
            for (int i = 0; i < values.size(); i++) {
                pstmt.setString(i + 1, values.get(i));
            }
            pstmt.executeUpdate();
            final UpdateResult updateResult = new UpdateResult(pstmt.getUpdateCount());
            return new DatabaseServiceResults<UpdateResult>(sqlString, QueryOperation.UPDATE, null, true, updateResult);

        } catch (final SQLException e) {

            final ThirdPlaceDatabaseServiceException ex = new ThirdPlaceDatabaseServiceException(

                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_RUNNING_UPDATE, "Error updating record", e);
            LOGGER.error("Error updating record", ex);
            return new DatabaseServiceResults<UpdateResult>(sqlString, QueryOperation.UPDATE, e, false,
                    new UpdateResult(0));

        }
    }

    private static String buildWhereFilterString(final List<WhereFilter> whereClauses) {
        return whereClauses.stream().map(WhereFilter::toString).collect(Collectors.joining(" AND "));
    }

    /**
     * Delete a record from a table using the given where clause
     *
     * @param tableName    The name of the table to delete from
     * @param whereClauses The where clauses to use for the delete
     * @return A {@link DatabaseServiceResults} result of the delete
     */
    public DatabaseServiceResults<DeleteResult> deleteRecord(final String tableName,
            @Nonnull final List<WhereFilter> whereClauses) {

        final String whereClauseString = buildWhereFilterString(whereClauses);

        final String sql = String.format(DELETE_FORMATTER, tableName, whereClauseString);

        try {

            final PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.executeUpdate();

            final DeleteResult deleteResult = new DeleteResult(pstmt.getUpdateCount());
            return new DatabaseServiceResults<DeleteResult>(sql, QueryOperation.DELETE, null, true, deleteResult);

        } catch (final SQLException e) {
            final ThirdPlaceDatabaseServiceException ex = new ThirdPlaceDatabaseServiceException(
                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_RUNNING_DELETE, "Error deleting record", e);
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

        final String sql = String.format(QUERY_FORMATTER, getCommaSeparatedValues(columns), tableName,
                buildWhereFilterString(whereClauses));

        try {
            final PreparedStatement pstmt = connection.prepareStatement(sql);
            final ResultSet rs = pstmt.executeQuery();

            final Map<String, Object> results = new HashMap<>();
            while (rs.next()) {
                columns.forEach(c -> {
                    try {
                        results.put(c, rs.getObject(c));
                    } catch (final SQLException e) {
                        LOGGER.error("Error getting column value", e);
                        throw new ThirdPlaceDatabaseServiceException(
                                ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_GETTING_COLUMN_VALUE,
                                "Error getting column value", e);
                    }
                });
            }
            final QueryResult queryResult = new QueryResult(results);
            return new DatabaseServiceResults<QueryResult>(sql, QueryOperation.SELECT, null, true, queryResult);

        } catch (final SQLException e) {
            final ThirdPlaceDatabaseServiceException ex = new ThirdPlaceDatabaseServiceException(
                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_RUNNING_QUERY, "Error querying record", e);
            LOGGER.error("Error querying record", ex);
            return new DatabaseServiceResults<QueryResult>(sql, QueryOperation.SELECT, e, false,
                    new QueryResult(Map.of()));
        }
    }
}
