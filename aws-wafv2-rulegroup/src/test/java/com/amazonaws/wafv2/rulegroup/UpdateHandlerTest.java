package com.amazonaws.wafv2.rulegroup;

import com.amazonaws.wafv2.commons.CommonVariables;
import com.amazonaws.wafv2.rulegroup.helpers.RuleGroupHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.GetRuleGroupRequest;
import software.amazon.awssdk.services.wafv2.model.GetRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.wafv2.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.wafv2.model.RuleGroup;
import software.amazon.awssdk.services.wafv2.model.Tag;
import software.amazon.awssdk.services.wafv2.model.TagInfoForResource;
import software.amazon.awssdk.services.wafv2.model.UpdateRuleGroupRequest;
import software.amazon.awssdk.services.wafv2.model.UpdateRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.WafLimitsExceededException;
import software.amazon.awssdk.services.wafv2.model.WafUnavailableEntityException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class UpdateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private UpdateHandler updateHandler;
    private RuleGroup ruleGroup;
    private ResourceModel resourceModel;
    private RuleGroup ruleGroupCustomRequestResponse;
    private RuleGroup ruleGroupWithRuleLabelsWithinRule;
    private ResourceModel resourceModelCustomRequestResponse;
    private ResourceModel resourceModelWithRuleLabelsWithinRule;

    @Before
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        updateHandler = new UpdateHandler(mock(Wafv2Client.class));
        ruleGroup = RuleGroupHelper.getSdkRuleGroup();
        resourceModel = RuleGroupHelper.getTestResourceModel();
        ruleGroupCustomRequestResponse = RuleGroupHelper.getSdkRuleGroupCustomRequestAndResponse();
        ruleGroupWithRuleLabelsWithinRule = RuleGroupHelper.getSdkRuleGroupWithRulelabelsWithinRule();
        resourceModelCustomRequestResponse = RuleGroupHelper.getTestResourceModelCustomRequestAndResponse();
        resourceModelWithRuleLabelsWithinRule = RuleGroupHelper.getTestResourceModelWithRulelabelsWithinRule();
        ListTagsForResourceResponse response = ListTagsForResourceResponse.builder()
                .tagInfoForResource(TagInfoForResource.builder()
                        .tagList(Tag.builder().key("k1").value("v1").build())
                        .build())
                .build();
        doReturn(response).when(proxy).injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());
    }

    @Test
    public void testSuccessfulUpdateRuleGroup() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        final UpdateRuleGroupResponse stubCreateResponse = UpdateRuleGroupResponse.builder()
                .nextLockToken("dummyLockToken")
                .build();
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroup)
                .build();

        doReturn(stubCreateResponse).when(proxy).injectCredentialsAndInvokeV2(any(UpdateRuleGroupRequest.class), any());
        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                updateHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResourceModel());
        Assert.assertNotNull(response.getResourceModel().getArn());
        Assert.assertNotNull(response.getResourceModel().getDescription());
        Assert.assertNotNull(response.getResourceModel().getId());
        Assert.assertNotNull(response.getResourceModel().getName());
        Assert.assertNotNull(response.getResourceModel().getScope());
        Assert.assertNotNull(response.getResourceModel().getCapacity());
        Assert.assertNotNull(response.getResourceModel().getRules());
        Assert.assertNotNull(response.getResourceModel().getVisibilityConfig());
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
        Assert.assertEquals(response.getResourceModel().getTags().size(), 1);
        Assert.assertEquals(response.getResourceModel().getTags().get(0).getKey(), "k1");
        Assert.assertEquals(response.getResourceModel().getTags().get(0).getValue(), "v1");
    }

    @Test
    public void testSuccessfulUpdateRuleGroupCustomRequestAndResponse() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(resourceModelCustomRequestResponse)
            .build();

        final UpdateRuleGroupResponse stubCreateResponse = UpdateRuleGroupResponse.builder()
            .nextLockToken("dummyLockToken")
            .build();
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
            .ruleGroup(ruleGroupCustomRequestResponse)
            .build();

        doReturn(stubCreateResponse).when(proxy).injectCredentialsAndInvokeV2(any(UpdateRuleGroupRequest.class), any());
        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
            updateHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getResourceModel());
        Assert.assertNotNull(response.getResourceModel().getArn());
        Assert.assertNotNull(response.getResourceModel().getDescription());
        Assert.assertNotNull(response.getResourceModel().getId());
        Assert.assertNotNull(response.getResourceModel().getName());
        Assert.assertNotNull(response.getResourceModel().getScope());
        Assert.assertNotNull(response.getResourceModel().getCapacity());
        Assert.assertNotNull(response.getResourceModel().getVisibilityConfig());

        // Verify rules
        Assert.assertNotNull(response.getResourceModel().getRules());
        Assert.assertEquals(3, response.getResourceModel().getRules().size());
        RuleAction ruleActionCustomRequest = response.getResourceModel().getRules().get(1).getAction();
        Assert.assertNull(ruleActionCustomRequest.getAllow());
        Assert.assertNull(ruleActionCustomRequest.getBlock());
        Assert.assertNotNull(ruleActionCustomRequest.getCount());
        Assert.assertNotNull(ruleActionCustomRequest.getCount().getCustomRequestHandling());
        Assert.assertEquals(2, ruleActionCustomRequest.getCount().getCustomRequestHandling().getInsertHeaders().size());
        Assert.assertEquals("ruleCountActionHeader1Name", ruleActionCustomRequest.getCount().getCustomRequestHandling().getInsertHeaders().get(0).getName());
        Assert.assertEquals("ruleCountActionHeader1Value", ruleActionCustomRequest.getCount().getCustomRequestHandling().getInsertHeaders().get(0).getValue());
        Assert.assertEquals("ruleCountActionHeader2Name", ruleActionCustomRequest.getCount().getCustomRequestHandling().getInsertHeaders().get(1).getName());
        Assert.assertEquals("ruleCountActionHeader2Value", ruleActionCustomRequest.getCount().getCustomRequestHandling().getInsertHeaders().get(1).getValue());
        RuleAction ruleActionCustomResponse = response.getResourceModel().getRules().get(2).getAction();
        Assert.assertNull(ruleActionCustomResponse.getAllow());
        Assert.assertNotNull(ruleActionCustomResponse.getBlock());
        Assert.assertNull(ruleActionCustomResponse.getCount());
        Assert.assertNotNull(ruleActionCustomResponse.getBlock().getCustomResponse());
        Assert.assertEquals(503, ruleActionCustomResponse.getBlock().getCustomResponse().getResponseCode().intValue());
        Assert.assertEquals("CustomResponseBodyKey1", ruleActionCustomResponse.getBlock().getCustomResponse().getCustomResponseBodyKey());
        Assert.assertEquals(2, ruleActionCustomResponse.getBlock().getCustomResponse().getResponseHeaders().size());
        Assert.assertEquals("ruleBlockActionHeader1Name", ruleActionCustomResponse.getBlock().getCustomResponse().getResponseHeaders().get(0).getName());
        Assert.assertEquals("ruleBlockActionHeader1Value", ruleActionCustomResponse.getBlock().getCustomResponse().getResponseHeaders().get(0).getValue());
        Assert.assertEquals("ruleBlockActionHeader2Name", ruleActionCustomResponse.getBlock().getCustomResponse().getResponseHeaders().get(1).getName());
        Assert.assertEquals("ruleBlockActionHeader2Value", ruleActionCustomResponse.getBlock().getCustomResponse().getResponseHeaders().get(1).getValue());

        // Verify custom response bodies
        Assert.assertNotNull(response.getResourceModel().getCustomResponseBodies());
        Map<String, CustomResponseBody> customResponseBodies = response.getResourceModel().getCustomResponseBodies();
        Assert.assertTrue(customResponseBodies.containsKey("CustomResponseBodyKey1"));
        CustomResponseBody customResponseBody1 = customResponseBodies.get("CustomResponseBodyKey1");
        Assert.assertEquals("TEXT_PLAIN", customResponseBody1.getContentType());
        Assert.assertEquals("this is a plain text", customResponseBody1.getContent());
        Assert.assertTrue(customResponseBodies.containsKey("CustomResponseBodyKey2"));
        CustomResponseBody customResponseBody2 = customResponseBodies.get("CustomResponseBodyKey2");
        Assert.assertEquals("APPLICATION_JSON", customResponseBody2.getContentType());
        Assert.assertEquals("{\"jsonfieldname\": \"jsonfieldvalue\"}", customResponseBody2.getContent());
        CustomResponseBody customResponseBody3 = customResponseBodies.get("CustomResponseBodyKey3");
        Assert.assertEquals("TEXT_HTML", customResponseBody3.getContentType());
        Assert.assertEquals("<html>HTML text content<html>", customResponseBody3.getContent());

        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
        Assert.assertEquals(response.getResourceModel().getTags().size(), 1);
        Assert.assertEquals(response.getResourceModel().getTags().get(0).getKey(), "k1");
        Assert.assertEquals(response.getResourceModel().getTags().get(0).getValue(), "v1");
    }

    @Test
    public void testUpdateWithStabilization() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroup)
                .build();

        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        doThrow(WafUnavailableEntityException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(UpdateRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                updateHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getCallbackContext());
        Assert.assertEquals(OperationStatus.IN_PROGRESS, response.getStatus());
        Assert.assertEquals(CommonVariables.CALLBACK_DELAY_SECONDS, response.getCallbackDelaySeconds());
    }

    @Test
    public void testSuccessfulUpdateWithCallbackContext() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final CallbackContext callbackContext = CallbackContext.builder()
                .stabilizationRetriesRemaining(1)
                .build();

        final UpdateRuleGroupResponse stubCreateResponse = UpdateRuleGroupResponse.builder()
                .nextLockToken("dummyLockToken")
                .build();
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroup)
                .build();

        doReturn(stubCreateResponse).when(proxy).injectCredentialsAndInvokeV2(any(UpdateRuleGroupRequest.class), any());
        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                updateHandler.handleRequest(proxy, request, callbackContext, logger);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResourceModel());
        Assert.assertNotNull(response.getResourceModel().getArn());
        Assert.assertNotNull(response.getResourceModel().getDescription());
        Assert.assertNotNull(response.getResourceModel().getId());
        Assert.assertNotNull(response.getResourceModel().getName());
        Assert.assertNotNull(response.getResourceModel().getScope());
        Assert.assertNotNull(response.getResourceModel().getCapacity());
        Assert.assertNotNull(response.getResourceModel().getRules());
        Assert.assertNotNull(response.getResourceModel().getVisibilityConfig());
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testFailUpdateWithRetryExceeded() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final CallbackContext callbackContext = CallbackContext.builder()
                .stabilizationRetriesRemaining(0)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                updateHandler.handleRequest(proxy, request, callbackContext, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());
        Assert.assertEquals(HandlerErrorCode.NotStabilized, response.getErrorCode());
        Assert.assertNull(response.getResourceModel());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testFailUpdateWithNoneStabilizationError() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroup)
                .build();

        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        doThrow(WafLimitsExceededException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(UpdateRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                updateHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());
        Assert.assertEquals(HandlerErrorCode.ServiceLimitExceeded, response.getErrorCode());
        Assert.assertNull(response.getResourceModel());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testUpdateWithNullRules() {
        resourceModel.setRules(null);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        final UpdateRuleGroupResponse stubCreateResponse = UpdateRuleGroupResponse.builder()
                .nextLockToken("dummyLockToken")
                .build();
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroup)
                .build();

        doReturn(stubCreateResponse).when(proxy).injectCredentialsAndInvokeV2(any(UpdateRuleGroupRequest.class), any());
        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                updateHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResourceModel());
        Assert.assertNotNull(response.getResourceModel().getArn());
        Assert.assertNotNull(response.getResourceModel().getDescription());
        Assert.assertNotNull(response.getResourceModel().getId());
        Assert.assertNotNull(response.getResourceModel().getName());
        Assert.assertNotNull(response.getResourceModel().getScope());
        Assert.assertNotNull(response.getResourceModel().getCapacity());
        Assert.assertNotNull(response.getResourceModel().getRules());
        Assert.assertNotNull(response.getResourceModel().getVisibilityConfig());
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertEquals(0, response.getCallbackDelaySeconds());
        Assert.assertNull(response.getErrorCode());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testUpdateRuleGroupWithRuleLabelWithinRule() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModelWithRuleLabelsWithinRule)
                .build();

        final UpdateRuleGroupResponse stubCreateResponse = UpdateRuleGroupResponse.builder()
                .nextLockToken("dummyLockToken")
                .build();
        final GetRuleGroupResponse stubGetResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroupWithRuleLabelsWithinRule)
                .build();

        doReturn(stubCreateResponse).when(proxy).injectCredentialsAndInvokeV2(any(UpdateRuleGroupRequest.class), any());
        doReturn(stubGetResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                updateHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.SUCCESS, response.getStatus());
        Assert.assertNotNull(response.getResourceModel());
        Assert.assertNotNull(response.getResourceModel().getArn());
        Assert.assertNotNull(response.getResourceModel().getDescription());
        Assert.assertNotNull(response.getResourceModel().getId());
        Assert.assertNotNull(response.getResourceModel().getName());
        Assert.assertNotNull(response.getResourceModel().getScope());
        Assert.assertNotNull(response.getResourceModel().getCapacity());
        Assert.assertNotNull(response.getResourceModel().getVisibilityConfig());

        // Verify rules
        Assert.assertNotNull(response.getResourceModel().getRules());
        Assert.assertEquals(2, response.getResourceModel().getRules().size());
        Rule ruleWithRuleLabels = response.getResourceModel().getRules().get(1);
        Assert.assertNotNull(ruleWithRuleLabels.getAction().getBlock());
        Assert.assertNull(ruleWithRuleLabels.getAction().getAllow());
        Assert.assertNull(ruleWithRuleLabels.getAction().getCount());
        Assert.assertNotNull(ruleWithRuleLabels.getRuleLabels());
        Assert.assertEquals(2, ruleWithRuleLabels.getRuleLabels().size());
        Assert.assertNotNull(ruleWithRuleLabels.getRuleLabels().get(0).getName());
        Assert.assertEquals("testRuleLabel1", ruleWithRuleLabels.getRuleLabels().get(0).getName());
        Assert.assertNotNull(ruleWithRuleLabels.getRuleLabels().get(1).getName());
        Assert.assertEquals("testRuleLabel2", ruleWithRuleLabels.getRuleLabels().get(1).getName());
    }

}
