package com.amazonaws.wafv2.ipset;

import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import com.amazonaws.wafv2.commons.HandlerHelper;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.GetIpSetRequest;
import software.amazon.awssdk.services.wafv2.model.GetIpSetResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

@RequiredArgsConstructor
public class ReadHandler extends BaseHandler<CallbackContext> {

    private final Wafv2Client client;

    public ReadHandler() {
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
            final GetIpSetResponse response = getIpsetExceptionWrapper(proxy, model).execute();
            final List<Tag> tags = HandlerHelper.getConvertedTags(proxy, client, response.ipSet().arn(),
                    tag -> Tag.builder()
                            .key(tag.key())
                            .value(tag.value())
                            .build());
            final ResourceModel result = ResourceModel.builder()
                    .id(response.ipSet().id())
                    .name(response.ipSet().name())
                    .scope(model.getScope())
                    .arn(response.ipSet().arn())
                    .description(response.ipSet().description())
                    .addresses(response.ipSet().addresses()) //addresses doesn't have limit issue with Uluru V2
                    .iPAddressVersion(response.ipSet().ipAddressVersionAsString())
                    .tags(tags)
                    .build();

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(result)
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (RuntimeException e) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(ExceptionTranslationWrapper.translateExceptionIntoErrorCode(e))
                    .message(e.getMessage())
                    .build();
        }
    }

    private ExceptionTranslationWrapper<GetIpSetResponse> getIpsetExceptionWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<GetIpSetResponse>() {
            @Override
            public GetIpSetResponse doWithTranslation() throws RuntimeException {
                final GetIpSetRequest getIpSetRequest = GetIpSetRequest.builder()
                        .name(model.getName())
                        .id(model.getId())
                        .scope(model.getScope())
                        .build();
                final GetIpSetResponse response = proxy.injectCredentialsAndInvokeV2(
                        getIpSetRequest, client::getIPSet);
                return response;
            }
        };
    }
}
