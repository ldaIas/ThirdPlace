package com.thirdplace.ThirdPlaceDatabaseService;

public class ThirdPlaceDatabaseServiceException extends RuntimeException {
    
    public enum ErrorCode {
        ERROR_GETTING_CONNECTION,
        ERROR_CREATING_TABLE,
        ERROR_RUNNING_INSERT,
        ERROR_RUNNING_QUERY,
        ERROR_RUNNING_UPDATE,
        ERROR_RUNNING_DELETE,

        ERROR_STARTING_DB_SERVER,
        ERROR_STOPPING_DB_SERVER
    }

    
    private final ErrorCode errorCode;

    public ThirdPlaceDatabaseServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        if (errorCode == null) {
            throw new IllegalArgumentException("Error code cannot be null");
        }
        this.errorCode = errorCode;
    }

    public ThirdPlaceDatabaseServiceException(ErrorCode errorCode, String message) {
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
