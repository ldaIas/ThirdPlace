package com.thirdplace.db;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.postgresql.jdbc.PgArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdplace.db.schemas.SchemaFieldReference;
import com.thirdplace.db.schemas.TableSchema;

import java.sql.ResultSet;

public class AppDbInterpreter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDbInterpreter.class);

    public static final String EMPTY = "";
    public static final String SPACE = " ";
    public static final String COMMA = ", ";

    private static final String CREATE_TABLE_DDL = "CREATE TABLE IF NOT EXISTS %s (%s)";
    private static final String INSERT_TABLE_SQL = "INSERT INTO %s (%s) VALUES (%s)";
    private static final String UPDATE_TABLE_SQL = "UPDATE %s SET %s WHERE %s";
    private static final String SELECT_TABLE_SQL = "SELECT * FROM %s WHERE %s";

    /**
     * Generate the table's DDL for creation in the database.
     * Input schemas must implement getSchemaFieldReferences().
     * The name of the table is the name of the schema class in lowercase.
     * 
     * @param schema The schema to generate the DDL for
     * @return The DDL for the table
     */
    public static String generateTableDdl(final String tableName, final List<SchemaFieldReference> schemaFields) {
        try {

            final StringBuilder fieldDefinitions = new StringBuilder();

            schemaFields.stream()
                    .map(fieldRef -> {
                        final String fieldName = fieldRef.getFieldName();
                        final String fieldType = fieldRef.getFieldType().getValue();
                        final String modifiers = Arrays.stream(fieldRef.getModifiers())
                                .map(modifier -> modifier.getValue())
                                .reduce((a, b) -> a + SPACE + b).orElse(EMPTY);

                        return String.format("%s %s %s", fieldName, fieldType, modifiers);
                    })
                    .reduce((a, b) -> a + COMMA + b)
                    .ifPresentOrElse(fieldDefinitions::append,
                            () -> {
                                throw new IllegalArgumentException(
                                        "ust have at least one field reference");
                            });

            return String.format(CREATE_TABLE_DDL, tableName, fieldDefinitions);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate DDL for schema " + tableName, ex);
        }
    }

    public static String generateInsertSql(final TableSchema schema) {
        try {

            final String fields = schema.getSchemaFieldReferences().stream()
                    .map(SchemaFieldReference::getFieldName)
                    .reduce((a, b) -> a + COMMA + b)
                    .orElse(EMPTY);

            final String placeholders = schema.getSchemaFieldReferences().stream()
                    .map(field -> "?")
                    .reduce((a, b) -> a + COMMA + b)
                    .orElse(EMPTY);

            return String.format(INSERT_TABLE_SQL, schema.getTableName(), fields, placeholders);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate insert SQL for schema " + schema.getClass().getSimpleName(),
                    ex);
        }
    }

    public static String generateUpdateSql(final TableSchema schema, final List<WhereFilter> whereClause) {
        try {
            final String setClause = schema.getSchemaFieldReferences().stream()
                    .map(fieldRef -> fieldRef.getFieldName() + " = ?")
                    .reduce((a, b) -> a + COMMA + b)
                    .orElse(EMPTY);

            final String whereString = whereClause.stream()
                    .map(filter -> filter.schemaField().getFieldName() + " " + filter.operator() + " ?")
                    .reduce((a, b) -> a + " AND " + b)
                    .orElse(EMPTY);

            return String.format(UPDATE_TABLE_SQL, schema.getTableName(), setClause, whereString);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate update SQL for schema " + schema.getClass().getSimpleName(),
                    ex);
        }
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
    public static PreparedStatement prepareInsertStatement(final TableSchema schema, final Connection connection)
            throws SQLException {

        final PreparedStatement stmt = connection.prepareStatement(generateInsertSql(schema));

        final AtomicInteger paramCounter = new AtomicInteger(1);
        schema.getSchemaFieldReferences().stream()
                .forEachOrdered(fieldRef -> {
                    try {
                        final Object fieldValue = getFieldValue(schema, fieldRef.getFieldName());
                        addFieldToStatement(stmt, connection, fieldValue, fieldRef, paramCounter.getAndIncrement());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

        return stmt;
    }

    public static PreparedStatement prepareUpdateStatement(final TableSchema schema,
            final List<WhereFilter> whereClause,
            final Connection connection) throws SQLException {

        final PreparedStatement stmt = connection.prepareStatement(generateUpdateSql(schema, whereClause));

        final AtomicInteger paramCounter = new AtomicInteger(1);
        schema.getSchemaFieldReferences().stream()
                .forEachOrdered(fieldRef -> {
                    try {
                        final Object fieldValue = getFieldValue(schema, fieldRef.getFieldName());
                        addFieldToStatement(stmt, connection, fieldValue, fieldRef, paramCounter.getAndIncrement());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

        whereClause.stream()
                .forEachOrdered(filter -> {
                    try {
                        addFilterValueToStatement(stmt, connection, filter, paramCounter.getAndIncrement());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
        return stmt;
    }

    private static Object getFieldValue(final TableSchema schema, final String fieldName) {
        try {
            return Arrays.stream(schema.getClass().getRecordComponents())
                    .filter(component -> component.getName().equals(fieldName))
                    .findFirst()
                    .map(component -> {
                        try {
                            return component.getAccessor().invoke(schema);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    })
                    .orElseThrow(() -> new IllegalArgumentException("Field " + fieldName + " not found in schema"));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to get field value for " + fieldName, ex);
        }
    }

    private static void addFieldToStatement(final PreparedStatement stmt, final Connection conn, final Object value,
            final SchemaFieldReference fieldRef, final int index) {
        try {
            switch (fieldRef.getFieldType()) {
                case STRING -> stmt.setString(index, (String) value);
                case LONG_STRING -> stmt.setString(index, (String) value);
                case INTEGER -> stmt.setInt(index, (int) value);
                case DOUBLE -> stmt.setDouble(index, (double) value);
                case BOOLEAN -> stmt.setBoolean(index, (boolean) value);
                case TIMESTAMP -> stmt.setTimestamp(index, Timestamp.from((Instant) value));
                case ARRAY -> stmt.setArray(index, conn.createArrayOf("TEXT", (String[]) value));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to add field to statement. Value: " + value + "; Field reference: "
                    + fieldRef.getFieldName()
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
    public static <T extends TableSchema> T mapResultSetToSchema(final Class<T> schemaClass,
            final ResultSet rs) throws SQLException {

        final Object[] constructorArgs = new Object[schemaClass.getRecordComponents().length];

        try {
            final T schemaInstance = (T) schemaClass.getDeclaredConstructors()[0]
                    .newInstance(new Object[schemaClass.getRecordComponents().length]);

            for (final AtomicInteger atomicI = new AtomicInteger(1); atomicI.get() <= rs.getMetaData()
                    .getColumnCount(); atomicI.incrementAndGet()) {

                final int i = atomicI.get();
                final String fieldName = rs.getMetaData().getColumnName(i);
                final Object fieldValue = rs.getObject(i);

                schemaInstance.getSchemaFieldReferences().stream()
                        .filter(fieldRef -> fieldRef.getFieldName().equalsIgnoreCase(fieldName))
                        .findFirst()
                        .ifPresentOrElse((fieldRef) -> {
                            final Object convertedValue = mapResultObjectToSchemaFieldValue(fieldRef, fieldValue);
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

    private static Object mapResultObjectToSchemaFieldValue(final SchemaFieldReference fieldRef,
            final Object incomingValue) {
        return switch (fieldRef.getFieldType()) {
            case STRING, LONG_STRING -> (String) incomingValue;
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

    public static String generateSelectSql(final String tableName, final List<WhereFilter> whereClause) {
        try {
            final String whereString = whereClause.stream()
                    .map(filter -> filter.schemaField().getFieldName() + " " + filter.operator().getValue() + " ?")
                    .reduce((a, b) -> a + " AND " + b)
                    .orElse("1=1");

            return String.format(SELECT_TABLE_SQL, tableName, whereString);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate select SQL for schema " + tableName, ex);
        }
    }

    public static PreparedStatement prepareSelectStatement(final String tableName, final List<WhereFilter> whereClause,
            final Connection connection) throws SQLException {

        final PreparedStatement stmt = connection.prepareStatement(generateSelectSql(tableName, whereClause));

        final AtomicInteger paramCounter = new AtomicInteger(1);
        whereClause.stream()
                .forEachOrdered(filter -> {
                    try {
                        addFilterValueToStatement(stmt, connection, filter, paramCounter.getAndIncrement());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
        return stmt;
    }

    private static void addFilterValueToStatement(final PreparedStatement stmt, final Connection conn,
            final WhereFilter filter, final int index) {
        try {
            switch (filter.schemaField().getFieldType()) {
                case STRING -> stmt.setString(index, (String) filter.value());
                case LONG_STRING -> stmt.setString(index, (String) filter.value());
                case INTEGER -> stmt.setInt(index, (Integer) filter.value());
                case DOUBLE -> stmt.setDouble(index, (Double) filter.value());
                case BOOLEAN -> stmt.setBoolean(index, (Boolean) filter.value());
                case TIMESTAMP -> stmt.setTimestamp(index, Timestamp.from((Instant) filter.value()));
                case ARRAY -> stmt.setArray(index, conn.createArrayOf("TEXT", (String[]) filter.value()));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to add filter value to statement. Value: " + filter.value() + "; Field: "
                    + filter.schemaField().getFieldName() + "; Index: " + index, e);
            throw new RuntimeException(e);
        }
    }
}
