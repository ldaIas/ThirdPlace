package com.thirdplace.usertabledriver;

import com.thirdplace.utils.RuntimeExceptionBase;

public class UserTableDriverException extends RuntimeExceptionBase {
    
    public enum ErrorCode implements ErrorCodeBase {
        ERROR_INSERTING_USER,
        ERROR_UPDATING_NULL_ID,
        ERROR_UPDATING_USER,
        ERROR_UPDATING_USER_NOT_FOUND,
        ERROR_DELETING_NULL_ID,
        ERROR_DELETING_USER,
        ERROR_QUERYING_USER,
    }

    public UserTableDriverException(ErrorCodeBase errorCode, String message) {
        super(errorCode, message);
    }

    public UserTableDriverException(ErrorCodeBase errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
