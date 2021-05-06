package com.amazonaws.wafv2.regexpatternset;

import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import com.amazonaws.wafv2.commons.HandlerHelper;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.GetRegexPatternSetRequest;
import software.amazon.awssdk.services.wafv2.model.GetRegexPatternSetResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            final GetRegexPatternSetResponse response = getRegexPatternSetExceptionWrapper(proxy, model).execute();
            final List<Tag> tags = HandlerHelper.getConvertedTags(proxy, client, response.regexPatternSet().arn(),
                    tag -> Tag.builder()
                            .key(tag.key())
                            .value(tag.value())
                            .build());
            final ResourceModel result = ResourceModel.builder()
                    // primary identifier
                    .id(response.regexPatternSet().id())
                    .name(response.regexPatternSet().name())
                    .scope(model.getScope())
                    // readOnly field
                    .arn(response.regexPatternSet().arn())
                    // other fields
                    .regularExpressionList(Optional.ofNullable(response.regexPatternSet().regularExpressionList())
                            .orElse(ImmutableList.of()).stream()
                            .map(regex ->regex.regexString())
                            .collect(Collectors.toList()))
                    .description(response.regexPatternSet().description())
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

    private ExceptionTranslationWrapper<GetRegexPatternSetResponse> getRegexPatternSetExceptionWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<GetRegexPatternSetResponse>() {
            @Override
            public GetRegexPatternSetResponse doWithTranslation() throws RuntimeException {
                final GetRegexPatternSetRequest getRegexPatternSetRequest = GetRegexPatternSetRequest.builder()
                        .scope(model.getScope())
                        .name(model.getName())
                        .id(model.getId())
                        .build();
                final GetRegexPatternSetResponse response = proxy.injectCredentialsAndInvokeV2(
                        getRegexPatternSetRequest, client::getRegexPatternSet);
                return response;
            }
        };
    }
}
