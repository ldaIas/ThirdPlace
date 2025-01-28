package com.thirdplace.ThirdPlaceDatabaseService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to drive the user table creation and manipulation
 */
public class UserTableDriver {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserTableDriver.class);

    public static final String TABLE_NAME = "users";

    public static final String ID_COLUMN = "id";
    public static final String USERNAME_COLUMN = "username";
    public static final String PASSWORD_COLUMN = "password";
    public static final String EMAIL_COLUMN = "email";
    public static final String FIRST_NAME_COLUMN = "first_name";
    public static final String LAST_NAME_COLUMN = "last_name";
    public static final String CREATED_AT_COLUMN = "created_at";
    public static final String UPDATED_AT_COLUMN = "updated_at";

    private static final List<TableColumnType> COLUMN_TYPES = List.of(
        new TableColumnType(ID_COLUMN, "INTEGER PRIMARY KEY AUTOINCREMENT"),
        new TableColumnType(USERNAME_COLUMN, "TEXT NOT NULL"),
        new TableColumnType(PASSWORD_COLUMN, "TEXT NOT NULL"),
        new TableColumnType(EMAIL_COLUMN, "TEXT NOT NULL"),
        new TableColumnType(FIRST_NAME_COLUMN, "TEXT"),
        new TableColumnType(LAST_NAME_COLUMN, "TEXT"),
        new TableColumnType(CREATED_AT_COLUMN, "TEXT DEFAULT CURRENT_TIMESTAMP"),
        new TableColumnType(UPDATED_AT_COLUMN, "TEXT DEFAULT CURRENT_TIMESTAMP")
    );

    final ThirdPlaceDatabaseService dbService;

    public UserTableDriver(final ThirdPlaceDatabaseService dbService) {
        LOGGER.debug("Creating UserTableDriver");
        this.dbService = dbService;

    }


    public void init() {
        
        LOGGER.debug("Initializing UserTableDriver");

        // Create the users table
        dbService.createTable(TABLE_NAME, COLUMN_TYPES);
    }

    public void insertUserRecord(final UserRecord userRecord) {
        LOGGER.debug("Inserting user record: " + userRecord);
        
    }

}
