package com.amazonaws.wafv2.rulegroup;

import com.amazonaws.wafv2.commons.CommonVariables;
import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import com.amazonaws.wafv2.rulegroup.converters.Converter;
import com.amazonaws.wafv2.rulegroup.converters.StatementCommonsConverter;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.MapUtils;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.GetRuleGroupRequest;
import software.amazon.awssdk.services.wafv2.model.GetRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.UpdateRuleGroupRequest;
import software.amazon.awssdk.services.wafv2.model.UpdateRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.WafUnavailableEntityException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UpdateHandler extends BaseHandler<CallbackContext> {

    private final Wafv2Client client;

    public UpdateHandler() {
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
            updateRuleGroupExceptionWrapper(proxy, model).execute();

            final ResourceModel readResourceModel = ResourceModel.builder()
                    .id(model.getId())
                    .name(model.getName())
                    .scope(model.getScope())
                    .build();
            return new ReadHandler(client).handleRequest(proxy,
                    ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(readResourceModel).build(),
                    null, logger);
        } catch (WafUnavailableEntityException e) {
            // entity still being sequenced
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.IN_PROGRESS)
                    .callbackContext(CallbackContext.builder()
                            .stabilizationRetriesRemaining(currentContext.getStabilizationRetriesRemaining() - 1)
                            .build())
                    .callbackDelaySeconds(CommonVariables.CALLBACK_DELAY_SECONDS)
                    .resourceModel(request.getDesiredResourceState())
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

    private ExceptionTranslationWrapper<UpdateRuleGroupResponse> updateRuleGroupExceptionWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<UpdateRuleGroupResponse>() {
            @Override
            public UpdateRuleGroupResponse doWithTranslation() throws RuntimeException {
                final UpdateRuleGroupRequest.Builder updateRuleGroupRequestBuilder = UpdateRuleGroupRequest.builder()
                        .scope(model.getScope())
                        .name(model.getName())
                        .id(model.getId())
                        .description(model.getDescription())
                        .rules(Optional.ofNullable(model.getRules()).orElse(ImmutableList.of()).stream()
                                .map(rule -> Converter.INSTANCE.convert(rule))
                                .collect(Collectors.toList()))
                        .visibilityConfig(StatementCommonsConverter.INSTANCE.convert(model.getVisibilityConfig()))
                        .lockToken(getLockToken(proxy, model));
                if (MapUtils.isNotEmpty(model.getCustomResponseBodies())) {
                    updateRuleGroupRequestBuilder.customResponseBodies(
                        Converter.INSTANCE.convert(model.getCustomResponseBodies()));
                }
                final UpdateRuleGroupResponse response = proxy.injectCredentialsAndInvokeV2(
                    updateRuleGroupRequestBuilder.build(), client::updateRuleGroup);
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
