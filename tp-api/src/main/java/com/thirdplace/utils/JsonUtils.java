package com.thirdplace.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonUtilsException(JsonUtilsException.ErrorCode.ERROR_WHILE_SERIALIZING, "Failed to serialize object to JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new JsonUtilsException(JsonUtilsException.ErrorCode.ERROR_WHILE_DESERIALIZING, "Failed to deserialize JSON to object: " + json, e);
        }
    }

}
