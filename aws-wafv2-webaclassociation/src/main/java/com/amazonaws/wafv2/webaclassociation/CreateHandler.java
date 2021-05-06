package com.amazonaws.wafv2.webaclassociation;

import com.amazonaws.wafv2.commons.CommonVariables;
import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import com.amazonaws.wafv2.commons.ExceptionTranslationWrapper;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.AssociateWebAclRequest;
import software.amazon.awssdk.services.wafv2.model.AssociateWebAclResponse;
import software.amazon.awssdk.services.wafv2.model.WafUnavailableEntityException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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
        final CallbackContext currentContext = callbackContext == null ?
                CallbackContext.builder()
                        .stabilizationRetriesRemaining(CommonVariables.NUMBER_OF_STATE_POLL_RETRIES)
                        .build()
                : callbackContext;
        log(logger, model, "retries left: " + currentContext.getStabilizationRetriesRemaining());
        if (currentContext.getStabilizationRetriesRemaining() <= 0) {
            log(logger, model, "no more retries remaining");
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotStabilized)
                    .build();
        }
        try {
            associateWebACLExceptionWrapper(proxy, model).execute();
            log(logger, model, "created successfully");
            // propogate input values to make CFN happy, since these two make up the resource primary key.
            final ResourceModel result = ResourceModel.builder()
                    .resourceArn(model.getResourceArn())
                    .webACLArn(model.getWebACLArn())
                    .build();
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(result)
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (WafUnavailableEntityException e) {
            int retryLeft = currentContext.getStabilizationRetriesRemaining() - 1;
            int delaySeconds = CommonVariables.CALLBACK_DELAY_SECONDS;
            String message = String.format("WafUnavailableEntityException: %s Retryleft: %d NextDelay: %d",
                    e.getMessage(), retryLeft, delaySeconds);
            log(logger, model, message);
            // WebACL still being sequenced
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.IN_PROGRESS)
                    .callbackContext(CallbackContext.builder()
                            .stabilizationRetriesRemaining(retryLeft)
                            .build())
                    .callbackDelaySeconds(delaySeconds)
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
        logger.log(String.format("CreateHandler - webACL arn: %s, resourceARN: %s message: %s",
                model.getWebACLArn(),
                model.getResourceArn(),
                message));
    }

    private ExceptionTranslationWrapper<AssociateWebAclResponse> associateWebACLExceptionWrapper(
            final AmazonWebServicesClientProxy proxy, final ResourceModel model) {
        return new ExceptionTranslationWrapper<AssociateWebAclResponse>() {
            @Override
            public AssociateWebAclResponse doWithTranslation() throws RuntimeException {
                final AssociateWebAclRequest associateWebAclRequest = AssociateWebAclRequest.builder()
                        .resourceArn(model.getResourceArn())
                        .webACLArn(model.getWebACLArn())
                        .build();
                final AssociateWebAclResponse response = proxy.injectCredentialsAndInvokeV2(
                        associateWebAclRequest, client::associateWebACL);
                return response;
            }
        };
    }
}
