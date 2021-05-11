package com.amazonaws.wafv2.ipset;

import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.DeleteIpSetRequest;
import software.amazon.awssdk.services.wafv2.model.DeleteIpSetResponse;
import software.amazon.awssdk.services.wafv2.model.GetIpSetRequest;
import software.amazon.awssdk.services.wafv2.model.GetIpSetResponse;
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
            deleteIpSetExceptionTranslationWrapper(proxy, model).execute();
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.SUCCESS)
                    .build();
        }
        catch (RuntimeException ex) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(ExceptionTranslationWrapper.translateExceptionIntoErrorCode(ex))
                    .message(ex.getMessage()).build();
        }

    }

    private ExceptionTranslationWrapper<DeleteIpSetResponse> deleteIpSetExceptionTranslationWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<DeleteIpSetResponse>() {
            @Override
            public DeleteIpSetResponse doWithTranslation() throws RuntimeException {
                final DeleteIpSetRequest deleteIpSetRequest = DeleteIpSetRequest.builder()
                        .name(model.getName())
                        .id(model.getId())
                        .lockToken(getLockToken(proxy, model))
                        .scope(model.getScope())
                        .build();
                final DeleteIpSetResponse response = proxy.injectCredentialsAndInvokeV2(
                        deleteIpSetRequest, client::deleteIPSet
                );
                return response;
            }
        };
    }

    private String getLockToken(final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
            final GetIpSetRequest getIpSetRequest = GetIpSetRequest.builder()
                    .name(model.getName())
                    .id(model.getId())
                    .scope(model.getScope())
                    .build();
            final GetIpSetResponse response = proxy.injectCredentialsAndInvokeV2(getIpSetRequest
                    , client::getIPSet);
            return response.lockToken();
    }
}
