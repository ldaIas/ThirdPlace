package com.thirdplace.endpoints;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdplace.utils.result.Result;
import com.thirdplace.utils.result.Result.Error;
import com.thirdplace.utils.result.Result.AppException;
import com.thirdplace.utils.result.Result.Ok;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public final class EndpointsBase {

    private EndpointsBase() {

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointsBase.class);

    public static <T extends AppResponse> Response processRequest(final Supplier<Result<T>> method) {
        try {
            final Result<T> response = method.get();

            return switch (response) {
                case Ok(var value) -> Response.ok(value)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
                case Error(var error) -> Response.status(Response.Status.BAD_REQUEST)
                        .entity(error)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
                case AppException(var error, var cause) -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(error)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            };

        } catch (final RuntimeException ex) {

            LOGGER.error("Error processing request", ex);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(ex))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

    }
}
