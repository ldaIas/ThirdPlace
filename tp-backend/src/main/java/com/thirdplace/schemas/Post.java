package com.thirdplace.schemas;

import com.thirdplace.db.TableSchema;

import java.time.Instant;
import java.util.List;

public record Post(
    String id,
    String title,
    String author,
    String description,
    Instant createdAt,
    Instant endDate,
    int groupSize,
    List<String> tags,
    String location,
    String geohash,
    double latitude,
    double longitude,
    Instant proposedTime,
    boolean isDateActivity,
    String status,
    String genderBalance,
    String category
) implements TableSchema {}