package com.thirdplace.ThirdPlaceDatabaseService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdPlaceDatabaseService implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThirdPlaceDatabaseService.class);

    // Database connection configuration
    private final Connection connection;
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/thirdplace";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "0133";

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
            String os = System.getProperty("os.name").toLowerCase();
            String checkCommand, startCommand;

            if (os.contains("win")) {
                // Windows commands
                checkCommand = "pg_ctl status -D C:\\Program Files\\PostgreSQL\\17\\data";
                startCommand = "pg_ctl start -D C:\\Program Files\\PostgreSQL\\17\\data -l C:\\Program Files\\PostgreSQL\\17\\data\\logfile.log";
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
                    LOGGER.error("PostgreSQL server started successfully.");
                } else {
                    LOGGER.error("Failed to start PostgreSQL server. Check the logs for more details.");
                }
            }

            LOGGER.info("PostgreSQL server started successfully");
        } catch (Exception e) {
            LOGGER.error("Error starting PostgreSQL server", e);
            throw new ThirdPlaceDatabaseServiceException(
                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_STOPPING_DB_SERVER,
                    "Error starting PostgreSQL server", e);
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

            LOGGER.info("PostgreSQL server started successfully");
        } catch (Exception e) {
            LOGGER.error("Error starting PostgreSQL server", e);
            throw new ThirdPlaceDatabaseServiceException(
                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_STOPPING_DB_SERVER,
                    "Error starting PostgreSQL server", e);
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

    // Create a new table
    public void createTable(String tableName, String[] columns) {
        try {
            StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
            for (int i = 0; i < columns.length; i++) {
                sql.append(columns[i]);
                if (i < columns.length - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");

            Statement stmt = connection.createStatement();
            stmt.execute(sql.toString());
        } catch (SQLException e) {
            LOGGER.error("Error creating table", e);
            throw new ThirdPlaceDatabaseServiceException(
                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_CREATING_TABLE, "Error creating table", e);
        }
    }

    // Insert a new record
    public void insertRecord(String tableName, String[] columns, String[] values) {
        try {
            StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
            for (int i = 0; i < columns.length; i++) {
                sql.append(columns[i]);
                if (i < columns.length - 1) {
                    sql.append(", ");
                }
            }
            sql.append(") VALUES (");
            for (int i = 0; i < values.length; i++) {
                sql.append("?");
                if (i < values.length - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");

            PreparedStatement pstmt = connection.prepareStatement(sql.toString());
            for (int i = 0; i < values.length; i++) {
                pstmt.setString(i + 1, values[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error inserting record", e);
            throw new ThirdPlaceDatabaseServiceException(
                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_RUNNING_INSERT, "Error inserting record", e);
        }
    }

    // Update a record
    public void updateRecord(String tableName, String[] columns, String[] values, String whereClause) {
        try {
            StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
            for (int i = 0; i < columns.length; i++) {
                sql.append(columns[i]).append("=?");
                if (i < columns.length - 1) {
                    sql.append(", ");
                }
            }
            sql.append(" WHERE ").append(whereClause);

            PreparedStatement pstmt = connection.prepareStatement(sql.toString());
            for (int i = 0; i < values.length; i++) {
                pstmt.setString(i + 1, values[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error updating record", e);
            throw new ThirdPlaceDatabaseServiceException(
                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_RUNNING_UPDATE, "Error updating record", e);
        }
    }

    // Delete a record
    public void deleteRecord(String tableName, String whereClause) {
        try {
            String sql = "DELETE FROM " + tableName + " WHERE " + whereClause;
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error deleting record", e);
            throw new ThirdPlaceDatabaseServiceException(
                    ThirdPlaceDatabaseServiceException.ErrorCode.ERROR_RUNNING_DELETE, "Error deleting record", e);
        }
    }
}
