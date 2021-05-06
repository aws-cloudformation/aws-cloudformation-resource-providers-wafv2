package com.amazonaws.wafv2.regexpatternset;

import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.DeleteRegexPatternSetRequest;
import software.amazon.awssdk.services.wafv2.model.DeleteRegexPatternSetResponse;
import software.amazon.awssdk.services.wafv2.model.GetRegexPatternSetRequest;
import software.amazon.awssdk.services.wafv2.model.GetRegexPatternSetResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
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

        try {
            deleteRegexPatternSetExceptionWrapper(proxy, model).execute();
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.SUCCESS)
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

    private ExceptionTranslationWrapper<DeleteRegexPatternSetResponse> deleteRegexPatternSetExceptionWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<DeleteRegexPatternSetResponse>() {
            @Override
            public DeleteRegexPatternSetResponse doWithTranslation() throws RuntimeException {
                final DeleteRegexPatternSetRequest deleteRegexPatternSetRequest = DeleteRegexPatternSetRequest.builder()
                        .name(model.getName())
                        .id(model.getId())
                        .lockToken(getLockToken(proxy, model))
                        .scope(model.getScope())
                        .build();
                final DeleteRegexPatternSetResponse response = proxy.injectCredentialsAndInvokeV2(
                        deleteRegexPatternSetRequest, client::deleteRegexPatternSet);
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
