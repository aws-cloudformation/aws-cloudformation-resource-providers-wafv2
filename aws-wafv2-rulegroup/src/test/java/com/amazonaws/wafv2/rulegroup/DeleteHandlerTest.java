package com.amazonaws.wafv2.rulegroup;

import com.amazonaws.wafv2.commons.CommonVariables;
import com.amazonaws.wafv2.rulegroup.helpers.RuleGroupHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.DeleteRuleGroupRequest;
import software.amazon.awssdk.services.wafv2.model.DeleteRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.GetRuleGroupRequest;
import software.amazon.awssdk.services.wafv2.model.GetRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.RuleGroup;
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

public class DeleteHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private DeleteHandler deleteHandler;
    private RuleGroup ruleGroup;
    private ResourceModel resourceModel;
    private RuleGroup ruleGroupCustomRequestResponse;
    private RuleGroup ruleGroupWithRuleLabelsWithinRule;
    private RuleGroup ruleGroupWithAvailableAndConsumedLabels;
    private ResourceModel resourceModelCustomRequestResponse;
    private ResourceModel resourceModelWithRuleLabelsWithinRule;
    private ResourceModel resourceModelWithAvailableAndConsumedLabels;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        deleteHandler = new DeleteHandler(mock(Wafv2Client.class));
        ruleGroup = RuleGroupHelper.getSdkRuleGroup();
        resourceModel = RuleGroupHelper.getTestResourceModel();
        ruleGroupCustomRequestResponse = RuleGroupHelper.getSdkRuleGroupCustomRequestAndResponse();
        ruleGroupWithRuleLabelsWithinRule = RuleGroupHelper.getSdkRuleGroupWithRulelabelsWithinRule();
        ruleGroupWithAvailableAndConsumedLabels = RuleGroupHelper.getSdkRuleGroupWithAvailableAndConsumedLabels();
        resourceModelCustomRequestResponse = RuleGroupHelper.getTestResourceModelCustomRequestAndResponse();
        resourceModelWithRuleLabelsWithinRule = RuleGroupHelper.getTestResourceModelWithRulelabelsWithinRule();
        resourceModelWithAvailableAndConsumedLabels = RuleGroupHelper.getTestResourceModelWithAvailableAndConsumedLabels();
    }

    @Test
    public void testSuccessfulDeleteRuleGroup() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroup)
                .build();

        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        doReturn(DeleteRuleGroupResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testSuccessfulDeleteRuleGroupCustomRequestAndResponse() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(resourceModelCustomRequestResponse)
            .build();
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
            .ruleGroup(ruleGroupCustomRequestResponse)
            .build();

        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        doReturn(DeleteRuleGroupResponse.builder().build())
            .when(proxy).injectCredentialsAndInvokeV2(any(DeleteRuleGroupRequest.class), any());
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
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroup)
                .build();

        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        doThrow(WafUnavailableEntityException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteRuleGroupRequest.class), any());
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
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroup)
                .build();

        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        doReturn(DeleteRuleGroupResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteRuleGroupRequest.class), any());
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
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroup)
                .build();

        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        doThrow(WafLimitsExceededException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());
        Assert.assertEquals(HandlerErrorCode.ServiceLimitExceeded, response.getErrorCode());
        Assert.assertNull(response.getResourceModel());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testDeleteRuleGroupWithRuleLabelWithinRule() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModelWithRuleLabelsWithinRule)
                .build();
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroupWithRuleLabelsWithinRule)
                .build();

        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        doReturn(DeleteRuleGroupResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testDeleteRuleGroupWithAvailableAndConsumedLabels() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModelWithAvailableAndConsumedLabels)
                .build();
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroupWithAvailableAndConsumedLabels)
                .build();

        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        doReturn(DeleteRuleGroupResponse.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(DeleteRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }
}
