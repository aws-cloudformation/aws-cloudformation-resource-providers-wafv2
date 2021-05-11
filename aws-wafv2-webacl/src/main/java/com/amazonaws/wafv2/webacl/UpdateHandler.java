package com.amazonaws.wafv2.webacl;

import com.amazonaws.wafv2.commons.CommonVariables;
import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import com.amazonaws.wafv2.webacl.converters.Converter;
import com.amazonaws.wafv2.webacl.converters.StatementCommonsConverter;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.MapUtils;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.GetWebAclRequest;
import software.amazon.awssdk.services.wafv2.model.GetWebAclResponse;
import software.amazon.awssdk.services.wafv2.model.UpdateWebAclRequest;
import software.amazon.awssdk.services.wafv2.model.UpdateWebAclResponse;
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
            updateWebACLExceptionWrapper(proxy, model).execute();

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

    private ExceptionTranslationWrapper<UpdateWebAclResponse> updateWebACLExceptionWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<UpdateWebAclResponse>() {
            @Override
            public UpdateWebAclResponse doWithTranslation() throws RuntimeException {
                final UpdateWebAclRequest.Builder updateWebAclRequest = UpdateWebAclRequest.builder()
                        .scope(model.getScope())
                        .name(model.getName())
                        .id(model.getId())
                        .defaultAction(StatementCommonsConverter.INSTANCE.convert(model.getDefaultAction()))
                        .description(model.getDescription())
                        .rules(Optional.ofNullable(model.getRules()).orElse(ImmutableList.of()).stream()
                                .map(rule -> Converter.INSTANCE.convert(rule))
                                .collect(Collectors.toList()))
                        .visibilityConfig(StatementCommonsConverter.INSTANCE.convert(model.getVisibilityConfig()))
                        .lockToken(getLockToken(proxy, model));
                if (MapUtils.isNotEmpty(model.getCustomResponseBodies())) {
                    updateWebAclRequest.customResponseBodies(
                        Converter.INSTANCE.convert(model.getCustomResponseBodies()));
                }
                final UpdateWebAclResponse response = proxy.injectCredentialsAndInvokeV2(
                        updateWebAclRequest.build(), client::updateWebACL);
                return response;
            }
        };
    }

    private String getLockToken(final AmazonWebServicesClientProxy proxy,
                                final ResourceModel model) {
        final GetWebAclRequest getWebAclRequest = GetWebAclRequest.builder()
                .name(model.getName())
                .id(model.getId())
                .scope(model.getScope())
                .build();
        final GetWebAclResponse response = proxy.injectCredentialsAndInvokeV2(
                getWebAclRequest, client::getWebACL);
        return response.lockToken();
    }
}
