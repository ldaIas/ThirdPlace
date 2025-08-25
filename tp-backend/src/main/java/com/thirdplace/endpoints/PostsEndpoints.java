package com.thirdplace.endpoints;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import com.thirdplace.db.PostsTableManager;
import com.thirdplace.db.schemas.Post;
import com.thirdplace.services.PostsService;
import com.thirdplace.services.PostsService.CreatePostRequest;

import java.util.List;

@Path("")
public class PostsEndpoints {

    public record GetAllPostsResponse(List<Post> posts)
            implements AppResponse {
    }

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