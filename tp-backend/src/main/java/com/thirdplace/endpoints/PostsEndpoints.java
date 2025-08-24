package com.thirdplace.endpoints;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.UUID;

import com.thirdplace.AppResponse;
import com.thirdplace.db.PostsTableManager;
import com.thirdplace.schemas.Post;
import java.util.List;

@Path("")
public class PostsEndpoints {

    public record CreatePostResponse(Post createdPost)
            implements AppResponse {
    }

    public record GetAllPostsResponse(List<Post> posts)
            implements AppResponse {
    }

    @GET
    @Path("api/Posts:create")
    public Response createPost() {
        Post samplePost = new Post(UUID.randomUUID().toString(), "Sample Post",
                "Sample Author", "some description", Instant.now(), Instant.now(), 2,
                new String[] {"tag1", "tag2"}, "some location", 0.0, 0.0, Instant.now(),
                false, "active", "any", "any");

        return EndpointsBase.processRequest(() -> {
            PostsTableManager.getInstance().insert(samplePost);
            return new CreatePostResponse(samplePost);
        });
    }

    @GET
    @Path("api/Posts:getAll")
    public Response getAllPosts() {
        return EndpointsBase.processRequest(() -> {
            List<Post> posts = PostsTableManager.getInstance().findAll();
            return new GetAllPostsResponse(posts);
        });
    }
}