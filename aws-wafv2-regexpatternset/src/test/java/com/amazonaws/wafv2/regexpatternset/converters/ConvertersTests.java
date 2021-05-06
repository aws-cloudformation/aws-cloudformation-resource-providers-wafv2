package com.amazonaws.wafv2.regexpatternset.converters;


import com.amazonaws.wafv2.regexpatternset.Tag;
import com.amazonaws.wafv2.regexpatternset.helpers.ObjectMapperHelper;
import org.junit.Assert;
import org.junit.Test;

public class ConvertersTests {

    @Test
    public void testTag() {
        Tag tag = ObjectMapperHelper.getObject("test-data/test-tag.json", Tag.class);

        software.amazon.awssdk.services.wafv2.model.Tag sdkTag =
                RegexPatternSetCommonsConverter.INSTANCE.convert(tag);
        Assert.assertEquals(tag.getKey(), sdkTag.key());
        Assert.assertEquals(tag.getValue(), sdkTag.value());

        Tag cloudFormationTag = RegexPatternSetCommonsConverter.INSTANCE.invert(sdkTag);
        Assert.assertEquals(sdkTag.key(), cloudFormationTag.getKey());
        Assert.assertEquals(sdkTag.value(), cloudFormationTag.getValue());
    }
}
