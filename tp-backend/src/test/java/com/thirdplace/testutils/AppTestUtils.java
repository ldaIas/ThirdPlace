package com.thirdplace.testutils;

import org.junit.jupiter.api.Assertions;

import com.thirdplace.utils.result.ErrorCode;
import com.thirdplace.utils.result.Result;

public final class AppTestUtils {

    public final static String EMPTY = "";

    private AppTestUtils() {
    }

    public static <T> T assertOkResult(final Result<T> result) {
        return assertOkResult(result, EMPTY);
    }

    public static <T> T assertOkResult(final Result<T> result, final String additionalMsg) {
        return switch (result) {
            case Result.Ok(final T ok) -> ok;
            case Result.Error(final ErrorCode err) ->
              Assertions.fail(additionalMsg + "\nUnexpected error. Code: " + err.getCode() + ", Message: "
                + err.getMessage());
            case Result.AppException(final ErrorCode err, final var cause) ->
              Assertions.fail(additionalMsg + "\nUnexpected app exception. Code: " + err.getCode() + ", Message: "
                + err.getMessage() + ", Cause: " + cause);
        };
    }

    public static void assertErrorResult(final Result<?> result, final ErrorCode expectedErrorCode) {
        assertErrorResult(result, expectedErrorCode, EMPTY);
    }

    public static void assertErrorResult(final Result<?> result, final ErrorCode expectedErrorCode,
                                         final String additionalMsg) {
        switch (result) {
            case Result.Ok(final var ok) -> Assertions.fail(additionalMsg + "\nUnexpected ok result. Ok: " + ok);
            case Result.Error(final ErrorCode err) -> {
                if (err != expectedErrorCode) {
                    Assertions.fail(additionalMsg + "\nUnexpected error code. Expected: " + expectedErrorCode.getCode() + ", Actual: " + err.getCode());
                }
            }
            case Result.AppException(final ErrorCode err, final var cause) ->
              Assertions.fail(additionalMsg + "\nUnexpected app exception. Code: " + err.getCode() + ", Message: "
                + err.getMessage() + ", Cause: " + cause);
        }
    }

}
