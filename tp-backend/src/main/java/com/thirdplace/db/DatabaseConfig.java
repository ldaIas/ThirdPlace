package com.thirdplace.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DatabaseConfig {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/thirdplace";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "postgres";
    
    private static HikariDataSource dataSource;
    
    public static DataSource getDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setUsername(DB_USERNAME);
            config.setPassword(DB_PASSWORD);
            config.setMaximumPoolSize(10);
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }
    
    public static void close() {
        System.out.println("Close called");
        if (dataSource != null) {
            dataSource.close();
        }
    }
}