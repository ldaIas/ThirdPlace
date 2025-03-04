package com.thirdplace.usertabledriver;

/**
 * Used in update queries. Set the id to the id of the record you want to update.
 * The record will be updated with the values in the other fields.
 * Fields with null values are skipped and unchanged after update.
 */
public record UserRecordMutate(
    Integer id,
    String username,
    String password,
    String email,
    String firstName,
    String lastName
) implements UserTableMutation {

    // Mainly for use in RecordUtils#init
    public static final String ID = UserTableDriver.ID_COLUMN;
    public static final String USERNAME = UserTableDriver.USERNAME_COLUMN;
    public static final String PASSWORD = UserTableDriver.PASSWORD_COLUMN;
    public static final String EMAIL = UserTableDriver.EMAIL_COLUMN;
    public static final String FIRST_NAME = UserTableDriver.FIRST_NAME_COLUMN;
    public static final String LAST_NAME = UserTableDriver.LAST_NAME_COLUMN;
 }