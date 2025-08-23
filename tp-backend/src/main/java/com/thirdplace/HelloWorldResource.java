package com.thirdplace;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thirdplace.schemas.Post;
import java.util.List;

@Path("")
public class HelloWorldResource {

    public record HelloWorldResponse(@JsonProperty("hello world") boolean helloWorld)
            implements AppResponse {
    }

    @GET
    @Path("api/helloworld")
    public Response getHelloWorld() {
        return Response.ok(new HelloWorldResponse(true))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("api/samplepost")
    public Response getSamplePost() {
        return Response.ok(new Post(UUID.randomUUID().toString(), "Sample Post",
                "Sample Author", "some description", Instant.now(), Instant.now(), 2,
                List.of("tag1", "tag2"), "some location", "some geohash", 0.0, 0.0, Instant.now(),
                false, "active", "any", "any"))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}