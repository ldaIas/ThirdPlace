package com.thirdplace.endpoints;

public record ErrorResponse(Exception serverError) implements AppResponse {}
