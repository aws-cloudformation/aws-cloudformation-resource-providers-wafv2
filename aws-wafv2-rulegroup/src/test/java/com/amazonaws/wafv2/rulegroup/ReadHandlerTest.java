package com.amazonaws.wafv2.rulegroup;

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
import software.amazon.awssdk.services.wafv2.model.WafLimitsExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;
import java.util.Map;

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
        readHandler = new ReadHandler(mock(Wafv2Client.class));
        ruleGroup = RuleGroupHelper.getSdkRuleGroup();
        resourceModel = RuleGroupHelper.getTestResourceModel();
        ruleGroupCustomRequestResponse = RuleGroupHelper.getSdkRuleGroupCustomRequestAndResponse();
        ruleGroupWithRuleLabelsWithinRule = RuleGroupHelper.getSdkRuleGroupWithRulelabelsWithinRule();
        ruleGroupWithAvailableAndConsumedLabels = RuleGroupHelper.getSdkRuleGroupWithAvailableAndConsumedLabels();
        resourceModelCustomRequestResponse = RuleGroupHelper.getTestResourceModelCustomRequestAndResponse();
        resourceModelWithRuleLabelsWithinRule = RuleGroupHelper.getTestResourceModelWithRulelabelsWithinRule();
        resourceModelWithAvailableAndConsumedLabels = RuleGroupHelper.getTestResourceModelWithAvailableAndConsumedLabels();
        TagInfoForResource tagInfo = TagInfoForResource.builder()
                .tagList(Tag.builder().key("k1").value("v1").build())
                .build();
        ListTagsForResourceResponse response = ListTagsForResourceResponse.builder()
                .tagInfoForResource(tagInfo)
                .build();
        doReturn(response).when(proxy).injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());
    }

    @Test
    public void testSuccessfulGetRuleGroup() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final GetRuleGroupResponse stubResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroup)
                .lockToken("someLockToken")
                .build();

        doReturn(stubResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                readHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResourceModel().getArn());
        Assert.assertNotNull(response.getResourceModel().getDescription());
        Assert.assertNotNull(response.getResourceModel().getId());
        Assert.assertNotNull(response.getResourceModel().getName());
        Assert.assertNotNull(response.getResourceModel().getScope());
        Assert.assertNotNull(response.getResourceModel().getCapacity());
        Assert.assertNotNull(response.getResourceModel().getRules());
        Assert.assertNotNull(response.getResourceModel().getVisibilityConfig());
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
    public void testSuccessfulGetRuleGroupCustomRequestAndResponse() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(resourceModelCustomRequestResponse)
            .build();
        final GetRuleGroupResponse stubResponse = GetRuleGroupResponse.builder()
            .ruleGroup(ruleGroupCustomRequestResponse)
            .lockToken("someLockToken")
            .build();

        doReturn(stubResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
            readHandler.handleRequest(proxy, request, null, logger);

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
    public void testFailGetWithError() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        doThrow(WafLimitsExceededException.builder().build())
                .when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                readHandler.handleRequest(proxy, request, null, logger);

        Assert.assertNotNull(response);
        Assert.assertEquals(OperationStatus.FAILED, response.getStatus());
        Assert.assertEquals(HandlerErrorCode.ServiceLimitExceeded, response.getErrorCode());
        Assert.assertNull(response.getResourceModel());
        Assert.assertNull(response.getCallbackContext());
    }

    @Test
    public void testGetRuleGroupWithRuleLabelWithinRule() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModelWithRuleLabelsWithinRule)
                .build();
        final GetRuleGroupResponse stubResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroupWithRuleLabelsWithinRule)
                .lockToken("someLockToken")
                .build();

        doReturn(stubResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                readHandler.handleRequest(proxy, request, null, logger);

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

    @Test
    public void testGetRuleGroupWithAvailableAndConsumedLabels() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModelWithAvailableAndConsumedLabels)
                .build();
        final GetRuleGroupResponse stubResponse = GetRuleGroupResponse.builder()
                .ruleGroup(ruleGroupWithAvailableAndConsumedLabels)
                .lockToken("someLockToken")
                .build();

        doReturn(stubResponse).when(proxy).injectCredentialsAndInvokeV2(any(GetRuleGroupRequest.class), any());
        final ProgressEvent<ResourceModel, CallbackContext> response =
                readHandler.handleRequest(proxy, request, null, logger);

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

        // Verify available Labels
        Assert.assertNotNull(response.getResourceModel().getAvailableLabels());
        Assert.assertEquals(2, response.getResourceModel().getAvailableLabels().size());
        List<LabelSummary> availableLabels = response.getResourceModel().getAvailableLabels();
        Assert.assertEquals("AvailableLabel1", availableLabels.get(0).getName());
        Assert.assertEquals("AvailableLabel:2", availableLabels.get(1).getName());

        // Verify consumed labels
        Assert.assertNotNull(response.getResourceModel().getConsumedLabels());
        Assert.assertEquals(2, response.getResourceModel().getConsumedLabels().size());
        List<LabelSummary> consumedLabels = response.getResourceModel().getConsumedLabels();
        Assert.assertEquals("ConsumedLabel1", consumedLabels.get(0).getName());
        Assert.assertEquals("ConsumedLabel:2", consumedLabels.get(1).getName());
    }


}
