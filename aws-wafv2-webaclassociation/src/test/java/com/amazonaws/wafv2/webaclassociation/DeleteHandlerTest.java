package com.amazonaws.wafv2.webaclassociation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.internal.verification.Times;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.DisassociateWebAclRequest;
import software.amazon.awssdk.services.wafv2.model.DisassociateWebAclResponse;
import software.amazon.awssdk.services.wafv2.model.GetWebAclForResourceRequest;
import software.amazon.awssdk.services.wafv2.model.GetWebAclForResourceResponse;
import software.amazon.awssdk.services.wafv2.model.WafLimitsExceededException;
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
import static org.mockito.Mockito.verify;

public class DeleteHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private DeleteHandler deleteHandler;
    private String webACLARN;
    private String resourceARN;
    private ResourceModel resourceModel;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        webACLARN = "webACLARN";
        resourceARN = "resourceARN";
        deleteHandler = new DeleteHandler(mock(Wafv2Client.class));
        resourceModel = ResourceModel.builder()
                .webACLArn(webACLARN)
                .resourceArn(resourceARN)
                .build();
    }

    @Test
    public void testSuccessfulDisassociateWebACL() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        WebACL webACL = WebACL.builder().arn(webACLARN).build();
        doReturn(GetWebAclForResourceResponse.builder().webACL(webACL).build())
                .when(proxy).injectCredentialsAndInvokeV2(any(GetWebAclForResourceRequest.class), any());
        doReturn(DisassociateWebAclResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DisassociateWebAclRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        verify(proxy, new Times(1)).injectCredentialsAndInvokeV2(any(DisassociateWebAclRequest.class), any());
        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testFailDisassociateWebACLWithNoneStabilizationError() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        WebACL webACL = WebACL.builder().arn(webACLARN).build();
        doReturn(GetWebAclForResourceResponse.builder().webACL(webACL).build())
                .when(proxy).injectCredentialsAndInvokeV2(any(GetWebAclForResourceRequest.class), any());
        doThrow(WafLimitsExceededException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DisassociateWebAclRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());
        Assert.assertEquals(HandlerErrorCode.ServiceLimitExceeded, response.getErrorCode());
        Assert.assertNull(response.getResourceModel());
        Assert.assertNull(response.getCallbackContext());
    }


    @Test
    public void testDisassociateWebACLWithNonMatchingWebACLARN() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        WebACL webACL = WebACL.builder().arn("anotherARN").build();
        doReturn(GetWebAclForResourceResponse.builder().webACL(webACL).build())
                .when(proxy).injectCredentialsAndInvokeV2(any(GetWebAclForResourceRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        verify(proxy, new Times(0)).injectCredentialsAndInvokeV2(any(DisassociateWebAclRequest.class), any());
        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testDisassociateWebACLWithNoWebACL() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        doReturn(GetWebAclForResourceResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(GetWebAclForResourceRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        verify(proxy, new Times(0)).injectCredentialsAndInvokeV2(any(DisassociateWebAclRequest.class), any());
        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }
}
