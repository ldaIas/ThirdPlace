package com.thirdplace.endpoints;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.thirdplace.utils.result.ErrorCode;
import com.thirdplace.utils.result.Result;

@Path("")
public class AuthEndpoints {

    public record SignedRequestResponse(String signedRequest) implements AppResponse {
    }

    public enum AuthErrorCode implements ErrorCode {
        SIG_FILE_READ_ERROR("Failed to read signedRequest file");

        private final String message;

        private AuthErrorCode(final String message) {
            this.message = message;
        }

        @Override
        public String getCode() {
            return this.name();
        }

        @Override
        public String getMessage() {
            return this.message;
        }
    }

    @GET
    @Path("api/auth/signedRequest")
    public Response getSignedRequest() {

        return EndpointsBase.processRequest(() -> {
            try {
                final String signedRequest = Files
                        .readString(Paths.get(".secrets/frequency_provider_signedRequests.b64"));
                return Result.ok(new SignedRequestResponse(signedRequest.trim()));

            } catch (final IOException e) {
                return Result.appException(AuthErrorCode.SIG_FILE_READ_ERROR, e);
            }
        });

    }
}