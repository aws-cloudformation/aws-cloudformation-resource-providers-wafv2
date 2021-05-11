package com.amazonaws.wafv2.regexpatternset;

import com.amazonaws.wafv2.regexpatternset.helpers.RegexPatternSetHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.GetRegexPatternSetRequest;
import software.amazon.awssdk.services.wafv2.model.GetRegexPatternSetResponse;
import software.amazon.awssdk.services.wafv2.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.wafv2.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.wafv2.model.RegexPatternSet;
import software.amazon.awssdk.services.wafv2.model.Tag;
import software.amazon.awssdk.services.wafv2.model.TagInfoForResource;
import software.amazon.awssdk.services.wafv2.model.WafLimitsExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class ReadHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private ReadHandler readHandler;
    private RegexPatternSet regexPatternSet;
    private ResourceModel resourceModel;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        readHandler = new ReadHandler(mock(Wafv2Client.class));
        regexPatternSet = RegexPatternSetHelper.getSdkRegexPatternSet();
        resourceModel = RegexPatternSetHelper.getTestResourceModel();
        ListTagsForResourceResponse response = ListTagsForResourceResponse.builder()
                .tagInfoForResource(TagInfoForResource.builder()
                        .tagList(Tag.builder().key("k1").value("v1").build())
                        .build())
                .build();
        doReturn(response).when(proxy).injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());
    }

    @Test
    public void testSuccessfulGetRegexPatternSet() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final GetRegexPatternSetResponse stubResponse = GetRegexPatternSetResponse.builder()
                .regexPatternSet(regexPatternSet)
                .lockToken("someLockToken")
                .build();

        doReturn(stubResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRegexPatternSetRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                readHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResourceModel().getArn());
        Assert.assertNotNull(response.getResourceModel().getDescription());
        Assert.assertNotNull(response.getResourceModel().getId());
        Assert.assertNotNull(response.getResourceModel().getName());
        Assert.assertNotNull(response.getResourceModel().getScope());
        Assert.assertNotNull(response.getResourceModel().getRegularExpressionList());
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getResourceModel());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
        Assert.assertEquals(response.getResourceModel().getTags().size(), 1);
        Assert.assertEquals(response.getResourceModel().getTags().get(0).getKey(), "k1");
        Assert.assertEquals(response.getResourceModel().getTags().get(0).getValue(), "v1");
    }

    @Test
    public void testFailGetWithError() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        doThrow(WafLimitsExceededException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(GetRegexPatternSetRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                readHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());
        Assert.assertEquals(HandlerErrorCode.ServiceLimitExceeded, response.getErrorCode());
        Assert.assertNull(response.getResourceModel());
        Assert.assertNull(response.getCallbackContext());
    }
}
