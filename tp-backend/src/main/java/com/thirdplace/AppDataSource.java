package com.thirdplace;

import com.thirdplace.db.DatabaseConfig.DataSourceCacheKey;

public class AppDataSource {
    private static DataSourceCacheKey APP_DATASOURCE;

    public static DataSourceCacheKey getAppDatasource() {
        return APP_DATASOURCE;
    }

    /**
     * This should only be called once in main or by tests
     * @param key
     */
    public static void setAppDatasource(final DataSourceCacheKey key) {
        APP_DATASOURCE = key;
    }
}
