package com.amazonaws.wafv2.rulegroup;

import com.amazonaws.wafv2.commons.CommonVariables;
import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.DeleteRuleGroupRequest;
import software.amazon.awssdk.services.wafv2.model.DeleteRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.GetRuleGroupRequest;
import software.amazon.awssdk.services.wafv2.model.GetRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.WafUnavailableEntityException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@RequiredArgsConstructor
public class DeleteHandler extends BaseHandler<CallbackContext> {

    private final Wafv2Client client;

    public DeleteHandler() {
        this.client = CustomerAPIClientBuilder.getClient();
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final CallbackContext currentContext = callbackContext == null ?
                CallbackContext.builder()
                        .stabilizationRetriesRemaining(CommonVariables.NUMBER_OF_STATE_POLL_RETRIES)
                        .build()
                : callbackContext;

        if (currentContext.getStabilizationRetriesRemaining() <= 0) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotStabilized)
                    .build();
        }

        try {
            deleteRuleGroupExceptionWrapper(proxy, model).execute();
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (WafUnavailableEntityException e) {
            // entity still being sequenced
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.IN_PROGRESS)
                    .callbackContext(CallbackContext.builder()
                            .stabilizationRetriesRemaining(currentContext.getStabilizationRetriesRemaining() - 1)
                            .build())
                    .callbackDelaySeconds(CommonVariables.CALLBACK_DELAY_SECONDS)
                    .build();
        } catch (RuntimeException e) {
            // handle error code
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(ExceptionTranslationWrapper.translateExceptionIntoErrorCode(e))
                    .message(e.getMessage())
                    .build();
        }
    }

    private ExceptionTranslationWrapper<DeleteRuleGroupResponse> deleteRuleGroupExceptionWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<DeleteRuleGroupResponse>() {
            @Override
            public DeleteRuleGroupResponse doWithTranslation() throws RuntimeException {
                final DeleteRuleGroupRequest deleteRuleGroupRequest = DeleteRuleGroupRequest.builder()
                        .name(model.getName())
                        .id(model.getId())
                        .lockToken(getLockToken(proxy, model))
                        .scope(model.getScope())
                        .build();
                final DeleteRuleGroupResponse response = proxy.injectCredentialsAndInvokeV2(
                        deleteRuleGroupRequest, client::deleteRuleGroup);
                return response;
            }
        };
    }

    private String getLockToken(final AmazonWebServicesClientProxy proxy,
                                final ResourceModel model) {
        final GetRuleGroupRequest getRuleGroupRequest = GetRuleGroupRequest.builder()
                .name(model.getName())
                .id(model.getId())
                .scope(model.getScope())
                .build();
        final GetRuleGroupResponse response = proxy.injectCredentialsAndInvokeV2(
                getRuleGroupRequest, client::getRuleGroup);
        return response.lockToken();
    }
}
