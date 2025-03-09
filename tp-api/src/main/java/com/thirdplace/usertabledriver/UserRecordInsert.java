package com.thirdplace.usertabledriver;

/**
 * Used in insert queries. Names of fields are the same as the column names in the database.
 */
public record UserRecordInsert(
    String username,
    String password,
    String email,
    String firstname,
    String lastname
) implements UserTableMutation {
    
    // For use with RecordUtils#init
    public static final String USERNAME = UserTableDriver.USERNAME_COLUMN;
    public static final String PASSWORD = UserTableDriver.PASSWORD_COLUMN;
    public static final String EMAIL = UserTableDriver.EMAIL_COLUMN;
    public static final String FIRST_NAME = UserTableDriver.FIRST_NAME_COLUMN;
    public static final String LAST_NAME = UserTableDriver.LAST_NAME_COLUMN;
 }
