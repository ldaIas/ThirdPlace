package com.thirdplace.usertabledriver;

import java.time.ZonedDateTime;

public record UserRecordResult (
    String id,
    String username,
    String email,
    String firstname,
    String lastname,
    String createdAt,
    String updatedAt
) { 

    /**
     * @return the created date as a ZonedDateTime
     */
    public ZonedDateTime createdDate() {
        return ZonedDateTime.parse(createdAt, UserTableDriver.DATE_FORMATTER);
    }

    /**
     * @return the updated date as a ZonedDateTime
     */
    public ZonedDateTime updatedDate() {
        return ZonedDateTime.parse(updatedAt, UserTableDriver.DATE_FORMATTER);
    }

}
