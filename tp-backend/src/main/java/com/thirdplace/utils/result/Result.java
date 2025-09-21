package com.thirdplace.utils.result;

public sealed interface Result<T> permits Result.Ok, Result.Error, Result.AppException {

    /**
     * This Ok type represents successful results.
     * In an endpoint response, if a method returns an Ok, the response status code will be 200.
     *
     */
    record Ok<T>(T value) implements Result<T> {
    }

    /**
     * This Error type represents errors that arise due to input or validation issues.
     * These are caused by application logic running into something unexpected.
     * These errors are usually presented to the user to explain that they did something wrong.
     * In an endpoint response, if a method returns an Error, the response status code will be 400.
     * 
     */
    record Error<T>(ErrorCode errorCode) implements Result<T> {
    }

    /**
     * This Error type represents errors that arise due to application logic running into something unexpected,
     * usually a deeper method throwing an exception, such as an IllegalAccessException or IOException.
     * These are caused by a true problem in the application logic and usually indicate a bug.
     * These errors are not usually presented to the user, instead we say the system encountered an unexpected problem.
     * In an endpoint response, if a method returns an AppException, the response status code will be 500.
     *
     */
    record AppException<T>(ErrorCode errorCode, Throwable cause) implements Result<T> {
    }

    static <T> Result<T> ok(final T value) {
        return new Ok<>(value);
    }

    static <T> Result<T> error(final ErrorCode error) {
        return new Error<>(error);
    }

    static <T> Result<T> appException(final ErrorCode error, final Throwable cause) {
        return new AppException<>(error, cause);
    }
}