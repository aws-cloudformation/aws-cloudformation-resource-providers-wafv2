package com.amazonaws.wafv2.rulegroup;

import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import com.amazonaws.wafv2.commons.HandlerHelper;
import com.amazonaws.wafv2.rulegroup.converters.Converter;
import com.amazonaws.wafv2.rulegroup.converters.StatementCommonsConverter;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.GetRuleGroupRequest;
import software.amazon.awssdk.services.wafv2.model.GetRuleGroupResponse;
import software.amazon.awssdk.services.wafv2.model.LabelSummary;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
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
            final GetRuleGroupResponse response = getRuleGroupExceptionWrapper(proxy, model).execute();

            final List<Tag> tags = HandlerHelper.getConvertedTags(proxy, client, response.ruleGroup().arn(),
                    tag -> Tag.builder()
                            .key(tag.key())
                            .value(tag.value())
                            .build());
            final ResourceModel.ResourceModelBuilder result = ResourceModel.builder()
                    // primary identifier
                    .id(response.ruleGroup().id())
                    .name(response.ruleGroup().name())
                    .scope(model.getScope())
                    // read only fields
                    .arn(response.ruleGroup().arn())
                    // other fields
                    .description(response.ruleGroup().description())
                    .rules(Optional.ofNullable(response.ruleGroup().rules()).orElse(ImmutableList.of()).stream()
                            .map(rule -> Converter.INSTANCE.invert(rule))
                            .collect(Collectors.toList()))
                    .visibilityConfig(StatementCommonsConverter.INSTANCE.invert(
                            response.ruleGroup().visibilityConfig()))
                    // capacity is specified by customer in RuleGroup
                    .capacity(new Integer(Math.toIntExact(response.ruleGroup().capacity().longValue())))
                    .tags(tags)
                    .availableLabels(Optional.ofNullable(Converter.INSTANCE.invert(response.ruleGroup().availableLabels())).orElse(ImmutableList.of()))
                    .consumedLabels(Optional.ofNullable(Converter.INSTANCE.invert(response.ruleGroup().consumedLabels())).orElse(ImmutableList.of()))
                    .labelNamespace(response.ruleGroup().labelNamespace());

            if (MapUtils.isNotEmpty(response.ruleGroup().customResponseBodies())) {
                result.customResponseBodies(
                    Converter.INSTANCE.invert(response.ruleGroup().customResponseBodies()));
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

    private ExceptionTranslationWrapper<GetRuleGroupResponse> getRuleGroupExceptionWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<GetRuleGroupResponse>() {
            @Override
            public GetRuleGroupResponse doWithTranslation() throws RuntimeException {
                final GetRuleGroupRequest getRuleGroupRequest = GetRuleGroupRequest.builder()
                        .scope(model.getScope())
                        .name(model.getName())
                        .id(model.getId())
                        .build();
                final GetRuleGroupResponse response = proxy.injectCredentialsAndInvokeV2(
                        getRuleGroupRequest, client::getRuleGroup);
                return response;
            }
        };
    }

}
