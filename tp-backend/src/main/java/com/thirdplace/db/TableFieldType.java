package com.thirdplace.db;

public enum TableFieldType {
    
    STRING("VARCHAR(255)"),
    LONG_STRING("VARCHAR(500)"),
    INTEGER("INTEGER"),
    TIMESTAMP("TIMESTAMP"),
    ARRAY("TEXT[]"),
    DOUBLE("DOUBLE PRECISION"),
    BOOLEAN("BOOLEAN");

    String value;

    private TableFieldType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
