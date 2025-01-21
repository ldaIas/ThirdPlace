package com.thirdplace.utils;

public class JsonUtilsException extends RuntimeException {

    public enum ErrorCode {
        ERROR_WHILE_SERIALIZING,
        ERROR_WHILE_DESERIALIZING
    }

    private final ErrorCode errorCode;

    public JsonUtilsException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        if (errorCode == null) {
            throw new IllegalArgumentException("Error code cannot be null");
        }
        this.errorCode = errorCode;
    }

    public JsonUtilsException(ErrorCode errorCode, String message) {
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