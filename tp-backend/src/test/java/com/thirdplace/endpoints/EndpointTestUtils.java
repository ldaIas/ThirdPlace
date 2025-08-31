package com.thirdplace.endpoints;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class EndpointTestUtils {

    public static void assertResponseStatus(final Response response, final Status status) {
        assertNotNull(response, "Response should not be null");
        if (status.getStatusCode() != response.getStatus()) {
            final String body = response.readEntity(String.class);
            fail(String.format("Expected status %d but got %d. Response body:",
                    status.getStatusCode(), response.getStatus(), body));
        }

    }
}
