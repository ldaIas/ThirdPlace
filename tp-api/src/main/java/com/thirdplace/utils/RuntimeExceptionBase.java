package com.thirdplace.utils;

public class RuntimeExceptionBase extends RuntimeException{
    
    public interface ErrorCodeBase {

    }

    public ErrorCodeBase getErrorCode() {
        return errorCode;
    }

    private final ErrorCodeBase errorCode;

    public RuntimeExceptionBase(ErrorCodeBase errorCode, String message, Throwable cause) {
        super(message, cause);
        if (errorCode == null) {
            throw new IllegalArgumentException("Error code cannot be null");
        }
        this.errorCode = errorCode;
    }

    public RuntimeExceptionBase(ErrorCodeBase errorCode, String message) {
        super(message);
        if (errorCode == null) {
            throw new IllegalArgumentException("Error code cannot be null");
        }
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + "[errorCode=" + errorCode + ", getMessage()=" + getMessage() + "]";
    }
}
