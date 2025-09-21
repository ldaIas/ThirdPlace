package com.thirdplace.endpoints;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import com.thirdplace.services.PostsService;
import com.thirdplace.services.PostsService.CreatePostRequest;

@Path("")
public class PostsEndpoints {

    @POST
    @Path("api/Posts:create")
    public Response createPost(final CreatePostRequest post) {

        return EndpointsBase.processRequest(() -> {
            return PostsService.createPost(post);
        });
    }

    @GET
    @Path("api/Posts:getAll")
    public Response getAllPosts() {
        return EndpointsBase.processRequest(() -> {
            return PostsService.getAllPosts();
        });
    }
}