package com.amazonaws.wafv2.webacl;

import com.amazonaws.wafv2.commons.CommonVariables;
import com.amazonaws.wafv2.webacl.helpers.WebACLHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.DeleteWebAclRequest;
import software.amazon.awssdk.services.wafv2.model.DeleteWebAclResponse;
import software.amazon.awssdk.services.wafv2.model.GetWebAclRequest;
import software.amazon.awssdk.services.wafv2.model.GetWebAclResponse;
import software.amazon.awssdk.services.wafv2.model.WafLimitsExceededException;
import software.amazon.awssdk.services.wafv2.model.WafUnavailableEntityException;
import software.amazon.awssdk.services.wafv2.model.WebACL;
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

public class DeleteHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private DeleteHandler deleteHandler;
    private WebACL webACL;
    private ResourceModel resourceModel;
    private WebACL webACLCustomRequestResponse;
    private WebACL webACLWithRuleLabelsWithinRules;
    private ResourceModel resourceModelCustomRequestResponse;
    private ResourceModel resourceModelWithRuleLabelsWithinRules;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        deleteHandler = new DeleteHandler(mock(Wafv2Client.class));
        webACL = WebACLHelper.getSdkWebACL();
        resourceModel = WebACLHelper.getTestResourceModel();
        webACLCustomRequestResponse = WebACLHelper.getSdkWebACLCustomRequestAndResponse();
        webACLWithRuleLabelsWithinRules = WebACLHelper.getSdkWebACLRuleLabels();
        resourceModelCustomRequestResponse = WebACLHelper.getTestResourceModelCustomRequestAndResponse();
        resourceModelWithRuleLabelsWithinRules = WebACLHelper.getTestResourceModelRuleLabelsWithinRules();
    }

    @Test
    public void testSuccessfulDeleteWebACL() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final GetWebAclResponse stubGetResponse = GetWebAclResponse.builder()
                .webACL(webACL)
                .build();

        doReturn(stubGetResponse)
                .when(proxy).injectCredentialsAndInvokeV2(any(GetWebAclRequest.class), any());
        doReturn(DeleteWebAclResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteWebAclRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testSuccessfulDeleteWebACLCustomRequestAndResponse() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(resourceModelCustomRequestResponse)
            .build();
        final GetWebAclResponse stubGetResponse = GetWebAclResponse.builder()
            .webACL(webACLCustomRequestResponse)
            .build();

        doReturn(stubGetResponse)
            .when(proxy).injectCredentialsAndInvokeV2(any(GetWebAclRequest.class), any());
        doReturn(DeleteWebAclResponse.builder().build())
            .when(proxy).injectCredentialsAndInvokeV2(any(DeleteWebAclRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
            deleteHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testDeleteWithStabilization() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final GetWebAclResponse stubGetResponse = GetWebAclResponse.builder()
                .webACL(webACL)
                .build();

        doReturn(stubGetResponse)
                .when(proxy).injectCredentialsAndInvokeV2(any(GetWebAclRequest.class), any());
        doThrow(WafUnavailableEntityException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteWebAclRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getCallbackContext());
        Assert.assertEquals(OperationStatus.IN_PROGRESS, response.getStatus());
        Assert.assertEquals(CommonVariables.CALLBACK_DELAY_SECONDS, response.getCallbackDelaySeconds());
    }

    @Test
    public void testSuccessfulDeletionWithCallbackContext() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final CallbackContext callbackContext = CallbackContext.builder()
                .stabilizationRetriesRemaining(1)
                .build();
        final GetWebAclResponse stubGetResponse = GetWebAclResponse.builder()
                .webACL(webACL)
                .build();

        doReturn(stubGetResponse)
                .when(proxy).injectCredentialsAndInvokeV2(any(GetWebAclRequest.class), any());
        doReturn(DeleteWebAclResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteWebAclRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, callbackContext, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testFailDeleteWithRetryExceeded() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final CallbackContext callbackContext = CallbackContext.builder()
                .stabilizationRetriesRemaining(0)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, callbackContext, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());
        Assert.assertEquals(HandlerErrorCode.NotStabilized, response.getErrorCode());
        Assert.assertNull(response.getResourceModel());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testFailDeleteWithNoneStabilizationError() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final GetWebAclResponse stubGetResponse = GetWebAclResponse.builder()
                .webACL(webACL)
                .build();

        doReturn(stubGetResponse)
                .when(proxy).injectCredentialsAndInvokeV2(any(GetWebAclRequest.class), any());
        doThrow(WafLimitsExceededException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteWebAclRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());
        Assert.assertEquals(HandlerErrorCode.ServiceLimitExceeded, response.getErrorCode());
        Assert.assertNull(response.getResourceModel());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testDeleteWebACLWithRuleLabelsWithinRules() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModelWithRuleLabelsWithinRules)
                .build();
        final GetWebAclResponse stubGetResponse = GetWebAclResponse.builder()
                .webACL(webACLWithRuleLabelsWithinRules)
                .build();

        doReturn(stubGetResponse)
                .when(proxy).injectCredentialsAndInvokeV2(any(GetWebAclRequest.class), any());
        doReturn(DeleteWebAclResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteWebAclRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }
}
