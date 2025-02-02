package com.thirdplace.ThirdPlaceDatabaseService;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test extension class of {@link ThirdPlaceDatabaseService} that ensures we
 * insert to test schema. Also deletes inserted data.
 */
public class ThirdPlaceDatabaseServiceTestExt extends ThirdPlaceDatabaseService {

    public static final String TEST_SCHEMA = "test_schema";

    ThirdPlaceDatabaseServiceTestExt() {
        super();
        getInstance();
    }

    @Override
    protected String getSchemaName() {
        return TEST_SCHEMA;
    }

    protected void runSql(final String sql) {
        try (final Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (getRefCount() == 1) {
            runSql("DROP SCHEMA IF EXISTS " + TEST_SCHEMA + " CASCADE");
        }
        super.close();
    }
}
