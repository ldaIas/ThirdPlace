package com.thirdplace.db.schemas;

import java.time.Instant;
import java.util.List;

@SchemaDefinition(fieldReference = Post.PostFieldReference.class)
public record Post(

        String id,

        String title,

        String author,

        String description,

        Instant createdAt,

        Instant endDate,

        int groupSize,

        String[] tags,

        String location,

        double latitude,

        double longitude,

        Instant proposedTime,

        boolean isDateActivity,

        String status,

        String genderBalance,

        String category

) implements TableSchema {

    public static final String TABLE_NAME = "posts";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    public static enum PostFieldReference implements SchemaFieldReference {
        ID("id", TableFieldType.STRING, new TableFieldModifiers[] { TableFieldModifiers.PRIMARY_KEY }),
        TITLE("title", TableFieldType.STRING, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        AUTHOR("author", TableFieldType.STRING, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        DESCRIPTION("description", TableFieldType.LONG_STRING, new TableFieldModifiers[] {}),
        CREATED_AT("createdAt", TableFieldType.TIMESTAMP, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        END_DATE("endDate", TableFieldType.TIMESTAMP, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        GROUP_SIZE("groupSize", TableFieldType.INTEGER, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        TAGS("tags", TableFieldType.ARRAY, new TableFieldModifiers[] {}),
        LOCATION("location", TableFieldType.STRING, new TableFieldModifiers[] {}),
        LATITUDE("latitude", TableFieldType.DOUBLE, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        LONGITUDE("longitude", TableFieldType.DOUBLE, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        PROPOSED_TIME("proposedTime", TableFieldType.TIMESTAMP,
                new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        IS_DATE_ACTIVITY("isDateActivity", TableFieldType.BOOLEAN,
                new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        STATUS("status", TableFieldType.STRING, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        GENDER_BALANCE("genderBalance", TableFieldType.STRING, new TableFieldModifiers[] {}),
        CATEGORY("category", TableFieldType.STRING, new TableFieldModifiers[] {});

        private final String fieldName;
        private final TableFieldType fieldType;
        private final TableFieldModifiers[] modifiers;

        PostFieldReference(String fieldName, TableFieldType fieldType, TableFieldModifiers[] modifiers) {
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

    private static final List<SchemaFieldReference> schemaFieldReferences = List.of(PostFieldReference.values());

    @Override
    public List<SchemaFieldReference> getSchemaFieldReferences() {
        return schemaFieldReferences;
    }

}