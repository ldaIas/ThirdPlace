package com.thirdplace.ThirdPlaceDatabaseService;

public record ColumnSetter(
    String column,
    String value
) {
    public String bindColumn() {
        return column + "=?";
    }
}
