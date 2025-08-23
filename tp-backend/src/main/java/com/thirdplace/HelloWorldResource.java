package com.thirdplace;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.fasterxml.jackson.annotation.JsonProperty;

@Path("/api")
public class HelloWorldResource {
    
    public record HelloWorldResponse(@JsonProperty("hello world") boolean helloWorld) 
        implements AppResponse {}
    
    @GET
    @Path("/helloworld")
    public Response getHelloWorld() {
        return Response.ok(new HelloWorldResponse(true))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}