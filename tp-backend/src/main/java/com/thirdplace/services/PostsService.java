package com.thirdplace.services;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.thirdplace.db.PostsTableManager;
import com.thirdplace.db.schemas.Post;
import com.thirdplace.endpoints.AppError;
import com.thirdplace.endpoints.AppRequestBody;
import com.thirdplace.endpoints.AppResponse;
import com.thirdplace.utils.result.Result;

public final class PostsService {

    public enum PostsErrorCode implements AppError {
        DATABASE_ERROR("Database operation failed");

        private final String message;

        PostsErrorCode(final String message) {
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

    private PostsService() {
    }

    /**
     * Creates a new post. Inserts the post to the database.
     *
     * @param post The post to create.
     * @return The created post.
     */
    public static Result<CreatePostResponse> createPost(final CreatePostRequest post) {
        try {

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
            return Result.ok(new CreatePostResponse(formattedPost));
        } catch (final SQLException e) {
            return Result.appException(PostsErrorCode.DATABASE_ERROR, e);
        }
    }

    public record GetAllPostsResponse(List<FormattedPostResponse> posts)
      implements AppResponse {
    }

    public static Result<GetAllPostsResponse> getAllPosts() {
        try {
            final List<Post> posts = PostsTableManager.getInstance().fetchAll();
            final List<FormattedPostResponse> formattedPosts = posts.stream()
              .map(post -> new FormattedPostResponse(
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
                post.category())).toList();
            return Result.ok(new GetAllPostsResponse(formattedPosts));
        } catch (final SQLException e) {
            return Result.appException(PostsErrorCode.DATABASE_ERROR, e);
        }
    }
}
