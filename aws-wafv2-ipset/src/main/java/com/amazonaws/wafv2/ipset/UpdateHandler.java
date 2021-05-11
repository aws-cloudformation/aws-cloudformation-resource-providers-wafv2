package com.amazonaws.wafv2.ipset;

import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.GetIpSetRequest;
import software.amazon.awssdk.services.wafv2.model.GetIpSetResponse;
import software.amazon.awssdk.services.wafv2.model.UpdateIpSetRequest;
import software.amazon.awssdk.services.wafv2.model.UpdateIpSetResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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
            updateIpSetResponseExceptionTranslationWrapper(proxy, model).execute();
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

    private ExceptionTranslationWrapper<UpdateIpSetResponse> updateIpSetResponseExceptionTranslationWrapper(
            final AmazonWebServicesClientProxy proxy, ResourceModel model) {

        return new ExceptionTranslationWrapper<UpdateIpSetResponse>() {
            @Override
            public UpdateIpSetResponse doWithTranslation() throws RuntimeException {
                final UpdateIpSetRequest request = UpdateIpSetRequest.builder()
                        .scope(model.getScope())
                        .name(model.getName())
                        .id(model.getId())
                        .lockToken(getLockToken(proxy, model))
                        .description(model.getDescription())
                        .addresses(model.getAddresses()).build();
                final UpdateIpSetResponse response = proxy.injectCredentialsAndInvokeV2(request, client::updateIPSet);
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
