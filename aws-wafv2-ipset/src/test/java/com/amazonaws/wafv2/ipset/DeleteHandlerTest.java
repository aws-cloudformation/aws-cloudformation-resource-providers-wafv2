package com.amazonaws.wafv2.ipset;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.DeleteIpSetRequest;
import software.amazon.awssdk.services.wafv2.model.DeleteIpSetResponse;
import software.amazon.awssdk.services.wafv2.model.GetIpSetRequest;
import software.amazon.awssdk.services.wafv2.model.GetIpSetResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static com.amazonaws.wafv2.ipset.helpers.IPSetHelper.getReadIPSetResponse;
import static com.amazonaws.wafv2.ipset.helpers.IPSetHelper.id;
import static com.amazonaws.wafv2.ipset.helpers.IPSetHelper.name;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class DeleteHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private DeleteHandler deleteHandler;
    private ResourceModel resourceModel;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        deleteHandler = new DeleteHandler(mock(Wafv2Client.class));
        resourceModel = ResourceModel.builder()
                .name(name)
                .id(id)
                .scope("REGIONAL")
                .build();
    }

    @Test
    public void testSuccessfulDelete() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        doReturn(DeleteIpSetResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteIpSetRequest.class), any());

        final GetIpSetResponse stubResponse = getReadIPSetResponse();

        doReturn(stubResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetIpSetRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext > response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }
}
