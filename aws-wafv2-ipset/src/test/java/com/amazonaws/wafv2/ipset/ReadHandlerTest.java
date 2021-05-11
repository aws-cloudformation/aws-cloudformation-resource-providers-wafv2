package com.amazonaws.wafv2.ipset;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.GetIpSetRequest;
import software.amazon.awssdk.services.wafv2.model.GetIpSetResponse;
import software.amazon.awssdk.services.wafv2.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.wafv2.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.wafv2.model.Tag;
import software.amazon.awssdk.services.wafv2.model.TagInfoForResource;
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

public class ReadHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;


    private ReadHandler handler;
    private ResourceModel model;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new ReadHandler(mock(Wafv2Client.class));
        model = ResourceModel.builder()
                .scope("REGIONAL")
                .name(name)
                .id(id)
                .build();
        ListTagsForResourceResponse response = ListTagsForResourceResponse.builder()
                .tagInfoForResource(TagInfoForResource.builder()
                        .tagList(Tag.builder().key("k1").value("v1").build())
                        .build())
                .build();
        doReturn(response).when(proxy).injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        final GetIpSetResponse stubResponse = getReadIPSetResponse();

        doReturn(stubResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetIpSetRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResourceModel().getIPAddressVersion());
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getResourceModel());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
        Assert.assertNotNull(response.getResourceModel().getAddresses());
        Assert.assertEquals(response.getResourceModel().getTags().size(), 1);
        Assert.assertEquals(response.getResourceModel().getTags().get(0).getKey(), "k1");
        Assert.assertEquals(response.getResourceModel().getTags().get(0).getValue(), "v1");
    }
}
