package com.amazonaws.wafv2.webaclassociation;

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
        String json = "{\"stabilizationRetriesRemaining\":100}";

        CallbackContext context = mapper.reader()
                .forType(CallbackContext.class)
                .readValue(json);

        Assert.assertEquals(100, context.getStabilizationRetriesRemaining());
    }
}
