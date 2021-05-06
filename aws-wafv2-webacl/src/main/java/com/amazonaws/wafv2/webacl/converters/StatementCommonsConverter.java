package com.amazonaws.wafv2.webacl.converters;

import com.google.common.collect.ImmutableMap;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.wafv2.model.All;
import software.amazon.awssdk.services.wafv2.model.AllQueryArguments;
import software.amazon.awssdk.services.wafv2.model.Body;
import software.amazon.awssdk.services.wafv2.model.CountAction;
import software.amazon.awssdk.services.wafv2.model.DefaultAction;
import software.amazon.awssdk.services.wafv2.model.ExcludedRule;
import software.amazon.awssdk.services.wafv2.model.FieldToMatch;
import software.amazon.awssdk.services.wafv2.model.Method;
import software.amazon.awssdk.services.wafv2.model.NoneAction;
import software.amazon.awssdk.services.wafv2.model.OverrideAction;
import software.amazon.awssdk.services.wafv2.model.QueryString;
import software.amazon.awssdk.services.wafv2.model.RuleAction;
import software.amazon.awssdk.services.wafv2.model.SingleHeader;
import software.amazon.awssdk.services.wafv2.model.SingleQueryArgument;
import software.amazon.awssdk.services.wafv2.model.TextTransformation;
import software.amazon.awssdk.services.wafv2.model.UriPath;
import software.amazon.awssdk.services.wafv2.model.VisibilityConfig;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Common types to support statement conversion.
 */
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface StatementCommonsConverter {
    StatementCommonsConverter INSTANCE = Mappers.getMapper(StatementCommonsConverter.class);

    //---------------------------------------------------------------------
    // DefaultAction
    //---------------------------------------------------------------------
    DefaultAction convert(com.amazonaws.wafv2.webacl.DefaultAction source);
    com.amazonaws.wafv2.webacl.DefaultAction invert(DefaultAction source);

    //---------------------------------------------------------------------
    // ExcludedRule
    //---------------------------------------------------------------------
    ExcludedRule convert(com.amazonaws.wafv2.webacl.ExcludedRule source);
    com.amazonaws.wafv2.webacl.ExcludedRule invert(ExcludedRule source);

    //---------------------------------------------------------------------
    // FieldToMatch
    //---------------------------------------------------------------------
    @Mapping(source = "uriPath", target = "uriPath")
    FieldToMatch convert(com.amazonaws.wafv2.webacl.FieldToMatch source);
    @Mapping(source = "uriPath", target = "uriPath")
    com.amazonaws.wafv2.webacl.FieldToMatch invert(FieldToMatch source);

    //---------------------------------------------------------------------
    // OverrideAction
    //---------------------------------------------------------------------
    OverrideAction convert(com.amazonaws.wafv2.webacl.OverrideAction source);
    com.amazonaws.wafv2.webacl.OverrideAction invert(OverrideAction source);

    //---------------------------------------------------------------------
    // RuleAction
    //---------------------------------------------------------------------
    RuleAction convert(com.amazonaws.wafv2.webacl.RuleAction source);
    com.amazonaws.wafv2.webacl.RuleAction invert(RuleAction source);

    //---------------------------------------------------------------------
    // SingleQueryArgument
    //---------------------------------------------------------------------
    SingleQueryArgument convert(com.amazonaws.wafv2.webacl.SingleQueryArgument source);
    com.amazonaws.wafv2.webacl.SingleQueryArgument invert(SingleQueryArgument source);

    //---------------------------------------------------------------------
    // SingleHeader
    //---------------------------------------------------------------------
    SingleHeader convert(com.amazonaws.wafv2.webacl.SingleHeader source);
    com.amazonaws.wafv2.webacl.SingleHeader invert(SingleHeader source);

    //---------------------------------------------------------------------
    // TextTransformation
    //---------------------------------------------------------------------
    TextTransformation convert(com.amazonaws.wafv2.webacl.TextTransformation source);
    com.amazonaws.wafv2.webacl.TextTransformation invert(TextTransformation source);

    //---------------------------------------------------------------------
    // List of TextTransformations
    //---------------------------------------------------------------------
    Collection<TextTransformation> convert(List<com.amazonaws.wafv2.webacl.TextTransformation> source);
    List<com.amazonaws.wafv2.webacl.TextTransformation> invert(List<TextTransformation> source);

    //---------------------------------------------------------------------
    // VisibilityConfig
    //---------------------------------------------------------------------
    VisibilityConfig convert(com.amazonaws.wafv2.webacl.VisibilityConfig source);
    com.amazonaws.wafv2.webacl.VisibilityConfig invert(VisibilityConfig source);


    //---------------------------------------------------------------------
    // Default custom conversions for Map<String,Object> to Object
    //---------------------------------------------------------------------

    // convertCount and invertCount is used by OverrideAction
    default CountAction convertCount(Map<String, Object> value) {
        return CountAction.builder().build();
    }

    default Map<String, Object> invertCount(CountAction value) {
        return (value == null) ? null : ImmutableMap.of();
    }

    default NoneAction convertNone(Map<String, Object> value) {
        return NoneAction.builder().build();
    }

    default Map<String, Object> invertNone(NoneAction value) {
        return (value == null) ? null : ImmutableMap.of();
    }

    default SdkBytes convertSearchString(String value) {
        return SdkBytes.fromUtf8String(value);
    }

    default String invertSearchString(SdkBytes value) {
        return value.asUtf8String();
    }

    default QueryString convertQueryString(Map<String, Object> value) {
        return QueryString.builder().build();
    }

    default Map<String, Object> invertQueryString(QueryString value) {
        return (value == null) ? null : ImmutableMap.of();
    }

    default AllQueryArguments convertAllQueryArguments(Map<String, Object> value) {
        return AllQueryArguments.builder().build();
    }

    default Map<String, Object> invertAllQueryArguments(AllQueryArguments value) {
        return (value == null) ? null : ImmutableMap.of();
    }

    default UriPath convertUriPath(Map<String, Object> value) {
        return UriPath.builder().build();
    }

    default Map<String, Object> invertUriPath(UriPath value) {
        return (value == null) ? null : ImmutableMap.of();
    }

    default Body convertBody(Map<String, Object> value) {
        return Body.builder().build();
    }

    default Map<String, Object> invertBody(Body value) {
        return (value == null) ? null : ImmutableMap.of();
    }

    default Method convertMethod(Map<String, Object> value) {
        return Method.builder().build();
    }

    default Map<String, Object> invertMethod(Method value) {
        return (value == null) ? null : ImmutableMap.of();
    }

    default All convertJsonMatchAll(Map<String, Object> value) {
        return All.builder().build();
    }

    default Map<String, Object> invertJsonMatchAll(All value) {
        return (value == null) ? null : ImmutableMap.of();
    }
}
