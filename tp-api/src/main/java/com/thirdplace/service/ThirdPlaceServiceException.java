package com.thirdplace.service;

public class ThirdPlaceServiceException extends RuntimeException {
    
    public enum ErrorCode {
        ERROR_WHILE_RUNNING
    }

    private final ErrorCode errorCode;

    public ThirdPlaceServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        if (errorCode == null) {
            throw new IllegalArgumentException("Error code cannot be null");
        }
        this.errorCode = errorCode;
    }

    public ThirdPlaceServiceException(ErrorCode errorCode, String message) {
        super(message);
        if (errorCode == null) {
            throw new IllegalArgumentException("Error code cannot be null");
        }
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "ThirdPlaceServiceExceptions [errorCode=" + errorCode + ", getMessage()=" + getMessage() + "]";
    }
}
