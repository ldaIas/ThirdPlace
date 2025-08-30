package com.thirdplace.db.schemas;

import java.time.Instant;
import java.util.List;

public record RSVP(

        String id,
        String userId,
        String postId,
        String status,
        Instant createdAt

) implements TableSchema {

    public static final String TABLE_NAME = "rsvps";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    public static enum RSVPFieldReference implements SchemaFieldReference {
        ID("id", TableFieldType.STRING, new TableFieldModifiers[] { TableFieldModifiers.PRIMARY_KEY }),
        USER_ID("userId", TableFieldType.STRING, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        POST_ID("postId", TableFieldType.STRING, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        STATUS("status", TableFieldType.STRING, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        CREATED_AT("createdAt", TableFieldType.TIMESTAMP, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL });

        private final String fieldName;
        private final TableFieldType fieldType;
        private final TableFieldModifiers[] modifiers;

        RSVPFieldReference(String fieldName, TableFieldType fieldType, TableFieldModifiers[] modifiers) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.modifiers = modifiers;
        }

        public String getFieldName() {
            return fieldName;
        }

        public TableFieldType getFieldType() {
            return fieldType;
        }

        public TableFieldModifiers[] getModifiers() {
            return modifiers;
        }
    }

    private static final List<SchemaFieldReference> schemaFieldReferences = List.of(RSVPFieldReference.values());

    @Override
    public List<SchemaFieldReference> getSchemaFieldReferences() {
        return schemaFieldReferences;
    }

}