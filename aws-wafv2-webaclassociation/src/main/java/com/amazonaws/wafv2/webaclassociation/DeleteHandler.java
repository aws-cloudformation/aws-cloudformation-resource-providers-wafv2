package com.amazonaws.wafv2.webaclassociation;

import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.DisassociateWebAclRequest;
import software.amazon.awssdk.services.wafv2.model.DisassociateWebAclResponse;
import software.amazon.awssdk.services.wafv2.model.GetWebAclForResourceRequest;
import software.amazon.awssdk.services.wafv2.model.GetWebAclForResourceResponse;
import software.amazon.awssdk.services.wafv2.model.WebACL;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Optional;

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
        log(logger, model, "invoked");
        try {
            Optional<WebACL> associatedWebACL = getAssociatedWebACL(proxy, model);
            // https://t.corp.amazon.com/V209544621 make sure webACL arn from model matches associated
            if (!associatedWebACL.isPresent()) {
                log(logger, model, "No WebACL is associated, disassociate will not be called");
            }
            else if (!associatedWebACL.get().arn().equals(model.getWebACLArn())) {
                String msg = String.format("Associated WebACL %s doesn't match the one from request, disassociate will "
                                + "not be called", associatedWebACL.get().arn());
                log(logger, model, msg);
            }
            else {
                // associated webACL is present and matches the one on the model
                disassociateWebACLExceptionWrapper(proxy, model).execute();
                log(logger, model, "deleted successfully");
            }
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (RuntimeException e) {
            log(logger, model, String.format("[%s]: %s",  e.getClass().getSimpleName(), e.getMessage()));
            // handle error code
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(ExceptionTranslationWrapper.translateExceptionIntoErrorCode(e))
                    .message(e.getMessage())
                    .build();
        }
    }

    private void log(final Logger logger, ResourceModel model, String message) {
        logger.log(String.format("DeleteHandler - webACL arn: %s, resourceARN: %s message: %s",
                model.getWebACLArn(),
                model.getResourceArn(),
                message));
    }

    private ExceptionTranslationWrapper<DisassociateWebAclResponse> disassociateWebACLExceptionWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<DisassociateWebAclResponse>() {
            @Override
            public DisassociateWebAclResponse doWithTranslation() throws RuntimeException {
                final DisassociateWebAclRequest disassociateWebAclRequest = DisassociateWebAclRequest.builder()
                        .resourceArn(model.getResourceArn())
                        .build();
                final DisassociateWebAclResponse response = proxy.injectCredentialsAndInvokeV2(
                        disassociateWebAclRequest, client::disassociateWebACL);
                return response;
            }
        };
    }

    private Optional<WebACL> getAssociatedWebACL(AmazonWebServicesClientProxy proxy, ResourceModel model) {
        ExceptionTranslationWrapper<GetWebAclForResourceResponse> wrapper =
                new ExceptionTranslationWrapper<GetWebAclForResourceResponse>() {
                    @Override
                    public GetWebAclForResourceResponse doWithTranslation() throws RuntimeException {
                        final GetWebAclForResourceRequest getWebAclForResourceRequest = GetWebAclForResourceRequest
                                .builder()
                                .resourceArn(model.getResourceArn())
                                .build();
                        final GetWebAclForResourceResponse response = proxy.injectCredentialsAndInvokeV2(
                                getWebAclForResourceRequest, client::getWebACLForResource);
                        return response;
                    }
                };

        WebACL webACL = wrapper.execute().webACL();
        return (webACL == null || StringUtils.isEmpty(webACL.arn())) ? Optional.empty() : Optional.of(webACL);
    }
}
