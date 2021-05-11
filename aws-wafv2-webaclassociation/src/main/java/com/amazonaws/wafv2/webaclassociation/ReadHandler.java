package com.amazonaws.wafv2.webaclassociation;

import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.GetWebAclForResourceRequest;
import software.amazon.awssdk.services.wafv2.model.GetWebAclForResourceResponse;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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
        log(logger, model, "invoked");
        try {
            final GetWebAclForResourceResponse response = getWebAclAssociationExceptionWrapper(proxy, model).execute();
            log(logger, model, "read successfully");
            // propogate input values to make CFN happy, since these two make up the resource primary key.
            final ResourceModel result = ResourceModel.builder()
                    .resourceArn(model.getResourceArn())
                    .webACLArn(model.getWebACLArn())
                    .build();
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(result)
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (RuntimeException e) {
            log(logger, model, String.format("[%s]: %s",  e.getClass().getSimpleName(), e.getMessage()));
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(ExceptionTranslationWrapper.translateExceptionIntoErrorCode(e))
                    .message(e.getMessage())
                    .build();
        }
    }

    private void log(final Logger logger, ResourceModel model, String message) {
        logger.log(String.format("ReadHandler - webACL arn: %s, resourceARN: %s message: %s",
                model.getWebACLArn(),
                model.getResourceArn(),
                message));
    }

    private ExceptionTranslationWrapper<GetWebAclForResourceResponse> getWebAclAssociationExceptionWrapper(
            AmazonWebServicesClientProxy proxy, ResourceModel model) {
        return new ExceptionTranslationWrapper<GetWebAclForResourceResponse>() {
            @Override
            public GetWebAclForResourceResponse doWithTranslation() throws RuntimeException {
                final GetWebAclForResourceRequest getWebAclForResourceRequest = GetWebAclForResourceRequest.builder()
                        .resourceArn(model.getResourceArn())
                        .build();
                final GetWebAclForResourceResponse response = proxy.injectCredentialsAndInvokeV2(
                        getWebAclForResourceRequest, client::getWebACLForResource);
                if (response.webACL() == null || StringUtils.isEmpty(response.webACL().arn())) {
                    throw  WafNonexistentItemException.builder().build();
                }
                return response;
            }
        };
    }
}
