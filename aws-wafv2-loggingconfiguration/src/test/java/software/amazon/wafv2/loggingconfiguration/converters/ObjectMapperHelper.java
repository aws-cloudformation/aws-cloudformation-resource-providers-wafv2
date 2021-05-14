package software.amazon.wafv2.loggingconfiguration.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;

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
}
