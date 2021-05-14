package software.amazon.wafv2.converters;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import com.google.common.collect.ImmutableMap;

import software.amazon.wafv2.loggingconfiguration.LoggingFilter;
import software.amazon.awssdk.services.wafv2.model.All;
import software.amazon.awssdk.services.wafv2.model.Method;
import software.amazon.awssdk.services.wafv2.model.QueryString;
import software.amazon.awssdk.services.wafv2.model.UriPath;
import software.amazon.wafv2.loggingconfiguration.FieldToMatch;

import java.util.Map;

/**
 * Converter for FieldToMatch and LoggingFilter Objects from/to SDK/Model
 */
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface Converter {
    Converter INSTANCE = Mappers.getMapper(Converter.class);
    
    // Logging Filter
    software.amazon.awssdk.services.wafv2.model.LoggingFilter convert(LoggingFilter source);
    LoggingFilter invert(software.amazon.awssdk.services.wafv2.model.LoggingFilter source);
    
    // FieldToMatch
    software.amazon.awssdk.services.wafv2.model.FieldToMatch convert(FieldToMatch source);
    FieldToMatch invert(software.amazon.awssdk.services.wafv2.model.FieldToMatch source);

    default UriPath convertUriPath(Map<String, Object> value) {
        return UriPath.builder().build();
    }

    default Map<String, Object> invertUriPath(UriPath value) {
        return (value == null) ? null : ImmutableMap.of();
    }

    default Method convertMethod(Map<String, Object> value) {
        return Method.builder().build();
    }

    default Map<String, Object> invertMethod(Method value) {
        return (value == null) ? null : ImmutableMap.of();
    }
    
    default QueryString convertQueryString(Map<String, Object> value) {
        return QueryString.builder().build();
    }

    default Map<String, Object> invertQueryString(QueryString value) {
        return (value == null) ? null : ImmutableMap.of();
    }

    default All convertJsonMatchAll(Map<String, Object> value) {
        return All.builder().build();
    }

    default Map<String, Object> invertJsonMatchAll(All value) {
        return (value == null) ? null : ImmutableMap.of();
    }

}
