package com.amazonaws.wafv2.webacl.converters;

import org.mapstruct.Mapper;

import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;
import software.amazon.awssdk.services.wafv2.model.CustomResponseBody;
import software.amazon.awssdk.services.wafv2.model.Rule;
import software.amazon.awssdk.services.wafv2.model.Tag;

import java.util.Map;

/**
 * Converter for top level objects such as WebACL, Rule.
 */
@Mapper(uses = { StatementCommonsConverter.class, StatementConverter.class },
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface Converter {
    Converter INSTANCE = Mappers.getMapper(Converter.class);

    //---------------------------------------------------------------------
    // Rule
    //---------------------------------------------------------------------
    Rule convert(com.amazonaws.wafv2.webacl.Rule source);
    com.amazonaws.wafv2.webacl.Rule invert(Rule source);

    //---------------------------------------------------------------------
    // Tag
    //---------------------------------------------------------------------
    Tag convert(com.amazonaws.wafv2.webacl.Tag source);
    com.amazonaws.wafv2.webacl.Tag invert(Tag source);

    //---------------------------------------------------------------------
    // Custom response bodies
    //---------------------------------------------------------------------
    Map<String, CustomResponseBody> convert(
        Map<String, com.amazonaws.wafv2.webacl.CustomResponseBody> customResponseBodies);

    Map<String, com.amazonaws.wafv2.webacl.CustomResponseBody> invert(
        Map<String, software.amazon.awssdk.services.wafv2.model.CustomResponseBody> customResponseBodies);

}
