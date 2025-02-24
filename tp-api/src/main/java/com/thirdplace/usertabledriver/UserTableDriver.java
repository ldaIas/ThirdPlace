package com.thirdplace.usertabledriver;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdplace.thirdplacedatabaseservice.ColumnSetter;
import com.thirdplace.thirdplacedatabaseservice.DatabaseServiceResults;
import com.thirdplace.thirdplacedatabaseservice.TableColumnType;
import com.thirdplace.thirdplacedatabaseservice.ThirdPlaceDatabaseService;
import com.thirdplace.thirdplacedatabaseservice.WhereFilter;
import com.thirdplace.thirdplacedatabaseservice.DatabaseServiceResults.DeleteResult;
import com.thirdplace.thirdplacedatabaseservice.DatabaseServiceResults.InsertResult;
import com.thirdplace.thirdplacedatabaseservice.DatabaseServiceResults.QueryResult;
import com.thirdplace.thirdplacedatabaseservice.DatabaseServiceResults.UpdateResult;

import jakarta.annotation.Nullable;

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
        // Serial ID - auto increments integer
        new TableColumnType(ID_COLUMN, "INTEGER PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY UNIQUE"),
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

    /**
     * Initializes the user table, or does nothing if the table already exists
     */
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
    public UserRecordResult insertUserRecord(final UserRecordInsert userRecord) {
        LOGGER.debug("Inserting user record: " + userRecord);

        final List<String> columnValues = getNullSafeValues(
            userRecord.username(),
            userRecord.password(),
            userRecord.email(),
            userRecord.firstName(),
            userRecord.lastName()
        );

        // Insert using the update columns, as the other ones are automatically generated by the db
        final DatabaseServiceResults<InsertResult> insertResult = dbService.insertRecord(TABLE_NAME, UPDATE_COLUMNS, columnValues);

        // If the db threw an exception, throw it back up
        if (!insertResult.successful()) {
            throw new UserTableDriverException(UserTableDriverException.ErrorCode.ERROR_INSERTING_USER, 
                "Error trying to insert user record " + userRecord, insertResult.exception());
        }

        final Map<String, Object> result = insertResult.result().inserted();
        
        return new UserRecordResult(
            result.get(ID_COLUMN).toString(),
            result.get(USERNAME_COLUMN).toString(),
            result.get(EMAIL_COLUMN).toString(),
            result.get(FIRST_NAME_COLUMN).toString(),
            result.get(LAST_NAME_COLUMN).toString(),
            result.get(CREATED_AT_COLUMN).toString(),
            result.get(UPDATED_AT_COLUMN).toString()
        );
    }

    /**
     * Get null safe values for the given values. If the value is null, an empty string is returned
     * @param values A list of values to get null safe values for
     * @return A list of strings, none null
     */
    private List<String> getNullSafeValues(String... values) {
    return Stream.of(values)
        .map(value -> value == null ? StringUtils.EMPTY : value)
        .collect(Collectors.toList());
    }


    /**
     * Update a user record in the users table. The id of the input record is what will be used to filter the record to update
     * @param userRecord The user record to update
     * @param returnUpdated Whether to return the updated record
     * @return The resulting record of the update operation. Null if returnUpdated is false
     */
    @Nullable
    public UserRecordResult updateUserRecord(final UserRecordMutate userRecord, final boolean returnUpdated) {
        LOGGER.debug("Updating user record: " + userRecord);

        final List<String> columnValues = getNullSafeValues(
            userRecord.username(),
            userRecord.password(),
            userRecord.email(),
            userRecord.firstName(),
            userRecord.lastName()
        );

        final List<ColumnSetter> columnSetters = IntStream.range(0, UPDATE_COLUMNS.size())
            .mapToObj(i -> new ColumnSetter(UPDATE_COLUMNS.get(i), columnValues.get(i)))
            .toList();

        // Filter where id (cast text) equals the input record id
        final WhereFilter userFilter = new WhereFilter(ID_COLUMN + TEXT_CAST, WhereFilter.Operator.EQUAL, Integer.toString(userRecord.id()));

        final DatabaseServiceResults<UpdateResult> dbUpdateResult = dbService.updateRecord(TABLE_NAME, columnSetters, List.of(userFilter), returnUpdated);

        if (returnUpdated) {
            final Map<String, Object> updatedUserRecord = dbUpdateResult.result().updated().getFirst();
            return new UserRecordResult(
                updatedUserRecord.get(ID_COLUMN).toString(),
                updatedUserRecord.get(USERNAME_COLUMN).toString(),
                updatedUserRecord.get(EMAIL_COLUMN).toString(),
                updatedUserRecord.get(FIRST_NAME_COLUMN).toString(),
                updatedUserRecord.get(LAST_NAME_COLUMN).toString(),
                updatedUserRecord.get(CREATED_AT_COLUMN).toString(),
                updatedUserRecord.get(UPDATED_AT_COLUMN).toString()
            );
        }

        return null;
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
