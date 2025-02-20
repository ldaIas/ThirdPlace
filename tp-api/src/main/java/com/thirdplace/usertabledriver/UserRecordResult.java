package com.thirdplace.usertabledriver;

public record UserRecordResult (
    int id,
    String username,
    String password,
    String email,
    String firstName,
    String lastName,
    String createdAt,
    String updatedAt
) { }
