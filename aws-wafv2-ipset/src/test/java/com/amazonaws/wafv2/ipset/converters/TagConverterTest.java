package com.amazonaws.wafv2.ipset.converters;

import com.amazonaws.wafv2.ipset.Tag;
import com.amazonaws.wafv2.ipset.helpers.ObjectMapperHelper;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TagConverterTest {


    @Test
    public void testOneValidTag() {
        Tag tag = ObjectMapperHelper.getObject("test-data/one-valid-tag.json", Tag.class);
        software.amazon.awssdk.services.wafv2.model.Tag sdkTag = TagConverter.INSTANCE.convert(tag);
        Assert.assertNotNull(sdkTag);
        Assert.assertEquals(tag.getKey(), sdkTag.key());
        Assert.assertEquals(tag.getValue(), sdkTag.value());

    }
}
