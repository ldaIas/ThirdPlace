package com.thirdplace.testutils;

import java.util.Arrays;

import com.thirdplace.service.ServiceArguments;

public class TestArgsLoader {

    private TestArgsLoader() {
        // private constructor to prevent instantiation
    }

    private static final String ARG_FORMATTER = "%s=%s";
    /**
     * Loads the arguments for the database bootstrap password and data path. The
     * arguments are expected to be passed as system properties. For example
     * {@code ./gradlew test -DDB_BOOTSTRAP_PW=secret -DDB_DATA_PATH=/path/to/data}
     */
    public static void loadArgs() {
        final String[] args = Arrays.stream(ServiceArguments.Argument.values())
                .map(arg -> String.format(ARG_FORMATTER, arg.name(), System.getProperty(arg.name())))
                .toArray(String[]::new);

        ServiceArguments.parseArguments(args);
    }
}
