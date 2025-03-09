package com.thirdplace.thirdplacedatabaseservice;

import com.thirdplace.utils.RuntimeExceptionBase;

public class ThirdPlaceDatabaseServiceRuntimeError extends RuntimeExceptionBase {

    public enum ErrorCode implements ErrorCodeBase {
        ERROR_GETTING_CONNECTION, ERROR_CREATING_TABLE, ERROR_RUNNING_INSERT, ERROR_RUNNING_QUERY, ERROR_RUNNING_UPDATE,
        ERROR_RUNNING_DELETE,

        ERROR_STARTING_DB_SERVER, ERROR_STOPPING_DB_SERVER,

        ERROR_CREATING_DATABASE, ERROR_CHECKING_IF_TABLE_EXISTS,

        ERROR_GETTING_COLUMN_VALUE,

        ERROR_EMPTY_WHERE_CLAUSES
    }

    public ThirdPlaceDatabaseServiceRuntimeError(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public ThirdPlaceDatabaseServiceRuntimeError(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
