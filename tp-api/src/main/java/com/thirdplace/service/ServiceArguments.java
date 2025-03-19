package com.thirdplace.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.Nullable;

/**
 * Class to parse and store service arguments
 */
public class ServiceArguments {
    
    public enum Argument {
        DB_BOOTSTRAP_PW,
        DB_HOST,
        DB_PORT,
        DB_USER,
        DB_NAME
    }

    private static final Map<Argument, String> arguments = new HashMap<>();

    private static boolean isInitialized = false;
    private static final String ARG_SPLIT = "=";

    public static void parseArguments(final String[] args) {
        final Map<Argument, String> tempArgs = new HashMap<>();
        for (final String arg : args) {

            if (!arg.contains(ARG_SPLIT)) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }

            final String[] split = arg.split(ARG_SPLIT);
            final String argString = split[0];
            final Argument argument = Optional.ofNullable(Argument.valueOf(argString))
                .orElseThrow(() -> new IllegalArgumentException("Invalid argument: " + argString));

            if (split.length != 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            
            final String value = split[1];
            tempArgs.put(argument, value);
        }
        arguments.putAll(tempArgs);
        isInitialized = true;
    }

    /**
     * Get the argument value
     * @param argument
     * @return The argument value, if any
     */
    @Nullable
    public static String getArgument(final Argument argument) {
        if (!isInitialized) {
            throw new IllegalStateException("ServiceArguments not initialized");
        }
        return arguments.get(argument);
    }
    
}
