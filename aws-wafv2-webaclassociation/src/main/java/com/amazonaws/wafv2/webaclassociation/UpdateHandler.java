package com.amazonaws.wafv2.webaclassociation;

import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
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
        log(logger, model, "updated successfully");
        // propogate input values to make CFN happy, since these two make up the resource primary key.
        // https://sim.amazon.com/issues/ULURU-2208
        final ResourceModel result = ResourceModel.builder()
                .resourceArn(model.getResourceArn())
                .webACLArn(model.getWebACLArn())
                .build();
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(result)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private void log(final Logger logger, ResourceModel model, String message) {
        logger.log(String.format("UpdateHandler - webACL arn: %s, resourceARN: %s message: %s",
                model.getWebACLArn(),
                model.getResourceArn(),
                message));
    }
}
