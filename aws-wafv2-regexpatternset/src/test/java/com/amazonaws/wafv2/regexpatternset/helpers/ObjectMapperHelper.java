package com.amazonaws.wafv2.regexpatternset.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;

public class ObjectMapperHelper {
    public static <T> T getObject(@NonNull final String filePath, @NonNull final Class<T> clazz) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(filePath), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
