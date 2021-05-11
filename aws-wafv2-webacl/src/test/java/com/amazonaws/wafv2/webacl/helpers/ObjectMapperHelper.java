package com.amazonaws.wafv2.webacl.helpers;

import com.amazonaws.wafv2.webacl.CustomResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ObjectMapperHelper {
    private final static String pathPrefix = new File("aws-wafv2-webacl").exists() ? "aws-wafv2-webacl/" : "";

    public static <T> T getObject(@NonNull final String filePath, @NonNull final Class<T> clazz) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(pathPrefix + filePath), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, CustomResponseBody> getObjectForCustomResponseBodyMap(
        @NonNull final String filePath) {
        TypeReference<HashMap<String, CustomResponseBody>> typeRef =
            new TypeReference<HashMap<String, CustomResponseBody>>() {};
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(pathPrefix + filePath), typeRef);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
