package com.thirdplace.utils;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
     *                       component fields
     * @return The initialized record of type T
     */
    public static <T extends Record> T init(final Class<T> recordClass, final Map<String, Object> fieldsToValues) {

        // Map list of record components to component name -> component
        final Map<String, RecordComponent> recordFields = Arrays.stream(recordClass.getRecordComponents())
                .collect(Collectors.toMap(comp -> comp.getName(), c -> c));
        final Object[] componentValues = new Object[recordFields.size()];

        recordFields.forEach((name, comp) -> {
            
        });

        IntStream.range(0, recordFields.size()).forEach(i -> {
            final RecordComponent component = recordFields.get(i);
            final String fieldName = component.getName();
            final Object fieldValue = Optional.ofNullable(fieldsToValues.get(fieldName)).orElse(null);

            if (fieldValue != null && !component.getType().isInstance(fieldValue)) {
                throw new RecordUtilsException(RecordUtilsException.ErrorCode.BAD_FIELD_TYPE,
                        "Field value " + fieldValue + " is not of type " + component.getType());
            }

            componentValues[i] = fieldValue;
        });

        try {
            return recordClass.getDeclaredConstructor(Arrays.stream(recordClass.getRecordComponents())
                    .map(RecordComponent::getType).toArray(Class[]::new)).newInstance(componentValues);
        } catch (final Exception e) {
            throw new RecordUtilsException(RecordUtilsException.ErrorCode.INSTANTIATION_ERROR,
                    "An error occured trying to create a new record of type " + recordClass.getSimpleName(), e);
        }

    }

}
