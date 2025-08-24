package com.thirdplace.schemas;

import com.thirdplace.db.TableField;
import com.thirdplace.db.TableFieldModifiers;
import com.thirdplace.db.TableFieldType;
import com.thirdplace.db.TableSchema;

import java.time.Instant;

public record Post(

    @TableField(fieldType = TableFieldType.STRING, modifiers = {TableFieldModifiers.PRIMARY_KEY})
    String id,

    @TableField(fieldType = TableFieldType.STRING, modifiers = {TableFieldModifiers.NOT_NULL})
    String title,

    @TableField(fieldType = TableFieldType.STRING, modifiers = {TableFieldModifiers.NOT_NULL})
    String author,

    @TableField(fieldType = TableFieldType.LONG_STRING)
    String description,

    @TableField(fieldType = TableFieldType.TIMESTAMP, modifiers = {TableFieldModifiers.NOT_NULL})
    Instant createdAt,

    @TableField(fieldType = TableFieldType.TIMESTAMP, modifiers = {TableFieldModifiers.NOT_NULL})
    Instant endDate,

    @TableField(fieldType = TableFieldType.INTEGER, modifiers = {TableFieldModifiers.NOT_NULL})
    int groupSize,

    @TableField(fieldType = TableFieldType.ARRAY)
    String[] tags,

    @TableField(fieldType = TableFieldType.STRING)
    String location,

    @TableField(fieldType = TableFieldType.DOUBLE, modifiers = {TableFieldModifiers.NOT_NULL})
    double latitude,

    @TableField(fieldType = TableFieldType.DOUBLE, modifiers = {TableFieldModifiers.NOT_NULL})
    double longitude,

    @TableField(fieldType = TableFieldType.TIMESTAMP, modifiers = {TableFieldModifiers.NOT_NULL})
    Instant proposedTime,

    @TableField(fieldType = TableFieldType.BOOLEAN, modifiers = {TableFieldModifiers.NOT_NULL})
    boolean isDateActivity,

    @TableField(fieldType = TableFieldType.STRING, modifiers = {TableFieldModifiers.NOT_NULL})
    String status,

    @TableField(fieldType = TableFieldType.STRING)
    String genderBalance,

    @TableField(fieldType = TableFieldType.STRING)
    String category
) implements TableSchema {}