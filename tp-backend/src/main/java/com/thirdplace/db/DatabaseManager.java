package com.thirdplace.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
    
    public static Connection getConnection() throws SQLException {
        return DatabaseConfig.getDataSource().getConnection();
    }
    
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            LOGGER.info("Database connection successful!");
        } catch (SQLException e) {
            LOGGER.error("Database connection failed: {}", e.getMessage());
        }
    }
}