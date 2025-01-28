package com.thirdplace.ThirdPlaceDatabaseService;

public record UserRecord (
    int id,
    String username,
    String password,
    String email,
    String firstName,
    String lastName,
    String createdAt,
    String updatedAt
) { }
