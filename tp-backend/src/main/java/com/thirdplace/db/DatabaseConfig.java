package com.thirdplace.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

public class DatabaseConfig {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/thirdplace";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "postgres";
    private static HikariDataSource dataSource;

    public static record DataSourceCacheKey(
            String schemaName) {
    }

    private static final Map<DataSourceCacheKey, HikariDataSource> datasourceCache = new ConcurrentHashMap<>();

    public static DataSource getDataSource(final DataSourceCacheKey key) {
        final HikariDataSource dataSource = datasourceCache.computeIfAbsent(key, k -> {
            final HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setUsername(DB_USERNAME);
            config.setPassword(DB_PASSWORD);
            config.setMaximumPoolSize(10);
            config.setSchema(k.schemaName());
            return new HikariDataSource(config);
        });
        return dataSource;
    }

    public static void close() {
        System.out.println("Close called");
        if (dataSource != null) {
            dataSource.close();
        }
    }
}