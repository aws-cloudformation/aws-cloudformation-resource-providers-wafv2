package com.amazonaws.wafv2.ipset;

import com.amazonaws.wafv2.ipset.helpers.IPSetHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.CreateIpSetRequest;
import software.amazon.awssdk.services.wafv2.model.CreateIpSetResponse;
import software.amazon.awssdk.services.wafv2.model.GetIpSetRequest;
import software.amazon.awssdk.services.wafv2.model.GetIpSetResponse;
import software.amazon.awssdk.services.wafv2.model.IPAddressVersion;
import software.amazon.awssdk.services.wafv2.model.IPSetSummary;
import software.amazon.awssdk.services.wafv2.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.wafv2.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.wafv2.model.Tag;
import software.amazon.awssdk.services.wafv2.model.TagInfoForResource;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Arrays;

import static com.amazonaws.wafv2.ipset.helpers.IPSetHelper.address;
import static com.amazonaws.wafv2.ipset.helpers.IPSetHelper.arn;
import static com.amazonaws.wafv2.ipset.helpers.IPSetHelper.description;
import static com.amazonaws.wafv2.ipset.helpers.IPSetHelper.id;
import static com.amazonaws.wafv2.ipset.helpers.IPSetHelper.ipAddressVersion;
import static com.amazonaws.wafv2.ipset.helpers.IPSetHelper.name;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class CreateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private Wafv2Client client;

    private CreateHandler handler;
    private ResourceModel model;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new CreateHandler(mock(Wafv2Client.class));
        model = ResourceModel.builder()
                .name(name)
                .scope("REGIONAL")
                .description(description)
                .iPAddressVersion(ipAddressVersion)
                .addresses(Arrays.asList(address))
                .build();
        ListTagsForResourceResponse response = ListTagsForResourceResponse.builder()
                .tagInfoForResource(TagInfoForResource.builder()
                        .tagList(Tag.builder().key("k1").value("v1").build())
                        .build())
                .build();
        doReturn(response).when(proxy).injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());
    }

    @Test
    public void testSuccessfulCreate() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model).build();
        final IPSetSummary summary = IPSetSummary.builder()
                .arn(arn)
                .description(description)
                .id(id)
                .name(name)
                .build();
        final CreateIpSetResponse response = CreateIpSetResponse.builder().summary(summary).build();

        final GetIpSetResponse getIpSetResponse = IPSetHelper.getReadIPSetResponse();


        doReturn(response).when(proxy).injectCredentialsAndInvokeV2(any(CreateIpSetRequest.class), any());
        doReturn(getIpSetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetIpSetRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> createResponse =
                handler.handleRequest(proxy, request, null, logger);

        assertNotNull(createResponse);
        assertNotNull(createResponse.getResourceModel());
        assertEquals(IPAddressVersion.IPV4.toString(), createResponse.getResourceModel().getIPAddressVersion());
        assertEquals(OperationStatus.SUCCESS, createResponse.getStatus());
        assertEquals(0, createResponse.getCallbackDelaySeconds());
        assertNull(createResponse.getErrorCode());
        assertNull(createResponse.getCallbackContext());
        assertNotNull(createResponse.getResourceModel().getAddresses());
        Assert.assertEquals(createResponse.getResourceModel().getTags().size(), 1);
        Assert.assertEquals(createResponse.getResourceModel().getTags().get(0).getKey(), "k1");
        Assert.assertEquals(createResponse.getResourceModel().getTags().get(0).getValue(), "v1");
    }

}
