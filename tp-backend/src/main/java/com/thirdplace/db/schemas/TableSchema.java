package com.thirdplace.db.schemas;

import java.util.List;

public interface TableSchema {
    String getTableName();
    List<SchemaFieldReference> getSchemaFieldReferences();
}
