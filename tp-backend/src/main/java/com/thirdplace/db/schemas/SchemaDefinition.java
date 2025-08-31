package com.thirdplace.db.schemas;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SchemaDefinition {
    String tableName();
    Class<? extends SchemaFieldReference> fieldReference();
}
