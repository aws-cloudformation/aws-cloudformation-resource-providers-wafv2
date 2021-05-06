package com.amazonaws.wafv2.webaclassociation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import static org.mockito.Mockito.mock;

public class UpdateHandlerTest {
    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private UpdateHandler updateHandler;
    private String webACLARN;
    private String resourceARN;
    private ResourceModel resourceModel;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        webACLARN = "webACLARN";
        resourceARN = "resourceARN";
        updateHandler = new UpdateHandler(mock(Wafv2Client.class));
        resourceModel = ResourceModel.builder()
                .webACLArn(webACLARN)
                .resourceArn(resourceARN)
                .build();
    }

    @Test
    public void testSuccessfulAssociateWebACLWithCallbackContext() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final CallbackContext callbackContext = CallbackContext.builder()
                .stabilizationRetriesRemaining(1)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                updateHandler.handleRequest(proxy, request, callbackContext, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getResourceModel());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }
}
