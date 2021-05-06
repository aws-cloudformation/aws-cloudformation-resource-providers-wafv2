package com.amazonaws.wafv2.rulegroup;

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.amazonaws.wafv2.commons.CommonVariables;
import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import com.amazonaws.wafv2.commons.HandlerHelper;
import com.amazonaws.wafv2.rulegroup.converters.Converter;
import com.amazonaws.wafv2.rulegroup.converters.StatementCommonsConverter;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.MapUtils;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.CreateRuleGroupRequest;
import software.amazon.awssdk.services.wafv2.model.CreateRuleGroupResponse;
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
public class CreateHandler extends BaseHandler<CallbackContext> {

    private final Wafv2Client client;

    public CreateHandler() {
        this.client = CustomerAPIClientBuilder.getClient();
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        if (StringUtils.isNullOrEmpty(model.getName())) {
            model.setName(HandlerHelper.generateName(request));
        }

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
            final CreateRuleGroupResponse response = createRuleGroupExceptionWrapper(proxy, model).execute();

            final ResourceModel readResourceModel = ResourceModel.builder()
                    .id(response.summary().id())
                    .name(response.summary().name())
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

    private ExceptionTranslationWrapper<CreateRuleGroupResponse> createRuleGroupExceptionWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<CreateRuleGroupResponse>() {
            @Override
            public CreateRuleGroupResponse doWithTranslation() throws RuntimeException {
                final CreateRuleGroupRequest.Builder createRuleGroupRequestBuilder = CreateRuleGroupRequest.builder()
                        .name(model.getName())
                        .scope(model.getScope())
                        .description(model.getDescription())
                        .visibilityConfig(StatementCommonsConverter.INSTANCE.convert(model.getVisibilityConfig()))
                        .rules(Optional.ofNullable(model.getRules()).orElse(ImmutableList.of()).stream()
                                .map(rule -> Converter.INSTANCE.convert(rule))
                                .collect(Collectors.toList()))
                        .capacity(new Long(model.getCapacity().intValue()));

                if (MapUtils.isNotEmpty(model.getCustomResponseBodies())) {
                    createRuleGroupRequestBuilder.customResponseBodies(
                        Converter.INSTANCE.convert(model.getCustomResponseBodies()));
                }

                if (!CollectionUtils.isNullOrEmpty(model.getTags())) {
                    createRuleGroupRequestBuilder.tags(model.getTags().stream()
                            .map(Converter.INSTANCE::convert)
                            .collect(Collectors.toList()));
                }

                final CreateRuleGroupResponse response = proxy.injectCredentialsAndInvokeV2(
                        createRuleGroupRequestBuilder.build(), client::createRuleGroup);
                return response;
            }
        };
    }
}
