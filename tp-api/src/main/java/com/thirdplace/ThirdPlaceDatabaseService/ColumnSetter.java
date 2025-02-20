package com.thirdplace.ThirdPlaceDatabaseService;

public record ColumnSetter(
    String column,
    String value
) {
    private static final String BIND_FORMATTER = "%s=?";

    public String bindColumn() {
        return String.format(BIND_FORMATTER, column);
    }
}
