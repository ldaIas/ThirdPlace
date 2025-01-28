package com.thirdplace.ThirdPlaceDatabaseService;

public record UserRecordMutate(
    int id,
    String username,
    String password,
    String email,
    String firstName,
    String lastName
) { }