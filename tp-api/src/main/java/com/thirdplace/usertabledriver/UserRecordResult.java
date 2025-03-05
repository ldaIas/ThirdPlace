package com.thirdplace.usertabledriver;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public record UserRecordResult (
    String id,
    String username,
    String email,
    String firstName,
    String lastName,
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
