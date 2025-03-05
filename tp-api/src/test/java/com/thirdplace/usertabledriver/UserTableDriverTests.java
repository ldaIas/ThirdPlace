package com.thirdplace.usertabledriver;

import com.thirdplace.thirdplacedatabaseservice.ThirdPlaceDatabaseService;
import com.thirdplace.utils.RecordUtils;
import com.thirdplace.testutils.ThirdPlaceDatabaseServiceTestExt;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

class UserTableDriverTests {

    private static final String TEST_USERNAME = "testUsername";
    private static final String TEST_NAME = "test";
    private static final String TEST_LAST = "user";
    private static final String TEST_EMAIL = "test@example.com";


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
        Assertions.assertEquals(userRecord.firstName(), result.firstName(), "Expected first name to equal that of the insert record");
        Assertions.assertEquals(userRecord.lastName(), result.lastName(), "Expected last name to equal that of the insert record");

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

        final Class<UserRecordInsert> recordClass = UserRecordInsert.class;
        final UserRecordInsert insertUser = RecordUtils.init(recordClass, Map.of(
            UserRecordInsert.USERNAME, TEST_USERNAME,
            UserRecordInsert.PASSWORD, "testpass!23",
            UserRecordInsert.EMAIL, TEST_EMAIL,
            UserRecordInsert.FIRST_NAME, TEST_NAME + "testUpdateUser()"
        ));

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

    }

    // @Test
    // public void testDeleteUser() {
    //     // Arrange
    //     UserRecordMutate userRecord = new UserRecordMutate("testUser", "test@example.com");
    //     userTableDriver.insertUser(userRecord);

    //     // Act
    //     boolean deleteResult = userTableDriver.deleteUser(userRecord.getUserName());

    //     // Assert
    //     assertTrue(deleteResult);
    // }

    // @Test
    // public void testGetUser() {
    //     // Arrange
    //     UserRecordMutate userRecord = new UserRecordMutate("testUser", "test@example.com");
    //     userTableDriver.insertUser(userRecord);

    //     // Act
    //     UserRecordResult result = userTableDriver.getUser(userRecord.getUserName());

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals("testUser", result.getUserName());
    //     assertEquals("test@example.com", result.getEmail());
    // }

    // @Test
    // public void testGetNonExistentUser() {
    //     // Act
    //     UserRecordResult result = userTableDriver.getUser("nonExistentUser");

    //     // Assert
    //     assertNull(result);
    // }

    // Additional tests can be added here for edge cases, error handling, etc.
}
