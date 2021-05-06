package com.amazonaws.wafv2.webacl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Serialization test for {@link CallbackContext}
 */
public class CallbackContextSerializationTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void deserializeCallbackContext() throws IOException {
        String json = "{\"stabilizationRetriesRemaining\":50, \"id\":\"id\", \"name\":\"test\", \"lockToken\":\"foo\"}";

        CallbackContext context = mapper.reader()
                .forType(CallbackContext.class)
                .readValue(json);

        Assert.assertEquals(50, context.getStabilizationRetriesRemaining());
        Assert.assertEquals("id", context.getId());
        Assert.assertEquals("test", context.getName());
        Assert.assertEquals("foo", context.getLockToken());
    }
}
