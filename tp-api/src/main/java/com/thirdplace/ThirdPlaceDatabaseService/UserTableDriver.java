package com.thirdplace.ThirdPlaceDatabaseService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdplace.ThirdPlaceDatabaseService.DatabaseServiceResults.DeleteResult;
import com.thirdplace.ThirdPlaceDatabaseService.DatabaseServiceResults.InsertResult;
import com.thirdplace.ThirdPlaceDatabaseService.DatabaseServiceResults.QueryResult;
import com.thirdplace.ThirdPlaceDatabaseService.DatabaseServiceResults.UpdateResult;

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

    private static final String WILDCARD = "*";
    private static final String TEXT_CAST = "::text";

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

    // Columns used to update a user
    private static final List<String> UPDATE_COLUMNS = List.of(
        USERNAME_COLUMN,
        PASSWORD_COLUMN,
        EMAIL_COLUMN,
        FIRST_NAME_COLUMN,
        LAST_NAME_COLUMN
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

    /**
     * Insert a user record into the users table
     * @param userRecord The user record to insert
     * @return The results of the insert operation
     */
    public DatabaseServiceResults<InsertResult> insertUserRecord(final UserRecordMutate userRecord) {
        LOGGER.debug("Inserting user record: " + userRecord);
        
        final List<String> columnNames = COLUMN_TYPES.stream()
            .map(TableColumnType::columnName)
            .toList();

        final List<String> columnValues = List.of(
            null,
            userRecord.username(),
            userRecord.password(),
            userRecord.email(),
            userRecord.firstName(),
            userRecord.lastName(),
            null,
            null
        );
        
        return dbService.insertRecord(TABLE_NAME, columnNames, columnValues);
    }

    /**
     * Update a user record in the users table. The id of the input record is what will be used to filter the record to update
     * @param userRecord The user record to update
     * @return The results of the update operation
     */
    public DatabaseServiceResults<UpdateResult> updateUserRecord(final UserRecordMutate userRecord) {
        LOGGER.debug("Updating user record: " + userRecord);

        final List<String> columnValues = List.of(
            userRecord.username(),
            userRecord.password(),
            userRecord.email(),
            userRecord.firstName(),
            userRecord.lastName()
        );

        // Filter where id (cast text) equals the input record id
        final WhereFilter userFilter = new WhereFilter(ID_COLUMN + TEXT_CAST, WhereFilter.Operator.EQUAL, Integer.toString(userRecord.id()));

        return dbService.updateRecord(TABLE_NAME, UPDATE_COLUMNS, columnValues, List.of(userFilter));

    }

    /**
     * Delete a user record from the users table
     * @param userRecord The user record to delete
     * @return The results of the delete operation
     */
    public DatabaseServiceResults<DeleteResult> deleteUserRecord(final UserRecordMutate userRecord) {
        LOGGER.debug("Deleting user record: " + userRecord);

        // Filter where id (cast text) equals the input record id
        final WhereFilter userFilter = new WhereFilter(ID_COLUMN + TEXT_CAST, WhereFilter.Operator.EQUAL, Integer.toString(userRecord.id()));

        return dbService.deleteRecord(TABLE_NAME, List.of(userFilter));
    }

    /**
     * Get user record from the table based on criteria
     * @return The results of the select operation
     */
    public DatabaseServiceResults<QueryResult> getUserRecord(final List<WhereFilter> recordFilters) {
        return dbService.queryRecord(TABLE_NAME, List.of(WILDCARD), recordFilters);
    }



}
