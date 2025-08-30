package com.thirdplace.services;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.thirdplace.db.PostsTableManager;
import com.thirdplace.db.schemas.Post;
import com.thirdplace.endpoints.AppRequestBody;
import com.thirdplace.endpoints.AppResponse;

public class PostsService {
        public record FormattedPostResponse(
                        String id,
                        String title,
                        String author,
                        String description,
                        String createdAt,
                        String endDate,
                        int groupSize,
                        String[] tags,
                        String location,
                        double latitude,
                        double longitude,
                        String proposedTime,
                        boolean isDateActivity,
                        String status,
                        String genderBalance,
                        String category) {
        }

        public record CreatePostResponse(FormattedPostResponse createdPost)
                        implements AppResponse {
        }

        public record CreatePostRequest(
                        String title,
                        String author,
                        String description,
                        String endDate,
                        int groupSize,
                        String[] tags,
                        String location,
                        double latitude,
                        double longitude,
                        String proposedTime,
                        String genderBalance,
                        String category) implements AppRequestBody {
        }

        /**
         * Creates a new post. Inserts the post to the database.
         * 
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
                                Instant.parse(post.endDate()),
                                post.groupSize(),
                                post.tags(),
                                post.location(),
                                post.latitude(),
                                post.longitude(),
                                Instant.parse(post.proposedTime()),
                                false,
                                "active",
                                post.genderBalance(),
                                post.category());
                PostsTableManager.getInstance().insert(postToInsert);

                final FormattedPostResponse formattedPost = new FormattedPostResponse(
                                postToInsert.id(),
                                postToInsert.title(),
                                postToInsert.author(),
                                postToInsert.description(),
                                postToInsert.createdAt().toString(),
                                postToInsert.endDate().toString(),
                                postToInsert.groupSize(),
                                postToInsert.tags(),
                                postToInsert.location(),
                                postToInsert.latitude(),
                                postToInsert.longitude(),
                                postToInsert.proposedTime().toString(),
                                postToInsert.isDateActivity(),
                                postToInsert.status(),
                                postToInsert.genderBalance(),
                                postToInsert.category());
                return new CreatePostResponse(formattedPost);

        }

        public record GetAllPostsResponse(List<FormattedPostResponse> posts)
                        implements AppResponse {
        }

        public static GetAllPostsResponse getAllPosts() throws SQLException {
                final List<Post> posts = PostsTableManager.getInstance().fetchAll();
                final List<FormattedPostResponse> formattedPosts = posts.stream().map(post -> {
                        return new FormattedPostResponse(
                                        post.id(),
                                        post.title(),
                                        post.author(),
                                        post.description(),
                                        post.createdAt().toString(),
                                        post.endDate().toString(),
                                        post.groupSize(),
                                        post.tags(),
                                        post.location(),
                                        post.latitude(),
                                        post.longitude(),
                                        post.proposedTime().toString(),
                                        post.isDateActivity(),
                                        post.status(),
                                        post.genderBalance(),
                                        post.category());
                }).toList();
                return new GetAllPostsResponse(formattedPosts);
        }
}
