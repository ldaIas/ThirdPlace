package com.thirdplace.service;

import com.thirdplace.utils.RuntimeExceptionBase;

public class ThirdPlaceServiceException extends RuntimeExceptionBase {
    
    public enum ErrorCode implements ErrorCodeBase {
        ERROR_WHILE_RUNNING
    }

    public ThirdPlaceServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public ThirdPlaceServiceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
