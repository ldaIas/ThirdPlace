package com.thirdplace.db.schemas;

public enum TableFieldModifiers {

    NOT_NULL("NOT NULL"),
    PRIMARY_KEY("PRIMARY KEY"),
    UNIQUE("UNIQUE");
    
    String value;

    private TableFieldModifiers(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
