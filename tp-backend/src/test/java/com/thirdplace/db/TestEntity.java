package com.thirdplace.db;

import java.time.Instant;
import java.util.List;

import com.thirdplace.db.schemas.SchemaFieldReference;
import com.thirdplace.db.schemas.TableFieldModifiers;
import com.thirdplace.db.schemas.TableFieldType;
import com.thirdplace.db.schemas.TableSchema;

public record TestEntity(
    String id,
    String name,
    String description,
    Instant createdAt
) implements TableSchema {

    public static final String TABLE_NAME = "test_entities";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    public static enum TestEntityFieldReference implements SchemaFieldReference {
        ID("id", TableFieldType.STRING, new TableFieldModifiers[] { TableFieldModifiers.PRIMARY_KEY }),
        NAME("name", TableFieldType.STRING, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL }),
        DESCRIPTION("description", TableFieldType.LONG_STRING, new TableFieldModifiers[] {}),
        CREATED_AT("createdAt", TableFieldType.TIMESTAMP, new TableFieldModifiers[] { TableFieldModifiers.NOT_NULL });

        private final String fieldName;
        private final TableFieldType fieldType;
        private final TableFieldModifiers[] modifiers;

        TestEntityFieldReference(String fieldName, TableFieldType fieldType, TableFieldModifiers[] modifiers) {
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

    private static final List<SchemaFieldReference> schemaFieldReferences = List.of(TestEntityFieldReference.values());

    @Override
    public List<SchemaFieldReference> getSchemaFieldReferences() {
        return schemaFieldReferences;
    }
}