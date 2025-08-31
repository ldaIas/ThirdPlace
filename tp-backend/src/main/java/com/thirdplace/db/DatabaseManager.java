package com.thirdplace.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdplace.AppDataSource;
import com.thirdplace.db.DatabaseConfig.DataSourceCacheKey;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);

    public static Connection getConnection() throws SQLException {
        final DataSourceCacheKey key = AppDataSource.getAppDatasource();
        return DatabaseConfig.getDataSource(key).getConnection();
    }

    public static void testConnection() {
        try (Connection conn = getConnection()) {
            conn.createStatement()
                    .executeQuery("CREATE SCHEMA IF NOT EXISTS " + AppDataSource.getAppDatasource().schemaName());

            LOGGER.debug("Database connection successful!");
        } catch (SQLException e) {
            LOGGER.error("Database connection failed: {} {}", e.getMessage(),
                    AppDataSource.getAppDatasource().schemaName());
        }
    }
}