package com.thirdplace.usertabledriver;

public record UserRecordMutate(
    int id,
    String username,
    String password,
    String email,
    String firstName,
    String lastName
) { }