package com.thirdplace.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.Nullable;

/**
 * Class to parse and store command line arguments for the service
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
    private static final String INVALID_ARG_FORMATTER = "Invalid argument: %s";

    /**
     * <p>Parse the command line arguments and store them in the arguments map.
     * Format of arguments: {@code <argument>=<value>} </p>
     * Example: {@code -DB_BOOTSTRAP_PW=1234}
     * @param args The command line arguments
     * @throws IllegalArgumentException if the argument is not in the correct format
     */
    public static void parseArguments(final String[] args) {
        final Map<Argument, String> tempArgs = new HashMap<>();
        for (final String arg : args) {

            if (!arg.contains(ARG_SPLIT)) {
                throw new IllegalArgumentException(String.format(INVALID_ARG_FORMATTER, arg));
            }

            final String[] split = arg.split(ARG_SPLIT);
            final String argString = split[0];
            final Argument argument = Optional.ofNullable(Argument.valueOf(argString))
                .orElseThrow(() -> new IllegalArgumentException(String.format(INVALID_ARG_FORMATTER, argString)));

            if (split.length != 2) {
                throw new IllegalArgumentException(String.format(INVALID_ARG_FORMATTER, arg));
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
