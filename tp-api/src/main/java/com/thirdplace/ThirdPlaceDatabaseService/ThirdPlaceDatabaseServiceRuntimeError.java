package com.thirdplace.thirdplacedatabaseservice;

public class ThirdPlaceDatabaseServiceRuntimeError extends RuntimeException {

    public enum ErrorCode {
        ERROR_GETTING_CONNECTION, ERROR_CREATING_TABLE, ERROR_RUNNING_INSERT, ERROR_RUNNING_QUERY, ERROR_RUNNING_UPDATE,
        ERROR_RUNNING_DELETE,

        ERROR_STARTING_DB_SERVER, ERROR_STOPPING_DB_SERVER,

        ERROR_CREATING_DATABASE, ERROR_CHECKING_IF_TABLE_EXISTS,

        ERROR_GETTING_COLUMN_VALUE,

        ERROR_EMPTY_WHERE_CLAUSES
    }

    private final ErrorCode errorCode;

    public ThirdPlaceDatabaseServiceRuntimeError(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        if (errorCode == null) {
            throw new IllegalArgumentException("Error code cannot be null");
        }
        this.errorCode = errorCode;
    }

    public ThirdPlaceDatabaseServiceRuntimeError(ErrorCode errorCode, String message) {
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

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
