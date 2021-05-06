package com.amazonaws.wafv2.webacl;

import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import com.amazonaws.wafv2.commons.HandlerHelper;
import com.amazonaws.wafv2.webacl.converters.Converter;
import com.amazonaws.wafv2.webacl.converters.StatementCommonsConverter;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.MapUtils;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.GetWebAclRequest;
import software.amazon.awssdk.services.wafv2.model.GetWebAclResponse;
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
            final GetWebAclResponse response = getWebACLExceptionWrapper(proxy, model).execute();
            final List<Tag> tags = HandlerHelper.getConvertedTags(proxy, client, response.webACL().arn(),
                    tag -> Tag.builder()
                            .key(tag.key())
                            .value(tag.value())
                            .build());
            final ResourceModel.ResourceModelBuilder result = ResourceModel.builder()
                    // primary identifiers
                    .id(response.webACL().id())
                    .name(response.webACL().name())
                    .scope(model.getScope())
                    // read only fields
                    .arn(response.webACL().arn())
                    .capacity(new Integer(Math.toIntExact(response.webACL().capacity().longValue())))
                    // other fields
                    .description(response.webACL().description())
                    .defaultAction(StatementCommonsConverter.INSTANCE.invert(response.webACL().defaultAction()))
                    .rules(Optional.ofNullable(response.webACL().rules()).orElse(ImmutableList.of()).stream()
                            .map(rule -> Converter.INSTANCE.invert(rule))
                            .collect(Collectors.toList()))
                    .visibilityConfig(StatementCommonsConverter.INSTANCE.invert(response.webACL().visibilityConfig()))
                    .labelNamespace(response.webACL().labelNamespace())
                    .tags(tags);

            if (MapUtils.isNotEmpty(response.webACL().customResponseBodies())) {
                result.customResponseBodies(
                    Converter.INSTANCE.invert(response.webACL().customResponseBodies()));
            }
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(result.build())
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

    private ExceptionTranslationWrapper<GetWebAclResponse> getWebACLExceptionWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<GetWebAclResponse>() {
            @Override
            public GetWebAclResponse doWithTranslation() throws RuntimeException {
                final GetWebAclRequest getWebAclRequest = GetWebAclRequest.builder()
                        .scope(model.getScope())
                        .name(model.getName())
                        .id(model.getId())
                        .build();
                final GetWebAclResponse response = proxy.injectCredentialsAndInvokeV2(
                        getWebAclRequest, client::getWebACL);
                return response;
            }
        };
    }
}
