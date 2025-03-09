package com.thirdplace.utils;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;

import org.eclipse.jetty.util.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link RecordUtils}
 */
public class RecordUtilsTests {

    record TestRecord(String field1, int field2) {
        static final String FIELD_1 = "field1";
        static final String FIELD_2 = "field2";
    }

    /**
     * Test to ensure that we can create a TestRecord using RecordUtils.init() with
     * valid fields and values.
     */
    @Test
    void testInitWithValidFieldsAndValues() {
        final Class<TestRecord> recordClass = TestRecord.class;
        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(TestRecord.FIELD_1, "value1");
        fieldValues.put(TestRecord.FIELD_2, 42);

        final TestRecord result = RecordUtils.init(recordClass, fieldValues);

        assertNotNull(result);
        assertEquals("value1", result.field1());
        assertEquals(42, result.field2());
    }

    /**
     * Test to ensure that we get the correct error when an incompatible type is
     * supplied
     */
    @Test
    void testInitIncompatibleValueType() {
        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(TestRecord.FIELD_2, "not an integer");

        try {
            RecordUtils.init(TestRecord.class, fieldValues);
        } catch (RecordUtilsException e) {
            assertEquals(RecordUtilsException.ErrorCode.BAD_FIELD_TYPE, e.getErrorCode());
        }
    }

    /**
     * Test to ensure that if a field is supplied that doesn't exist on the record,
     * we throw the correct error
     */
    @Test
    void testInitNonExistentField() {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(TestRecord.FIELD_1, "value1");
        fieldValues.put("nonExistentField", "value2");

        try {
            RecordUtils.init(TestRecord.class, fieldValues);
        } catch (RecordUtilsException e) {
            assertEquals(RecordUtilsException.ErrorCode.BAD_FIELDS_SUPPLIED, e.getErrorCode(),
                    "Expected error for bad values supplied to Record init");
        }
    }

    /**
     * Test to ensure that if no values are passed in, we return a defaulted record
     */
    @Test
    void testInitEmptyMap() {
        record TestFnRecord(boolean field1, byte field2, char field3, double field4, float field5, int field6,
                long field7, short field8, Object fieldO) {
        }

        final TestFnRecord result = RecordUtils.init(TestFnRecord.class, Map.of());

        Assertions.assertFalse(result.field1, "Expected field1 to default false");
        Assertions.assertEquals(0, result.field2, "Expected field2 to default 0");
        Assertions.assertEquals('\u0000', result.field3, "Expected field3 to default '\u0000'");
        Assertions.assertEquals(0.0, result.field4, "Expected field4 to default 0.0");
        Assertions.assertEquals(0.0f, result.field5, "Expected field5 to default 0.0f");
        Assertions.assertEquals(0, result.field6, "Expected field6 to default 0");
        Assertions.assertEquals(0, result.field7, "Expected field7 to default 0");
        Assertions.assertEquals(0, result.field8, "Expected field8 to default 0");
        Assertions.assertNull(result.fieldO, "Expected field3 to default null");
    }
}