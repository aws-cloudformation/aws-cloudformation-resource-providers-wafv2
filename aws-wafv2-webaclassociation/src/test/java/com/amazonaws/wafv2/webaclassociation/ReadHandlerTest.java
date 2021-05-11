package com.amazonaws.wafv2.webaclassociation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.GetWebAclForResourceRequest;
import software.amazon.awssdk.services.wafv2.model.GetWebAclForResourceResponse;
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

public class ReadHandlerTest {
    private static final String SOME_OTHER_ARN = "someOtherArn";
    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private ReadHandler readHandler;
    private String webACLARN;
    private String resourceARN;
    private ResourceModel resourceModel;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        webACLARN = "webACLARN";
        resourceARN = "resourceARN";
        readHandler = new ReadHandler(mock(Wafv2Client.class));
        resourceModel = ResourceModel.builder()
                .webACLArn(webACLARN)
                .resourceArn(resourceARN)
                .build();
    }

    @Test
    public void readAssociationForNonExistantEntity() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        doThrow(WafUnavailableEntityException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(GetWebAclForResourceRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response = readHandler.handleRequest(
                proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertNull(response.getResourceModel());
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());

    }

    @Test
    public void readAssociationForWrongCombination() {

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        WebACL webaclResponse = WebACL.builder().arn("").build();
        final GetWebAclForResourceResponse getWebAclForResourceResponse = GetWebAclForResourceResponse.builder().
                webACL(webaclResponse).build();
        doReturn(getWebAclForResourceResponse).when(proxy).injectCredentialsAndInvokeV2(
                any(GetWebAclForResourceRequest.class), any());

        final ProgressEvent<ResourceModel, CallbackContext> response = readHandler.handleRequest(
                proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(HandlerErrorCode.NotFound, response.getErrorCode());
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());

    }

    @Test
    public void readUnavailableEntity() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        doThrow(WafUnavailableEntityException.class).when(proxy).injectCredentialsAndInvokeV2(
                any(GetWebAclForResourceRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response = readHandler.handleRequest(
                proxy, request, null, logger);


        Assert.assertNotNull(response);
        Assert.assertNull(response.getResourceModel());
        Assert.assertEquals(HandlerErrorCode.NotStabilized, response.getErrorCode());
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());

    }

}
