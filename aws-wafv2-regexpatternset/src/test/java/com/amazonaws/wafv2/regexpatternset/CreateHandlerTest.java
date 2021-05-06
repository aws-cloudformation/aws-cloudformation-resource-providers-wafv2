package com.amazonaws.wafv2.regexpatternset;

import com.amazonaws.wafv2.regexpatternset.helpers.RegexPatternSetHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.CreateRegexPatternSetRequest;
import software.amazon.awssdk.services.wafv2.model.CreateRegexPatternSetResponse;
import software.amazon.awssdk.services.wafv2.model.GetRegexPatternSetRequest;
import software.amazon.awssdk.services.wafv2.model.GetRegexPatternSetResponse;
import software.amazon.awssdk.services.wafv2.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.wafv2.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.wafv2.model.RegexPatternSet;
import software.amazon.awssdk.services.wafv2.model.RegexPatternSetSummary;
import software.amazon.awssdk.services.wafv2.model.Tag;
import software.amazon.awssdk.services.wafv2.model.TagInfoForResource;
import software.amazon.awssdk.services.wafv2.model.WafLimitsExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class CreateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private CreateHandler createHandler;
    private RegexPatternSet regexPatternSet;
    private ResourceModel resourceModel;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        createHandler = new CreateHandler(mock(Wafv2Client.class));
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
    public void testSuccessfulCreateRegexPatternSet() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        final RegexPatternSetSummary stubRegexPatternSetSummary = RegexPatternSetSummary.builder()
                .arn(regexPatternSet.arn())
                .description(regexPatternSet.description())
                .id(regexPatternSet.id())
                .name(regexPatternSet.name())
                .lockToken("dummyLockToken")
                .build();
        final CreateRegexPatternSetResponse stubCreateResponse = CreateRegexPatternSetResponse.builder()
                .summary(stubRegexPatternSetSummary)
                .build();
        final GetRegexPatternSetResponse stubGetResponse = GetRegexPatternSetResponse.builder()
                .regexPatternSet(regexPatternSet)
                .build();

        doReturn(stubCreateResponse).when(proxy).injectCredentialsAndInvokeV2(any(CreateRegexPatternSetRequest.class), any());
        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRegexPatternSetRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                createHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResourceModel());
        Assert.assertNotNull(response.getResourceModel().getArn());
        Assert.assertNotNull(response.getResourceModel().getDescription());
        Assert.assertNotNull(response.getResourceModel().getId());
        Assert.assertNotNull(response.getResourceModel().getName());
        Assert.assertNotNull(response.getResourceModel().getScope());
        Assert.assertNotNull(response.getResourceModel().getRegularExpressionList());
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
        Assert.assertEquals(response.getResourceModel().getTags().size(), 1);
        Assert.assertEquals(response.getResourceModel().getTags().get(0).getKey(), "k1");
        Assert.assertEquals(response.getResourceModel().getTags().get(0).getValue(), "v1");
    }

    @Test
    public void testFailCreateWithNoneStabilizationError() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        doThrow(WafLimitsExceededException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(CreateRegexPatternSetRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                createHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());
        Assert.assertEquals(HandlerErrorCode.ServiceLimitExceeded, response.getErrorCode());
        Assert.assertNull(response.getResourceModel());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testCreateWithEmptyRegexs() {
        resourceModel.setRegularExpressionList(new ArrayList<>());
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        final RegexPatternSetSummary stubRegexPatternSetSummary = RegexPatternSetSummary.builder()
                .arn(regexPatternSet.arn())
                .description(regexPatternSet.description())
                .id(regexPatternSet.id())
                .name(regexPatternSet.name())
                .lockToken("dummyLockToken")
                .build();
        final CreateRegexPatternSetResponse stubCreateResponse = CreateRegexPatternSetResponse.builder()
                .summary(stubRegexPatternSetSummary)
                .build();
        final GetRegexPatternSetResponse stubGetResponse = GetRegexPatternSetResponse.builder()
                .regexPatternSet(regexPatternSet)
                .build();

        doReturn(stubCreateResponse).when(proxy).injectCredentialsAndInvokeV2(any(CreateRegexPatternSetRequest.class), any());
        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRegexPatternSetRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                createHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResourceModel());
        Assert.assertNotNull(response.getResourceModel().getArn());
        Assert.assertNotNull(response.getResourceModel().getDescription());
        Assert.assertNotNull(response.getResourceModel().getId());
        Assert.assertNotNull(response.getResourceModel().getName());
        Assert.assertNotNull(response.getResourceModel().getScope());
        Assert.assertNotNull(response.getResourceModel().getRegularExpressionList());
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }
}
