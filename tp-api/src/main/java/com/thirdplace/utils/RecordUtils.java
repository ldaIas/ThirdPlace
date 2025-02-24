package com.thirdplace.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * A class of utility methods for working with records.
 */
public class RecordUtils {

    /**
     * Initialize a record with the given fields and values.
     * 
     * @param <T>            The type of the record to initialize
     * @param recordClass    The class of the record to initialize
     * @param fieldsToValues A map of field names to values to initialize the record
     *                       with. Field names are exact name of the record
     *                       component fields. Empty map creates a default record
     * @return The initialized record of type T
     */
    public static <T extends Record> T init(@Nonnull final Class<T> recordClass,
            @Nonnull final Map<String, Object> fieldsToValues) {

        // Map list of record components to component name -> component
        final Map<String, RecordComponent> recordFields = Arrays.stream(recordClass.getRecordComponents())
                .collect(Collectors.toMap(comp -> comp.getName(), c -> c, (o, n) -> o, LinkedHashMap::new));
        final Object[] componentValues = new Object[recordFields.size()];

        // Set to keep track of fields not yet set
        final Set<String> fieldsNotSet = new HashSet<>(recordFields.keySet());

        final AtomicInteger i = new AtomicInteger(0);
        recordFields.forEach((name, component) -> {
            final Object fieldValue = Optional.ofNullable(fieldsToValues.get(name)).orElse(null);

            if (fieldValue != null && !component.getType().isInstance(fieldValue)) {
                throw new RecordUtilsException(RecordUtilsException.ErrorCode.BAD_FIELD_TYPE,
                        "Field value " + fieldValue + " is not of type " + component.getType() + " when constructing " + recordClass.getName());
            }

            // If the field value is null, get the default value for the component
            final Object valueToSet = Optional.ofNullable(fieldValue)
                    .orElseGet(() -> getComponentDefaultValue(component));

            componentValues[i.getAndIncrement()] = valueToSet;

            // Remove the field from the set. Left over fields will result in an error
            // as that means they supplied fields not on the record
            fieldsNotSet.remove(name);
        });

        // If there are keys left in the set, throw an exception listing them as the
        // user supplied invalid fields
        if (!fieldsNotSet.isEmpty()) {
            throw new RecordUtilsException(RecordUtilsException.ErrorCode.BAD_FIELDS_SUPPLIED,
                    "Invalid fields supplied: " + fieldsToValues.keySet());
        }

        try {
            return recordClass.getDeclaredConstructor(Arrays.stream(recordClass.getRecordComponents())
                    .map(RecordComponent::getType).toArray(Class[]::new)).newInstance(componentValues);
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new RecordUtilsException(RecordUtilsException.ErrorCode.INSTANTIATION_ERROR,
                    "An error occured trying to create a new record of type " + recordClass.getSimpleName(), e);
        }

    }

    /**
     * Returns the default value for a record component. If the component is a
     * primitive, the default value is 0. If the component is an object reference,
     * the default value is null.
     *
     * @param component The record component to get the default value for
     * @return The default value for the record component
     */
    @Nullable
    private static Object getComponentDefaultValue(final RecordComponent component) {

        final Class<?> type = component.getType();

        return switch (type) {
        case Class<?> c when c == boolean.class -> false;
        case Class<?> c when c == byte.class -> (byte) 0;
        case Class<?> c when c == char.class -> (char) 0;
        case Class<?> c when c == short.class -> (short) 0;
        case Class<?> c when c == int.class -> 0;
        case Class<?> c when c == long.class -> 0L;
        case Class<?> c when c == float.class -> 0.0f;
        case Class<?> c when c == double.class -> 0.0d;
        default -> null;
        };

    }

}
