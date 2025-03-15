package com.thirdplace.testutils;

import com.thirdplace.service.ServiceArguments;

public class TestArgsLoader {

    private TestArgsLoader() {
        // private constructor to prevent instantiation
    }
    
    /**
     * Loads the arguments for the database bootstrap password and data path.
     * The arguments are expected to be passed as system properties.
     * For example {@code ./gradlew test -DDB_BOOTSTRAP_PW=secret -DDB_DATA_PATH=/path/to/data}
     */
    public static void loadArgs() {
        ServiceArguments.parseArguments(new String[] { "DB_BOOTSTRAP_PW=" + System.getProperty("DB_BOOTSTRAP_PW"),
                "DB_DATA_PATH=" + System.getProperty("DB_DATA_PATH") });
    }
}
