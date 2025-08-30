package com.thirdplace.db;

import com.thirdplace.db.schemas.SchemaFieldReference;

public record WhereFilter(
    SchemaFieldReference schemaField,
    FilterOperator operator,
    Object value
) {

    public static enum FilterOperator {
        EQUALS("="),
        NOT_EQUALS("!="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        GREATER_THAN_OR_EQUALS(">="),
        LESS_THAN_OR_EQUALS("<="),
        LIKE("LIKE"),
        IN("IN");

        private final String value;

        private FilterOperator(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
    
}
