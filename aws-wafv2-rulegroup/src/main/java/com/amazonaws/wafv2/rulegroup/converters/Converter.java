package com.amazonaws.wafv2.rulegroup.converters;

import org.mapstruct.Mapper;

import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;
import software.amazon.awssdk.services.wafv2.model.LabelSummary;
import software.amazon.awssdk.services.wafv2.model.Rule;
import software.amazon.awssdk.services.wafv2.model.Tag;

import java.util.List;
import java.util.Map;

/**
 * Converter for top level objects such as RuleGroup, Rule.
 */
@Mapper(uses = { StatementCommonsConverter.class, StatementConverter.class },
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface Converter {
    Converter INSTANCE = Mappers.getMapper(Converter.class);

    //---------------------------------------------------------------------
    // Rule
    //---------------------------------------------------------------------
    @Mapping(target = "overrideAction", ignore = true)
    Rule convert(com.amazonaws.wafv2.rulegroup.Rule source);
    com.amazonaws.wafv2.rulegroup.Rule invert(Rule source);

    //---------------------------------------------------------------------
    // Tag
    //---------------------------------------------------------------------
    software.amazon.awssdk.services.wafv2.model.Tag convert(com.amazonaws.wafv2.rulegroup.Tag source);
    com.amazonaws.wafv2.rulegroup.Tag invert(Tag source);

    //---------------------------------------------------------------------
    // Label Summary
    //---------------------------------------------------------------------
    LabelSummary convert(com.amazonaws.wafv2.rulegroup.LabelSummary source);
    com.amazonaws.wafv2.rulegroup.LabelSummary invert(LabelSummary source);

    //---------------------------------------------------------------------
    // Label Summaries
    //---------------------------------------------------------------------
    List<LabelSummary> convert(List<com.amazonaws.wafv2.rulegroup.LabelSummary> source);
    List<com.amazonaws.wafv2.rulegroup.LabelSummary> invert(List<LabelSummary> source);

    //---------------------------------------------------------------------
    // Custom response bodies
    //---------------------------------------------------------------------
    Map<String, software.amazon.awssdk.services.wafv2.model.CustomResponseBody> convert(
        Map<String, com.amazonaws.wafv2.rulegroup.CustomResponseBody> customResponseBodies);

    Map<String, com.amazonaws.wafv2.rulegroup.CustomResponseBody> invert(
        Map<String, software.amazon.awssdk.services.wafv2.model.CustomResponseBody> customResponseBodies);

}
