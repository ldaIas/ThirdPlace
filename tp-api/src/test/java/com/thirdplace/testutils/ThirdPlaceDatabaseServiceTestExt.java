package com.thirdplace.testutils;

import java.sql.SQLException;
import java.sql.Statement;

import com.thirdplace.service.ServiceArguments;
import com.thirdplace.thirdplacedatabaseservice.ThirdPlaceDatabaseService;

/**
 * Test extension class of {@link ThirdPlaceDatabaseService} that ensures we
 * insert to test schema. Also deletes inserted data.
 */
public class ThirdPlaceDatabaseServiceTestExt extends ThirdPlaceDatabaseService {

    static {
        ServiceArguments.parseArguments(new String[] { "DB_BOOTSTRAP_PW=" + System.getProperty("DB_BOOTSTRAP_PW"),
                "DB_DATA_PATH=" + System.getProperty("DB_DATA_PATH") });
    }

    public static final String TEST_SCHEMA = "test_schema";

    public ThirdPlaceDatabaseServiceTestExt() {
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
