package com.thirdplace.ThirdPlaceDatabaseService;

/**
 * Class to represent a column type in a table
 * Example:
 * {
 *   "columnName": "id",
 *   "columnType": "INTEGER PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY UNIQUE"
 * }
 */
public record TableColumnType(
    String columnName,
    String columnType
) {
    
}
