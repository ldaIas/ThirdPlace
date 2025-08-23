package com.thirdplace.endpoints;

import com.thirdplace.AppResponse;

public record ErrorResponse(Exception serverError) implements AppResponse {}
