package com.thirdplace.utils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class RecordUtilsTests {

    record TestRecord(String field1, int field2) {
        static final String FIELD_1 = "field1";
        static final String FIELD_2 = "field2";
    }

    @Test
    public void testInitWithValidFieldsAndValues() {
        final Class<TestRecord> recordClass = TestRecord.class;
        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(TestRecord.FIELD_1, "value1");
        fieldValues.put(TestRecord.FIELD_2, 42);

        final TestRecord result = RecordUtils.init(recordClass, fieldValues);

        assertNotNull(result);
        assertEquals("value1", result.field1());
        assertEquals(42, result.field2());
    }

    @Test
    public void testInitIncompatibleValueType() {
        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(TestRecord.FIELD_2, "not an integer");

        try {
            RecordUtils.init(TestRecord.class, fieldValues);
        }  catch (RecordUtilsException e) {
            assertEquals(RecordUtilsException.ErrorCode.BAD_FIELD_TYPE, e.getErrorCode());
        }
    }

    @Test
    public void testInitNonExistentField() {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(TestRecord.FIELD_1, "value1");
        fieldValues.put("nonExistentField", "value2");

        assertThrows(RuntimeException.class, () -> {
            RecordUtils.init(TestRecord.class, fieldValues);
        });
    }

    @Test
    public void testInitNullMap() {
        assertThrows(NullPointerException.class, () -> {
            RecordUtils.init(TestRecord.class, null);
        });
    }

    @Test
    public void testInitNullRecordClass() {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(TestRecord.FIELD_1, "value1");

        assertThrows(NullPointerException.class, () -> {
            RecordUtils.init(null, fieldValues);
        });
    }

    @Test
    public void testInitEmptyMap() {
        Map<String, Object> fieldValues = new HashMap<>();
        
        assertThrows(IllegalArgumentException.class, () -> {
            RecordUtils.init(TestRecord.class, fieldValues);
        });
    }
}