package com.thirdplace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.thirdplace.ThirdPlaceDatabaseService.ThirdPlaceDatabaseService;

public class ThirdPlaceDatabaseServiceTests {
    
    /**
     * Test that verifies that we can start and close out the database service
     */
    @Test
    void testInitializeAndCloseService() {
        try (final ThirdPlaceDatabaseService dbService = new ThirdPlaceDatabaseService()) {
            // If we make it here without an exception, the service was started successfully
        } catch (Exception e) {
            // If an exception is thrown, fail the test
            Assertions.fail("Database service initialization or closure failed", e);
        }
    }
}
