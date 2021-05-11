package com.amazonaws.wafv2.regexpatternset;

import com.amazonaws.wafv2.regexpatternset.helpers.RegexPatternSetHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.DeleteRegexPatternSetRequest;
import software.amazon.awssdk.services.wafv2.model.DeleteRegexPatternSetResponse;
import software.amazon.awssdk.services.wafv2.model.GetRegexPatternSetRequest;
import software.amazon.awssdk.services.wafv2.model.GetRegexPatternSetResponse;
import software.amazon.awssdk.services.wafv2.model.RegexPatternSet;
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

public class DeleteHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private DeleteHandler deleteHandler;
    private RegexPatternSet regexPatternSet;
    private ResourceModel resourceModel;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        deleteHandler = new DeleteHandler(mock(Wafv2Client.class));
        regexPatternSet = RegexPatternSetHelper.getSdkRegexPatternSet();
        resourceModel = RegexPatternSetHelper.getTestResourceModel();
    }

    @Test
    public void testSuccessfulDeleteRegexPatternSet() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final GetRegexPatternSetResponse stubGetResponse = GetRegexPatternSetResponse.builder()
                .regexPatternSet(regexPatternSet)
                .build();

        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRegexPatternSetRequest.class), any());
        doReturn(DeleteRegexPatternSetResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteRegexPatternSetRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testFailDeleteWithNoneStabilizationError() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final GetRegexPatternSetResponse stubGetResponse = GetRegexPatternSetResponse.builder()
                .regexPatternSet(regexPatternSet)
                .build();

        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRegexPatternSetRequest.class), any());
        doThrow(WafLimitsExceededException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteRegexPatternSetRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());
        Assert.assertEquals(HandlerErrorCode.ServiceLimitExceeded, response.getErrorCode());
        Assert.assertNull(response.getResourceModel());
        Assert.assertNull(response.getCallbackContext());
    }
}
