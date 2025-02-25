package com.thirdplace.usertabledriver;

/**
 * Used in insert queries
 */
public record UserRecordInsert(
    String username,
    String password,
    String email,
    String firstName,
    String lastName
) implements UserTableMutation {
    
    // For use with RecordUtils#init
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String EMAIL = "email";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
 }
