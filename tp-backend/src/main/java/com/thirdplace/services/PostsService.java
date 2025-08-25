package com.thirdplace.services;

import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import com.thirdplace.db.PostsTableManager;
import com.thirdplace.db.schemas.Post;
import com.thirdplace.endpoints.AppRequestBody;
import com.thirdplace.endpoints.AppResponse;


public class PostsService {
    
    public record CreatePostResponse(Post createdPost)
            implements AppResponse {
    }

    public record CreatePostRequest(
        String title,
        String author,
        String description,
        Instant endDate,
        int groupSize,
        String[] tags,
        String location,
        double latitude,
        double longitude,
        Instant proposedTime,
        String genderBalance,
        String category
    ) implements AppRequestBody {}

    /**
     * Creates a new post. Inserts the post to the database.
     * @param post The post to create.
     * @return The created post.
     */
    public static CreatePostResponse createPost(final CreatePostRequest post) throws SQLException {

        final Post postToInsert = new Post(
            UUID.randomUUID().toString(),
            post.title(),
            post.author(),
            post.description(),
            Instant.now(),
            post.endDate(),
            post.groupSize(),
            post.tags(),
            post.location(),
            post.latitude(),
            post.longitude(),
            post.proposedTime(),
            false,
            "active",
            post.genderBalance(),
            post.category()
        );
        PostsTableManager.getInstance().insert(postToInsert);

        return new CreatePostResponse(postToInsert);

    }

}
