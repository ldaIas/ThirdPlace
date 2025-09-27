package com.thirdplace.services;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.thirdplace.db.PostsTableManager;
import com.thirdplace.db.RSVPsTableManager;
import com.thirdplace.db.WhereFilter;
import com.thirdplace.db.schemas.Post;
import com.thirdplace.db.schemas.RSVP;
import com.thirdplace.endpoints.AppError;
import com.thirdplace.endpoints.AppRequestBody;
import com.thirdplace.endpoints.AppResponse;
import com.thirdplace.utils.result.ErrorCode;
import com.thirdplace.utils.result.Result;

public final class RSVPsService {

    private RSVPsService() {
    }

    public enum RSVPsErrorCode implements AppError {
        DATABASE_ERROR("Database operation failed"),
        POST_NOT_FOUND("Post not found"),
        RSVP_NOT_FOUND("RSVP not found"),
        CANNOT_RSVP_OWN_POST("Cannot RSVP to your own post"),
        ALREADY_RSVPED("User has already RSVPed to this post"),
        INVALID_STATUS("Invalid status. Must be PENDING, ACCEPTED, or DECLINED"),
        GROUP_SIZE_LIMIT_REACHED("Cannot accept more RSVPs - group size limit reached"),
        INVALID_RESPONSE("Invalid response. Must be ACCEPT or DECLINE");

        private final String message;

