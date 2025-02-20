package com.thirdplace.utils;

public class RecordUtilsException extends RuntimeExceptionBase {

    public enum ErrorCode implements ErrorCodeBase {
        BAD_FIELD_TYPE,
        INSTANTIATION_ERROR
    }

    public RecordUtilsException(ErrorCodeBase errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public RecordUtilsException(ErrorCodeBase errorCode, String message) {
        super(errorCode, message);
    }
}
