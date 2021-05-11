package com.amazonaws.wafv2.regexpatternset;

import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.GetRegexPatternSetRequest;
import software.amazon.awssdk.services.wafv2.model.GetRegexPatternSetResponse;
import software.amazon.awssdk.services.wafv2.model.Regex;
import software.amazon.awssdk.services.wafv2.model.UpdateRegexPatternSetRequest;
import software.amazon.awssdk.services.wafv2.model.UpdateRegexPatternSetResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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

        try {
            updateRegexPatternSetExceptionWrapper(proxy, model).execute();

            final ResourceModel readResourceModel = ResourceModel.builder()
                    .id(model.getId())
                    .name(model.getName())
                    .scope(model.getScope())
                    .build();
            return new ReadHandler(client).handleRequest(proxy,
                    ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(readResourceModel).build(),
                    null, logger);
        } catch (RuntimeException e) {
            // handle error code
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(ExceptionTranslationWrapper.translateExceptionIntoErrorCode(e))
                    .message(e.getMessage())
                    .build();
        }
    }

    private ExceptionTranslationWrapper<UpdateRegexPatternSetResponse> updateRegexPatternSetExceptionWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<UpdateRegexPatternSetResponse>() {
            @Override
            public UpdateRegexPatternSetResponse doWithTranslation() throws RuntimeException {
                final UpdateRegexPatternSetRequest updateRegexPatternSetRequest = UpdateRegexPatternSetRequest.builder()
                        .scope(model.getScope())
                        .name(model.getName())
                        .id(model.getId())
                        .description(model.getDescription())
                        .regularExpressionList(model.getRegularExpressionList().stream()
                                .map(regex -> Regex.builder().regexString(regex).build())
                                .collect(Collectors.toList()))
                        .lockToken(getLockToken(proxy, model))
                        .build();
                final UpdateRegexPatternSetResponse response = proxy.injectCredentialsAndInvokeV2(
                        updateRegexPatternSetRequest, client::updateRegexPatternSet);
                return response;
            }
        };
    }

    private String getLockToken(final AmazonWebServicesClientProxy proxy,
                                final ResourceModel model) {
        final GetRegexPatternSetRequest getRegexPatternSetRequest = GetRegexPatternSetRequest.builder()
                .name(model.getName())
                .id(model.getId())
                .scope(model.getScope())
                .build();
        final GetRegexPatternSetResponse response = proxy.injectCredentialsAndInvokeV2(
                getRegexPatternSetRequest, client::getRegexPatternSet);
        return response.lockToken();
    }
}
