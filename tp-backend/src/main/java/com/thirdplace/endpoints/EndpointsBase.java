package com.thirdplace.endpoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdplace.utils.FunctionalUtils.ErrorableSupplier;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class EndpointsBase {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointsBase.class);

    public static Response processRequest(final ErrorableSupplier<AppResponse> method) {
        try {
            final AppResponse response = method.get();

            return Response.ok(response)
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (Exception ex) {

            LOGGER.error("Error processing request", ex);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(ex))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        
    }
}
