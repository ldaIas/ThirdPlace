// package com.thirdplace.usertabledriver;

// import com.thirdplace.thirdplacedatabaseservice.ThirdPlaceDatabaseService;
// import com.thirdplace.usertabledriver.UserRecordMutate;
// import com.thirdplace.usertabledriver.UserRecordResult;
// import com.thirdplace.usertabledriver.UserTableDriver;
// import com.thirdplace.testutils.ThirdPlaceDatabaseServiceTestExt;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import static org.junit.jupiter.api.Assertions.*;

// public class UserTableDriverTests {

//     private UserTableDriver userTableDriver;
//     private ThirdPlaceDatabaseService databaseService;

//     @BeforeEach
//     public void setUp() {
//         // Initialize the database service and user table driver
//         databaseService = new ThirdPlaceDatabaseServiceTestExt();
//         userTableDriver = new UserTableDriver(databaseService);
//     }

//     @Test
//     public void testInsertUser() {
//         // Arrange
//         UserRecordMutate userRecord = new UserRecordMutate("testUser", "test@example.com");

//         // Act
//         UserRecordResult result = userTableDriver.insertUser(userRecord);

//         // Assert
//         assertNotNull(result);
//         assertTrue(result.isSuccess());
//         assertEquals("testUser", result.getUserName());
//     }

//     @Test
//     public void testUpdateUser() {
//         // Arrange
//         UserRecordMutate userRecord = new UserRecordMutate("testUser", "test@example.com");
//         userTableDriver.insertUser(userRecord);
//         userRecord.setEmail("updated@example.com");

//         // Act
//         UserRecordResult result = userTableDriver.updateUser(userRecord);

//         // Assert
//         assertNotNull(result);
//         assertTrue(result.isSuccess());
//         assertEquals("updated@example.com", result.getEmail());
//     }

//     @Test
//     public void testDeleteUser() {
//         // Arrange
//         UserRecordMutate userRecord = new UserRecordMutate("testUser", "test@example.com");
//         userTableDriver.insertUser(userRecord);

//         // Act
//         boolean deleteResult = userTableDriver.deleteUser(userRecord.getUserName());

//         // Assert
//         assertTrue(deleteResult);
//     }

//     @Test
//     public void testGetUser() {
//         // Arrange
//         UserRecordMutate userRecord = new UserRecordMutate("testUser", "test@example.com");
//         userTableDriver.insertUser(userRecord);

//         // Act
//         UserRecordResult result = userTableDriver.getUser(userRecord.getUserName());

//         // Assert
//         assertNotNull(result);
//         assertEquals("testUser", result.getUserName());
//         assertEquals("test@example.com", result.getEmail());
//     }

//     @Test
//     public void testGetNonExistentUser() {
//         // Act
//         UserRecordResult result = userTableDriver.getUser("nonExistentUser");

//         // Assert
//         assertNull(result);
//     }

//     // Additional tests can be added here for edge cases, error handling, etc.
// }
