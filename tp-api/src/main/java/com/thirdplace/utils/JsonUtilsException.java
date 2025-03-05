package com.thirdplace.utils;

public class JsonUtilsException extends RuntimeExceptionBase {

    public enum ErrorCode implements ErrorCodeBase {
        ERROR_WHILE_SERIALIZING,
        ERROR_WHILE_DESERIALIZING
    }

    public JsonUtilsException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public JsonUtilsException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}