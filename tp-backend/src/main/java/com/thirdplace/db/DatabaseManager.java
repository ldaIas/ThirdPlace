package com.thirdplace.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdplace.db.DatabaseConfig.DataSourceCacheKey;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
    
    public static Connection getConnection(final DataSourceCacheKey key) throws SQLException {
        return DatabaseConfig.getDataSource(key).getConnection();
    }
    
    public static void testConnection(final DataSourceCacheKey key) {
        try (Connection conn = getConnection(key)) {
            LOGGER.debug("Database connection successful!");
            conn.createStatement().executeQuery("CREATE SCHEMA IF NOT EXISTS " + key.schemaName());
        } catch (SQLException e) {
            LOGGER.error("Database connection failed: {} {}", e.getMessage(), key);
        }
    }
}