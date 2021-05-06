package com.amazonaws.wafv2.ipset;

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import com.amazonaws.wafv2.commons.HandlerHelper;
import com.amazonaws.wafv2.ipset.converters.TagConverter;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.CreateIpSetRequest;
import software.amazon.awssdk.services.wafv2.model.CreateIpSetResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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

        try {
            final CreateIpSetResponse response = createIPSetExceptionWrapper(proxy, model).execute();

            final ResourceModel readResourceModel = ResourceModel.builder()
                    .id(response.summary().id())
                    .name(response.summary().name())
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

    private ExceptionTranslationWrapper<CreateIpSetResponse> createIPSetExceptionWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<CreateIpSetResponse>() {
            @Override
            public CreateIpSetResponse doWithTranslation() throws RuntimeException {
                final CreateIpSetRequest.Builder createIpSetRequest = CreateIpSetRequest.builder()
                        .name(model.getName())
                        .scope(model.getScope())
                        .description(model.getDescription())
                        .ipAddressVersion(model.getIPAddressVersion())
                        //IPAddresses are not null, can be empty
                        .addresses(model.getAddresses());
                if (!CollectionUtils.isNullOrEmpty(model.getTags())) {
                    createIpSetRequest.tags(model.getTags().stream()
                            .map(TagConverter.INSTANCE::convert)
                            .collect(Collectors.toList()));
                }
                final CreateIpSetResponse response = proxy.injectCredentialsAndInvokeV2(createIpSetRequest.build(),
                        client::createIPSet);
                return response;
            }
        };
    }
}
