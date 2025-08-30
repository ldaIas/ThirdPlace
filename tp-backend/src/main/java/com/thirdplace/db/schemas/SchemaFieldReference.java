package com.thirdplace.db.schemas;

public interface SchemaFieldReference {
    String getFieldName();
    TableFieldType getFieldType();
    TableFieldModifiers[] getModifiers();
}
