package com.thirdplace.usertabledriver;

import com.thirdplace.utils.RuntimeExceptionBase;

public class UserTableDriverException extends RuntimeExceptionBase {
    
    public enum ErrorCode implements ErrorCodeBase {
        ERROR_INSERTING_USER
    }

    public UserTableDriverException(ErrorCodeBase errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
