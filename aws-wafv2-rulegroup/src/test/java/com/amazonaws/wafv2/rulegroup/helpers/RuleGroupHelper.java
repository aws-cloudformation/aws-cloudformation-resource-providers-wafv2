package com.amazonaws.wafv2.rulegroup.helpers;

import com.amazonaws.wafv2.rulegroup.CustomResponseBody;
import com.amazonaws.wafv2.rulegroup.LabelSummary;
import com.amazonaws.wafv2.rulegroup.ResourceModel;
import com.amazonaws.wafv2.rulegroup.Rule;
import com.amazonaws.wafv2.rulegroup.VisibilityConfig;
import com.amazonaws.wafv2.rulegroup.converters.Converter;
import com.amazonaws.wafv2.rulegroup.converters.StatementCommonsConverter;
import com.google.common.collect.ImmutableList;
import software.amazon.awssdk.services.wafv2.model.RuleGroup;

import java.io.File;
import java.util.Map;

public class RuleGroupHelper {
    private final static String pathPrefix = new File("aws-wafv2-rulegroup").exists() ? "aws-wafv2-rulegroup/" : "";
    private final static String testArn = "arn:aws:wafv2:us-west-2:123456789012:regional/rulegroup/foo/e1ebfa05-2348-4dfd-a8b4-ddce8b599d30";
    private final static Integer testCapacityInt = new Integer(100);
    private final static Long testCapacityLong = new Long(100);
    private final static String testDescription = "This is a test rulegroup.";
    private final static String testId = "e1ebfa05-2348-4dfd-a8b4-ddce8b599d30";
    private final static String testName = "testRuleGroup";
    private final static Rule testRule = ObjectMapperHelper.getObject(
            pathPrefix + "test-data/test-rule.json", Rule.class);
    private final static Rule testRuleCustomRequest = ObjectMapperHelper.getObject(
        pathPrefix + "test-data/test-rule-custom-request.json", Rule.class);
    private final static Rule testRuleCustomResponse = ObjectMapperHelper.getObject(
        pathPrefix + "test-data/test-rule-custom-response.json", Rule.class);
    private final static Rule testRuleWithRuleLabels = ObjectMapperHelper.getObject(
            pathPrefix + "test-data/test-rule-with-rule-labels.json", Rule.class);
    private final static LabelSummary availableLabel = ObjectMapperHelper.getObject(
            pathPrefix + "test-data/test-available-label.json", LabelSummary.class);
    private final static LabelSummary availableLabelWithColon = ObjectMapperHelper.getObject(
            pathPrefix + "test-data/test-available-label-with-colon.json", LabelSummary.class);
    private final static LabelSummary consumedLabel = ObjectMapperHelper.getObject(
            pathPrefix + "test-data/test-consumed-label.json", LabelSummary.class);
    private final static LabelSummary consumedLabelWithColon = ObjectMapperHelper.getObject(
            pathPrefix + "test-data/test-consumed-label-with-colon.json", LabelSummary.class);

    private final static VisibilityConfig testVisibilityConfig = ObjectMapperHelper.getObject(
            pathPrefix + "test-data/test-visibility-config.json", VisibilityConfig.class);

    private final static Map<String, CustomResponseBody> customResponseBodies =
        ObjectMapperHelper.getObjectForCustomResponseBodyMap("test-data/test-custom-response-bodies.json");

    public static RuleGroup getSdkRuleGroup() {

        return RuleGroup.builder()
                .arn(testArn)
                .capacity(testCapacityLong)
                .description(testDescription)
                .id(testId)
                .name(testName)
                .rules(ImmutableList.of(Converter.INSTANCE.convert(testRule)))
                .visibilityConfig(StatementCommonsConverter.INSTANCE.convert(testVisibilityConfig))
                .build();
    }

    public static RuleGroup getSdkRuleGroupCustomRequestAndResponse() {
        RuleGroup rg = RuleGroup.builder()
            .arn(testArn)
            .capacity(testCapacityLong)
            .description(testDescription)
            .id(testId)
            .name(testName)
            .rules(ImmutableList.of(Converter.INSTANCE.convert(testRule),
                Converter.INSTANCE.convert(testRuleCustomRequest),
                Converter.INSTANCE.convert(testRuleCustomResponse)))
            .visibilityConfig(StatementCommonsConverter.INSTANCE.convert(testVisibilityConfig))
            .customResponseBodies(Converter.INSTANCE.convert(customResponseBodies))
            .build();
        return rg;
    }

    public static RuleGroup getSdkRuleGroupWithRulelabelsWithinRule() {
        return RuleGroup.builder()
                .arn(testArn)
                .capacity(testCapacityLong)
                .description(testDescription)
                .id(testId)
                .name(testName)
                .rules(ImmutableList.of(Converter.INSTANCE.convert(testRule),
                        Converter.INSTANCE.convert(testRuleWithRuleLabels)))
                .visibilityConfig(StatementCommonsConverter.INSTANCE.convert(testVisibilityConfig))
                .customResponseBodies(Converter.INSTANCE.convert(customResponseBodies))
                .build();
    }

    public static RuleGroup getSdkRuleGroupWithAvailableAndConsumedLabels() {
        return RuleGroup.builder()
                .arn(testArn)
                .capacity(testCapacityLong)
                .description(testDescription)
                .id(testId)
                .name(testName)
                .rules(ImmutableList.of(Converter.INSTANCE.convert(testRule)))
                .visibilityConfig(StatementCommonsConverter.INSTANCE.convert(testVisibilityConfig))
                .customResponseBodies(Converter.INSTANCE.convert(customResponseBodies))
                .availableLabels(ImmutableList.of(Converter.INSTANCE.convert(availableLabel),
                        Converter.INSTANCE.convert(availableLabelWithColon)))
                .consumedLabels(ImmutableList.of(Converter.INSTANCE.convert(consumedLabel),
                        Converter.INSTANCE.convert(consumedLabelWithColon)))
                .build();
    }

    public static ResourceModel getTestResourceModel() {
        return ResourceModel.builder()
                .scope("REGIONAL")
                .name(testName)
                .description(testDescription)
                .rules(ImmutableList.of(testRule))
                .capacity(testCapacityInt)
                .build();
    }

    public static ResourceModel getTestResourceModelCustomRequestAndResponse() {
        return ResourceModel.builder()
            .scope("REGIONAL")
            .name(testName)
            .description(testDescription)
            .rules(ImmutableList.of(testRule, testRuleCustomRequest, testRuleCustomResponse))
            .customResponseBodies(customResponseBodies)
            .capacity(testCapacityInt)
            .build();
    }

    public static ResourceModel getTestResourceModelWithRulelabelsWithinRule() {
        return ResourceModel.builder()
                .scope("REGIONAL")
                .name(testName)
                .description(testDescription)
                .rules(ImmutableList.of(testRule, testRuleWithRuleLabels))
                .customResponseBodies(customResponseBodies)
                .capacity(testCapacityInt)
                .build();
    }

    public static ResourceModel getTestResourceModelWithAvailableAndConsumedLabels() {
        return ResourceModel.builder()
                .scope("REGIONAL")
                .name(testName)
                .description(testDescription)
                .rules(ImmutableList.of(testRule, testRuleWithRuleLabels))
                .customResponseBodies(customResponseBodies)
                .capacity(testCapacityInt)
                .availableLabels(ImmutableList.of(availableLabel,availableLabelWithColon))
                .consumedLabels(ImmutableList.of(consumedLabel,consumedLabelWithColon))
                .build();
    }
}
