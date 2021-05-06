package com.amazonaws.wafv2.webacl.helpers;

import com.amazonaws.wafv2.webacl.CustomResponseBody;
import com.amazonaws.wafv2.webacl.DefaultAction;
import com.amazonaws.wafv2.webacl.ResourceModel;
import com.amazonaws.wafv2.webacl.Rule;
import com.amazonaws.wafv2.webacl.VisibilityConfig;
import com.amazonaws.wafv2.webacl.converters.Converter;
import com.amazonaws.wafv2.webacl.converters.StatementCommonsConverter;
import com.google.common.collect.ImmutableList;
import software.amazon.awssdk.services.wafv2.model.WebACL;

import java.io.File;
import java.util.Map;

public class WebACLHelper {
    private final static String pathPrefix = new File("aws-wafv2-webacl").exists() ? "aws-wafv2-webacl/" : "";
    private final static String testArn = "arn:aws:wafv2:us-west-2:123456789012:regional/webacl/foo/e1ebfa05-2348-4dfd-a8b4-ddce8b599d30";
    private final static Long testCapacity = new Long(100);
    private final static String testDescription = "This is a test web acl.";
    private final static DefaultAction testDefaultAction = ObjectMapperHelper.getObject(
            pathPrefix + "test-data/test-default-action.json", DefaultAction.class);
    private final static DefaultAction testDefaultActionCustomResponse = ObjectMapperHelper.getObject(
        pathPrefix + "test-data/test-default-action-custom-response.json", DefaultAction.class);
    private final static String testId = "e1ebfa05-2348-4dfd-a8b4-ddce8b599d30";
    private final static String testName = "testWebACL";
    private final static Rule testRule = ObjectMapperHelper.getObject(
            pathPrefix + "test-data/test-rule.json", Rule.class);
    private final static Rule testRuleCustomRequest = ObjectMapperHelper.getObject(
        pathPrefix + "test-data/test-rule-custom-request.json", Rule.class);
    private final static Rule testRuleCustomResponse = ObjectMapperHelper.getObject(
        pathPrefix + "test-data/test-rule-custom-response.json", Rule.class);
    private final static Rule testRuleLabels = ObjectMapperHelper.getObject(
            pathPrefix + "test-data/test-rule-with-rule-labels.json", Rule.class);
    private final static VisibilityConfig testVisibilityConfig = ObjectMapperHelper.getObject(
            pathPrefix + "test-data/test-visibility-config.json", VisibilityConfig.class);

    private final static Map<String, CustomResponseBody> customResponseBodies =
        ObjectMapperHelper.getObjectForCustomResponseBodyMap("test-data/test-custom-response-bodies.json");

    public static WebACL getSdkWebACL() {
        return WebACL.builder()
                .arn(testArn)
                .capacity(testCapacity)
                .description(testDescription)
                .defaultAction(StatementCommonsConverter.INSTANCE.convert(testDefaultAction))
                .id(testId)
                .name(testName)
                .rules(ImmutableList.of(Converter.INSTANCE.convert(testRule)))
                .visibilityConfig(StatementCommonsConverter.INSTANCE.convert(testVisibilityConfig))
                .build();
    }

    public static WebACL getSdkWebACLCustomRequestAndResponse() {
        return WebACL.builder()
            .arn(testArn)
            .capacity(testCapacity)
            .description(testDescription)
            .defaultAction(StatementCommonsConverter.INSTANCE.convert(testDefaultActionCustomResponse))
            .id(testId)
            .name(testName)
            .rules(ImmutableList.of(Converter.INSTANCE.convert(testRule),
                Converter.INSTANCE.convert(testRuleCustomRequest),
                Converter.INSTANCE.convert(testRuleCustomResponse)))
            .visibilityConfig(StatementCommonsConverter.INSTANCE.convert(testVisibilityConfig))
            .customResponseBodies(Converter.INSTANCE.convert(customResponseBodies))
            .build();
    }

    public static WebACL getSdkWebACLRuleLabels() {
        return WebACL.builder()
                .arn(testArn)
                .capacity(testCapacity)
                .description(testDescription)
                .defaultAction(StatementCommonsConverter.INSTANCE.convert(testDefaultAction))
                .id(testId)
                .name(testName)
                .rules(ImmutableList.of(Converter.INSTANCE.convert(testRule),
                        Converter.INSTANCE.convert(testRuleLabels)))
                .visibilityConfig(StatementCommonsConverter.INSTANCE.convert(testVisibilityConfig))
                .build();
    }

    public static ResourceModel getTestResourceModel() {
        return ResourceModel.builder()
                .scope("REGIONAL")
                .name(testName)
                .description(testDescription)
                .defaultAction(testDefaultAction)
                .rules(ImmutableList.of(testRule))
                .build();

    }

    public static ResourceModel getTestResourceModelCustomRequestAndResponse() {
        return ResourceModel.builder()
            .scope("REGIONAL")
            .name(testName)
            .description(testDescription)
            .defaultAction(testDefaultActionCustomResponse)
            .rules(ImmutableList.of(testRule, testRuleCustomRequest, testRuleCustomResponse))
            .customResponseBodies(customResponseBodies)
            .build();
    }

    public static ResourceModel getTestResourceModelRuleLabelsWithinRules() {
        return ResourceModel.builder()
                .scope("REGIONAL")
                .name(testName)
                .description(testDescription)
                .defaultAction(testDefaultAction)
                .rules(ImmutableList.of(testRule, testRuleLabels))
                .build();
    }
}
