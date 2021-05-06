package com.amazonaws.wafv2.webaclassociation;

import com.amazonaws.wafv2.commons.CommonVariables;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.AssociateWebAclRequest;
import software.amazon.awssdk.services.wafv2.model.AssociateWebAclResponse;
import software.amazon.awssdk.services.wafv2.model.WafLimitsExceededException;
import software.amazon.awssdk.services.wafv2.model.WafUnavailableEntityException;
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

public class CreateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private CreateHandler createHandler;
    private String webACLARN;
    private String resourceARN;
    private ResourceModel resourceModel;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        webACLARN = "webACLARN";
        resourceARN = "resourceARN";
        createHandler = new CreateHandler(mock(Wafv2Client.class));
        resourceModel = ResourceModel.builder()
                .webACLArn(webACLARN)
                .resourceArn(resourceARN)
                .build();
    }

    @Test
    public void testSuccessfulAssociateWebACL() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        doReturn(AssociateWebAclResponse.builder().build()).when(proxy).injectCredentialsAndInvokeV2(any(AssociateWebAclRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                createHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getResourceModel());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testAssociateWebACLWithStabilization() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        doThrow(WafUnavailableEntityException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(AssociateWebAclRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                createHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getCallbackContext());
        Assert.assertEquals(OperationStatus.IN_PROGRESS, response.getStatus());
        Assert.assertEquals(CommonVariables.CALLBACK_DELAY_SECONDS, response.getCallbackDelaySeconds());
    }

    @Test
    public void testSuccessfulAssociateWebACLWithCallbackContext() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final CallbackContext callbackContext = CallbackContext.builder()
                .stabilizationRetriesRemaining(1)
                .build();

        doReturn(AssociateWebAclResponse.builder().build()).when(proxy).injectCredentialsAndInvokeV2(any(AssociateWebAclRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                createHandler.handleRequest(proxy, request, callbackContext, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getResourceModel());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testFailAssociateWebACLWithRetryExceeded() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final CallbackContext callbackContext = CallbackContext.builder()
                .stabilizationRetriesRemaining(0)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                createHandler.handleRequest(proxy, request, callbackContext, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());
        Assert.assertEquals(HandlerErrorCode.NotStabilized, response.getErrorCode());
        Assert.assertNull(response.getResourceModel());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testFailAssociateWebACLWithNoneStabilizationError() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        doThrow(WafLimitsExceededException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(AssociateWebAclRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                createHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());
        Assert.assertEquals(HandlerErrorCode.ServiceLimitExceeded, response.getErrorCode());
        Assert.assertNull(response.getResourceModel());
        Assert.assertNull(response.getCallbackContext());
    }
}
