package com.amazonaws.wafv2.regexpatternset.helpers;


import com.amazonaws.wafv2.regexpatternset.ResourceModel;
import software.amazon.awssdk.services.wafv2.model.Regex;
import software.amazon.awssdk.services.wafv2.model.RegexPatternSet;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RegexPatternSetHelper {
    private final static String testArn = "arn:aws:wafv2:us-west-2:123456789012:regional/regexpatternset/foo/e1ebfa05-2348-4dfd-a8b4-ddce8b599d30";
    private final static String testDescription = "This is a test regexpatternset.";
    private final static String testId = "e1ebfa05-2348-4dfd-a8b4-ddce8b599d30";
    private final static String testName = "testRegexPatternSet";
    private final static String[] regularExpressionArray = {"^foo", "bar$" };
    private final static List<String> testregExList = Arrays.asList(regularExpressionArray);


    public static RegexPatternSet getSdkRegexPatternSet() {
        List<Regex> val= testregExList.stream().map(regex -> Regex.builder().regexString(regex).build())
                .collect(Collectors.toList());
        return RegexPatternSet.builder()
                .arn(testArn)
                .description(testDescription)
                .id(testId)
                .name(testName)
                .regularExpressionList(val)
                .build();
    }

    public static ResourceModel getTestResourceModel() {
        return ResourceModel.builder()
                .scope("REGIONAL")
                .name(testName)
                .description(testDescription)
                .regularExpressionList(testregExList)
                .build();

    }
}