        private RSVPsErrorCode(final String message) {
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
    
    public record FormattedRSVPResponse(
            String id,
            String userId,
            String postId,
            String status,
            String createdAt) {
    }

    public record CreateRSVPRequest(
            String userId,
            String postId) implements AppRequestBody {
    }

    public record CreateRSVPResponse(FormattedRSVPResponse createdRSVP)
            implements AppResponse {
    }

    public record UpdateRSVPStatusRequest(
            String rsvpId,
            String status) implements AppRequestBody {
    }

    public record RespondToRSVPRequest(
            String rsvpId,
            String response) implements AppRequestBody {
    }

    public record UpdateRSVPStatusResponse(FormattedRSVPResponse updatedRSVP)
            implements AppResponse {
    }

    public record GetRSVPsByPostResponse(List<FormattedRSVPResponse> rsvps)
            implements AppResponse {
    }

    public record GetRSVPsByUserResponse(List<FormattedRSVPResponse> rsvps)
            implements AppResponse {
    }

    public record DeleteRSVPResponse(boolean success)
            implements AppResponse {
    }

    public record RespondToRSVPResponse(String action, FormattedRSVPResponse rsvp)
            implements AppResponse {
    }

    public static Result<CreateRSVPResponse> createRSVP(final CreateRSVPRequest request) {
        try {
            // Check if post exists and get post details
            final Post post = PostsTableManager.getInstance().fetchById(request.postId())
                    .orElse(null);
            if (post == null) {
                return Result.error(RSVPsErrorCode.POST_NOT_FOUND);
            }

            // App rule: User cannot RSVP to their own post
            if (post.author().equals(request.userId())) {
                return Result.error(RSVPsErrorCode.CANNOT_RSVP_OWN_POST);
            }

            // App rule: User cannot RSVP more than once to the same post
            List<WhereFilter> existingRSVPFilters = List.of(
                    new WhereFilter(RSVP.RSVPFieldReference.USER_ID, WhereFilter.FilterOperator.EQUALS, request.userId()),
                    new WhereFilter(RSVP.RSVPFieldReference.POST_ID, WhereFilter.FilterOperator.EQUALS, request.postId()));
            
            List<RSVP> existingRSVPs = RSVPsTableManager.getInstance().fetchByFilter(existingRSVPFilters);
            if (!existingRSVPs.isEmpty()) {
                return Result.error(RSVPsErrorCode.ALREADY_RSVPED);
            }

            final RSVP rsvpToInsert = new RSVP(
                    UUID.randomUUID().toString(),
                    request.userId(),
                    request.postId(),
                    "PENDING",
                    Instant.now());

            RSVPsTableManager.getInstance().insert(rsvpToInsert);

            final FormattedRSVPResponse formattedRSVP = new FormattedRSVPResponse(
                    rsvpToInsert.id(),
                    rsvpToInsert.userId(),
                    rsvpToInsert.postId(),
                    rsvpToInsert.status(),
                    rsvpToInsert.createdAt().toString());

            return Result.ok(new CreateRSVPResponse(formattedRSVP));
        } catch (final SQLException e) {
            return Result.appException(RSVPsErrorCode.DATABASE_ERROR, e);
        }
    }

    public static Result<GetRSVPsByPostResponse> getRSVPsByPost(final String postId) {
        try {
            List<WhereFilter> filters = List.of(
                    new WhereFilter(RSVP.RSVPFieldReference.POST_ID, WhereFilter.FilterOperator.EQUALS, postId));
            
            final List<RSVP> rsvps = RSVPsTableManager.getInstance().fetchByFilter(filters);
            final List<FormattedRSVPResponse> formattedRSVPs = rsvps.stream().map(rsvp -> 
                    new FormattedRSVPResponse(
                            rsvp.id(),
                            rsvp.userId(),
                            rsvp.postId(),
                            rsvp.status(),
                            rsvp.createdAt().toString())
            ).toList();

            return Result.ok(new GetRSVPsByPostResponse(formattedRSVPs));
        } catch (final SQLException e) {
            return Result.appException(RSVPsErrorCode.DATABASE_ERROR, e);
        }
    }

    public static Result<GetRSVPsByUserResponse> getRSVPsByUser(final String userId) {
        try {
            List<WhereFilter> filters = List.of(
                    new WhereFilter(RSVP.RSVPFieldReference.USER_ID, WhereFilter.FilterOperator.EQUALS, userId));
            
            final List<RSVP> rsvps = RSVPsTableManager.getInstance().fetchByFilter(filters);
            final List<FormattedRSVPResponse> formattedRSVPs = rsvps.stream().map(rsvp -> 
                    new FormattedRSVPResponse(
                            rsvp.id(),
                            rsvp.userId(),
                            rsvp.postId(),
                            rsvp.status(),
                            rsvp.createdAt().toString())
            ).toList();

            return Result.ok(new GetRSVPsByUserResponse(formattedRSVPs));
        } catch (final SQLException e) {
            return Result.appException(RSVPsErrorCode.DATABASE_ERROR, e);
        }
    }

    public static Result<UpdateRSVPStatusResponse> updateRSVPStatus(final UpdateRSVPStatusRequest request) {
        try {
            // Validate status
            if (!List.of("PENDING", "ACCEPTED", "DECLINED").contains(request.status())) {
                return Result.error(RSVPsErrorCode.INVALID_STATUS);
            }

            final RSVP existingRSVP = RSVPsTableManager.getInstance().fetchById(request.rsvpId())
                    .orElse(null);
            if (existingRSVP == null) {
                return Result.error(RSVPsErrorCode.RSVP_NOT_FOUND);
            }

            // If accepting, check group size limit
            if ("ACCEPTED".equals(request.status())) {
                final Post post = PostsTableManager.getInstance().fetchById(existingRSVP.postId())
                        .orElse(null);
                if (post == null) {
                    return Result.error(RSVPsErrorCode.POST_NOT_FOUND);
                }

                // Count current accepted RSVPs
                final List<WhereFilter> acceptedFilters = List.of(
                        new WhereFilter(RSVP.RSVPFieldReference.POST_ID, WhereFilter.FilterOperator.EQUALS, existingRSVP.postId()),
                        new WhereFilter(RSVP.RSVPFieldReference.STATUS, WhereFilter.FilterOperator.EQUALS, "ACCEPTED"));
                
                final List<RSVP> acceptedRSVPs = RSVPsTableManager.getInstance().fetchByFilter(acceptedFilters);
                
                if (acceptedRSVPs.size() >= post.groupSize()) {
                    return Result.error(RSVPsErrorCode.GROUP_SIZE_LIMIT_REACHED);
                }
            }

            final RSVP updatedRSVP = new RSVP(
                    existingRSVP.id(),
                    existingRSVP.userId(),
                    existingRSVP.postId(),
                    request.status(),
                    existingRSVP.createdAt());

            RSVPsTableManager.getInstance().update(updatedRSVP);

            final FormattedRSVPResponse formattedRSVP = new FormattedRSVPResponse(
                    updatedRSVP.id(),
                    updatedRSVP.userId(),
                    updatedRSVP.postId(),
                    updatedRSVP.status(),
                    updatedRSVP.createdAt().toString());

            return Result.ok(new UpdateRSVPStatusResponse(formattedRSVP));
        } catch (final SQLException e) {
            return Result.appException(RSVPsErrorCode.DATABASE_ERROR, e);
        }
    }

    public static Result<DeleteRSVPResponse> deleteRSVP(final String rsvpId) {
        try {
            boolean success = RSVPsTableManager.getInstance().delete(rsvpId);
            return Result.ok(new DeleteRSVPResponse(success));
        } catch (final SQLException e) {
            return Result.appException(RSVPsErrorCode.DATABASE_ERROR, e);
        }
    }

    public static Result<RespondToRSVPResponse> respondToRSVP(final RespondToRSVPRequest request) {
        try {
            // Validate response
            if (!List.of("ACCEPT", "DECLINE").contains(request.response())) {
                return Result.error(RSVPsErrorCode.INVALID_RESPONSE);
            }

            final RSVP existingRSVP = RSVPsTableManager.getInstance().fetchById(request.rsvpId())
                    .orElse(null);
            if (existingRSVP == null) {
                return Result.error(RSVPsErrorCode.RSVP_NOT_FOUND);
            }

            if ("DECLINE".equals(request.response())) {
                // Delete the RSVP
                RSVPsTableManager.getInstance().delete(request.rsvpId());
                return Result.ok(new RespondToRSVPResponse("DECLINED", null));
            } else {
                // Accept the RSVP - check group size limit first
                final Post post = PostsTableManager.getInstance().fetchById(existingRSVP.postId())
                        .orElse(null);
                if (post == null) {
                    return Result.error(RSVPsErrorCode.POST_NOT_FOUND);
                }

                // Count current accepted RSVPs
                final List<WhereFilter> acceptedFilters = List.of(
                        new WhereFilter(RSVP.RSVPFieldReference.POST_ID, WhereFilter.FilterOperator.EQUALS, existingRSVP.postId()),
                        new WhereFilter(RSVP.RSVPFieldReference.STATUS, WhereFilter.FilterOperator.EQUALS, "ACCEPTED"));
                
                final List<RSVP> acceptedRSVPs = RSVPsTableManager.getInstance().fetchByFilter(acceptedFilters);
                
                if (acceptedRSVPs.size() >= post.groupSize()) {
                    return Result.error(RSVPsErrorCode.GROUP_SIZE_LIMIT_REACHED);
                }

                // Update status to ACCEPTED
                final RSVP updatedRSVP = new RSVP(
                        existingRSVP.id(),
                        existingRSVP.userId(),
                        existingRSVP.postId(),
                        "ACCEPTED",
                        existingRSVP.createdAt());

                RSVPsTableManager.getInstance().update(updatedRSVP);

                final FormattedRSVPResponse formattedRSVP = new FormattedRSVPResponse(
                        updatedRSVP.id(),
                        updatedRSVP.userId(),
                        updatedRSVP.postId(),
                        updatedRSVP.status(),
                        updatedRSVP.createdAt().toString());

                return Result.ok(new RespondToRSVPResponse("ACCEPTED", formattedRSVP));
            }
        } catch (final SQLException e) {
            return Result.appException(RSVPsErrorCode.DATABASE_ERROR, e);
        }
    }
}