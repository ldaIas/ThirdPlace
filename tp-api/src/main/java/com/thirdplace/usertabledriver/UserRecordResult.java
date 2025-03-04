package com.thirdplace.usertabledriver;

public record UserRecordResult (
    String id,
    String username,
    String email,
    String firstName,
    String lastName,
    String createdAt,
    String updatedAt
) { }
