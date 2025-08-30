package com.thirdplace.db.schemas;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotations for fields in a {@link TableSchema} that describes
 * how the field should work with the database
 */
@Target({ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface TableField {
    String fieldReference();
    TableFieldType fieldType();
    TableFieldModifiers[] modifiers() default {};
}
