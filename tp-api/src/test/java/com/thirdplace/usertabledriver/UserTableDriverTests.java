package com.thirdplace.usertabledriver;

import com.thirdplace.thirdplacedatabaseservice.ThirdPlaceDatabaseService;
import com.thirdplace.thirdplacedatabaseservice.WhereFilter;
import com.thirdplace.thirdplacedatabaseservice.WhereFilter.Operator;
import com.thirdplace.utils.RecordUtils;
import com.thirdplace.testutils.ThirdPlaceDatabaseServiceTestExt;
import org.junit.jupiter.api.Test;


import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

/**
 * Tests for the {@link UserTableDriver} class
 */
class UserTableDriverTests {

    private static final String TEST_USERNAME = "testUsername";
    private static final String TEST_NAME = "test";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "testpass!23";


    private static UserTableDriver userTableDriver;
    private static ThirdPlaceDatabaseService databaseService;

    @BeforeAll
    static void setUp() {
        // Initialize the database service and user table driver
        databaseService = new ThirdPlaceDatabaseServiceTestExt();
        userTableDriver = new UserTableDriver(databaseService);
        userTableDriver.init();
    }

    @AfterAll
    static void tearDown() throws Exception {
        // Close the database service
        databaseService.close();
    }

    /**
     * Test to ensure that we can insert a user record into the database successfully
     */
    @Test
    void testInsertUser() {

        final UserRecordInsert userRecord = new UserRecordInsert("testUsername", "testpass", "test@example.com", "test", "user");

        final UserRecordResult result = userTableDriver.insertUserRecord(userRecord);

        // Validate the result record
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.id());
        Assertions.assertEquals(userRecord.username(), result.username(), "Expected username to equal that of the insert record");
        Assertions.assertEquals(userRecord.email(), result.email(), "Expected email to equal that of the insert record");
        Assertions.assertEquals(userRecord.firstname(), result.firstname(), "Expected first name to equal that of the insert record");
        Assertions.assertEquals(userRecord.lastname(), result.lastname(), "Expected last name to equal that of the insert record");

        // Valide the created and updated times
        Assertions.assertNotNull(result.createdAt(), "Expected createdAt to be non-null");
        Assertions.assertNotNull(result.updatedAt(), "Expected updatedAt to be non-null");
        Assertions.assertEquals(result.createdAt(), result.updatedAt());

    }

    /**
     * Test to ensure that we can correctly update a user and the updated time is correctly changed
     */
    @Test
    void testUpdateUser() {

        final UserRecordInsert insertUser = createTestUserRecord();

        // Insert initial record
        final UserRecordResult initRec = userTableDriver.insertUserRecord(insertUser);
        final String initRecUpdated = initRec.updatedAt();
        final String initRecCreated = initRec.createdAt();

        final String newUsername = "new username";
        final UserRecordMutate mutateUser = RecordUtils.init(UserRecordMutate.class, Map.of(
            UserRecordMutate.ID_KEY, Integer.parseInt(initRec.id()),
            UserRecordMutate.USERNAME_KEY, newUsername
        ));

        // Update the record
        final UserRecordResult result = userTableDriver.updateUserRecord(mutateUser);

        // Verify it has the new user name and updated date
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newUsername, result.username(), "Expected result to have new username");
        Assertions.assertNotEquals(initRecUpdated, result.updatedAt(), "Expected updated time to be different from initial record");

        // Assert updated at is formatted as a postgres date and we can get the date objects from the record
        final DateTimeFormatter formatter = UserTableDriver.DATE_FORMATTER;
        try {
            formatter.parse(result.updatedAt());
            result.createdDate();
            result.updatedDate();
        } catch (Exception e) {
            Assertions.fail("Expected updatedAt to be formatted as a postgres date. Found: " + result.updatedAt());
        }
        
        Assertions.assertEquals(initRecCreated, result.createdAt(), "Expected created time to be the same as initial record");

    }

    /**
     * Test to ensure when we update a user record, if we don't have an ID on the UserRecordMutation record, we get an error
     */
    @Test
    void testUpdateNoIdRequested() {

        final UserRecordMutate mutateUser = RecordUtils.init(UserRecordMutate.class, Map.of(
            UserRecordMutate.USERNAME_KEY, "test"
        ));

        try {
            userTableDriver.updateUserRecord(mutateUser);
            Assertions.fail("Expected an error to be thrown when updating a user record without an ID");
        } catch (Exception e) {

            if (e instanceof UserTableDriverException userTableDriverException) {
                Assertions.assertEquals(UserTableDriverException.ErrorCode.ERROR_UPDATING_NULL_ID, userTableDriverException.getErrorCode());
            } else {
                Assertions.fail("Expected a UserTableDriverException to be thrown, instead got: " + e);
            }
        
        }

    }

    /**
     * Test to ensure that we can insert and delete a user record successfully
     */
    @Test
    void testDeleteUser() {

        final UserRecordInsert insertUser = createTestUserRecord();
        final UserRecordResult insertRes = userTableDriver.insertUserRecord(insertUser);

        final UserRecordMutate deleteUser = RecordUtils.init(UserRecordMutate.class, Map.of(
            UserRecordMutate.ID_KEY, Integer.parseInt(insertRes.id())
        ));

        boolean deleteResult = userTableDriver.deleteUserRecord(deleteUser);

        Assertions.assertTrue(deleteResult, "Expected delete result to be true");
    }

    /**
     * Test to ensure that if we try to delete a user that doesn't exist, we get a false result
     */
    @Test
    void testDeleteUser_UserDNE() {

        final UserRecordMutate deleteUser = RecordUtils.init(UserRecordMutate.class, Map.of(
            UserRecordMutate.ID_KEY, 999999
        ));

        boolean deleteResult = userTableDriver.deleteUserRecord(deleteUser);

        Assertions.assertFalse(deleteResult, "Expected delete result to be false");
    }

    /**
     * Test to ensure that we can correctly get the user record from the database
     */
    @Test
    public void testGetSingleUser() {

        final UserRecordInsert insertUser = createTestUserRecord();
        final UserRecordResult insertRes = userTableDriver.insertUserRecord(insertUser);

        final List<WhereFilter> recordFilters = List.of(
            new WhereFilter(UserTableDriver.ID_COLUMN, Operator.EQUAL, insertRes.id())
        );
        final List<UserRecordResult> result = userTableDriver.getUserRecord(recordFilters);

        Assertions.assertNotNull(result);
        final UserRecordResult resultUser = result.getFirst();
        Assertions.assertEquals(insertRes.id(), resultUser.id(), "Expected found record to have the same id as the inserted record");
        Assertions.assertEquals(TEST_USERNAME, resultUser.username(), "Expected found record to have same username as inserted record");
    }

    /**
     * Test to ensure that when we try to get a user that doesn't exist, we just get an empty list
     */
    @Test
    public void testGetNonExistentUser() {

        final List<WhereFilter> recordFilters = List.of(
            new WhereFilter(UserTableDriver.USERNAME_COLUMN, Operator.EQUAL, "nonExistentUser")
        );

        final List<UserRecordResult> result = userTableDriver.getUserRecord(recordFilters);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty(), "Expected no records to be found for non-existent user");
    }

    private static UserRecordInsert createTestUserRecord() {
        return RecordUtils.init(UserRecordInsert.class, Map.of(
            UserRecordInsert.USERNAME, TEST_USERNAME,
            UserRecordInsert.PASSWORD, TEST_PASSWORD,
            UserRecordInsert.EMAIL, TEST_EMAIL,
            UserRecordInsert.FIRST_NAME, TEST_NAME
        ));
    }
}
