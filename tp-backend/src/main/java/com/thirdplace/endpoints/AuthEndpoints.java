package com.thirdplace.endpoints;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Path("")
public class AuthEndpoints {

    public record SignedRequestResponse(String signedRequest) implements AppResponse {}

    @GET
    @Path("api/auth/signedRequest")
    public Response getSignedRequest() {

        return EndpointsBase.processRequest(() -> {
            try {
                String signedRequest = Files.readString(Paths.get(".secrets/frequency_provider_signedRequests.b64"));
                return new SignedRequestResponse(signedRequest.trim());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read signedRequest file", e);
            }
        });
        
    }
}