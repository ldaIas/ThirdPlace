package com.thirdplace.db;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.postgresql.jdbc.PgArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

public class AppDbInterpreter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDbInterpreter.class);

    public static final String EMPTY = "";
    public static final String SPACE = " ";
    public static final String COMMA = ", ";

    private static final String CREATE_TABLE_DDL = "CREATE TABLE IF NOT EXISTS %s (%s)";
    private static final String INSERT_TABLE_SQL = "INSERT INTO %s (%s) VALUES (%s)";

    /**
     * Generate the table's DDL for creation in the database.
     * Input schemas must have all fields labeled with {@link TableField}.
     * The name of the table is the name of the schema class in lowercase.
     * 
     * @param schema The schema to generate the DDL for
     * @return The DDL for the table
     */
    public static <T extends Record & TableSchema> String generateTableDdl(final Class<T> schema) {

        final StringBuilder fieldDefinitions = new StringBuilder();

        // Loop through TableFields and create the ddl based on the annotation
        Arrays.asList(schema.getRecordComponents()).stream()
                .map(field -> {
                    if (field.getAnnotation(TableField.class) == null) {
                        throw new IllegalArgumentException(
                                "All fields in a TableSchema must be annotated with @TableField. " +
                                        "TableSchema: \"" + schema.getSimpleName() + "\" Field: \"" + field.getName()
                                        + "\"");
                    }

                    final TableField tableField = field.getAnnotation(TableField.class);
                    final String fieldName = field.getName();
                    final String fieldType = tableField.fieldType().getValue();
                    final String modifiers = Arrays.stream(tableField.modifiers())
                            .map(modifier -> modifier.getValue())
                            .reduce((a, b) -> a + SPACE + b).orElse(EMPTY);

                    return String.format("%s %s %s", fieldName, fieldType, modifiers);
                })
                .reduce((a, b) -> a + COMMA + b)
                .ifPresentOrElse(fieldDefinitions::append,
                        () -> new IllegalArgumentException(
                                "TableSchema" + schema.getSimpleName()
                                        + " must have at least one field annotated with @TableField"));

        return String.format(CREATE_TABLE_DDL, getTableNameForSchema(schema), fieldDefinitions);
    }

    public static <T extends Record & TableSchema> String generateInsertSql(final Class<T> schema) {
        final StringBuilder sql = new StringBuilder();

        // Generate the field names and placeholders
        final String fields = Arrays.asList(schema.getRecordComponents()).stream()
                .map(field -> field.getName())
                .reduce((a, b) -> a + COMMA + b)
                .orElse(EMPTY);

        final String placeholders = Arrays.asList(schema.getRecordComponents()).stream()
                .map(field -> "?")
                .reduce((a, b) -> a + COMMA + b)
                .orElse(EMPTY);

        sql.append(String.format(INSERT_TABLE_SQL,
                getTableNameForSchema(schema), fields, placeholders));

        return sql.toString();
    }

    /**
     * Generate the sql for an insert into the given schema then prepares the
     * statement with the
     * object's values.
     * 
     * @param <T>        Class of the schema
     * @param schema     The schema to generate the sql for
     * @param connection The connection to use to prepare the statement
     * @return The prepared statement with values bound
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public static <T extends Record & TableSchema> PreparedStatement prepareInsertStatement(
            final T schema, final Connection connection) throws SQLException {

        final Class<T> schemaClass = (Class<T>) schema.getClass();
        final PreparedStatement stmt = connection.prepareStatement(generateInsertSql(schemaClass));

        // Loop through the schema fields and add to prepared statement
        final AtomicInteger paramCounter = new AtomicInteger(1);
        Arrays.asList(schemaClass.getRecordComponents()).stream()
                .forEachOrdered(field -> {
                    try {
                        addFieldToStatement(stmt, connection, field.getAccessor().invoke(schema),
                                field.getAnnotation(TableField.class), paramCounter.getAndIncrement());
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        throw new RuntimeException(ex);
                    }
                });

        return stmt;
    }

    private static void addFieldToStatement(final PreparedStatement stmt, final Connection conn, final Object value,
            final TableField fieldDetails, final int index) {
        try {
            switch (fieldDetails.fieldType()) {
                case STRING -> stmt.setString(index, (String) value);
                case LONG_STRING -> stmt.setString(index, (String) value);
                case INTEGER -> stmt.setInt(index, (int) value);
                case DOUBLE -> stmt.setDouble(index, (double) value);
                case BOOLEAN -> stmt.setBoolean(index, (boolean) value);
                case TIMESTAMP -> stmt.setTimestamp(index, Timestamp.from((Instant) value));
                case ARRAY -> stmt.setArray(index, conn.createArrayOf("TEXT", (String[]) value));
            }
        } catch (SQLException e) {

            LOGGER.error("Failed to add field to statement. Value: " + value + "; Table Field details: " + fieldDetails
                    + "; Index: " + index, e);

            throw new RuntimeException(e);
        }
    }

    /**
     * Turn a result set into the specified schema object
     * 
     * @param <T>         The type of schema class to convert to
     * @param schemaClass The schema class
     * @param rs          The result set
     * @return The schema object
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public static <T extends Record & TableSchema> T mapResultSetToSchema(final Class<T> schemaClass,
            final ResultSet rs) throws SQLException {

        final Object[] constructorArgs = new Object[schemaClass.getRecordComponents().length];

        try {
            // Assuming the columns are in the same order as record components
            for (final AtomicInteger atomicI = new AtomicInteger(1); atomicI.get() <= rs.getMetaData()
                    .getColumnCount(); atomicI.incrementAndGet()) {

                final int i = atomicI.get();
                final String fieldName = rs.getMetaData().getColumnName(i);
                final Object fieldValue = rs.getObject(i);

                Arrays.asList(schemaClass.getRecordComponents()).stream()
                        .filter(field -> field.getName().equalsIgnoreCase(fieldName))
                        .findFirst()
                        .ifPresentOrElse((field) -> {
                            final Object convertedValue = mapResultObjectToTableFieldValue(
                                    field.getAnnotation(TableField.class), fieldValue);
                            constructorArgs[i - 1] = convertedValue;
                        },
                                () -> {
                                    final IllegalArgumentException ex = new IllegalArgumentException(
                                            "Field " + fieldName + " not found in schema "
                                                    + schemaClass.getSimpleName());
                                    LOGGER.error("Error while mapping result set to schema", ex);
                                    throw ex;
                                });
            }

            return (T) schemaClass.getDeclaredConstructors()[0].newInstance(constructorArgs);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {

            LOGGER.error("Failed to map result set to schema " + schemaClass.getSimpleName(), ex);
            throw new RuntimeException(ex);
        }
    }

    private static Object mapResultObjectToTableFieldValue(final TableField fieldDetails, final Object incomingValue) {

        return switch (fieldDetails.fieldType()) {
            case STRING -> (String) incomingValue;
            case LONG_STRING -> (String) incomingValue;
            case INTEGER -> (int) incomingValue;
            case DOUBLE -> (double) incomingValue;
            case BOOLEAN -> (boolean) incomingValue;
            case TIMESTAMP -> ((Timestamp) incomingValue).toInstant();
            case ARRAY -> {
                try {
                    final PgArray pgArray = (PgArray) incomingValue;
                    yield (String[]) pgArray.getArray();
                } catch (SQLException ex) {
                    LOGGER.error("Failed to convert array field with value" + incomingValue + " in result set to array",
                            ex);
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    public static <T extends Record & TableSchema> String getTableNameForSchema(final Class<T> schema) {
        return schema.getSimpleName().toLowerCase() + "s";
    }
}
