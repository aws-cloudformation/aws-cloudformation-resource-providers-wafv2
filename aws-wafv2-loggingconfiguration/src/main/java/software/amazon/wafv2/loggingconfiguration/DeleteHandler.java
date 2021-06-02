package software.amazon.wafv2.loggingconfiguration;

import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.Wafv2Exception;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;

import com.amazonaws.util.StringUtils;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

    protected static final String MESSAGE_STABILIZED = "Logging Configuration Delete has stabilized";
    protected static final String MESSAGE_DID_NOT_STABILIZE = "Logging Configuration Delete has not stabilized";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Wafv2Client> proxyClient,
        final Logger logger) {

        this.logger = logger;
        ResourceModel model = request.getDesiredResourceState();

        if (StringUtils.isNullOrEmpty(model.getResourceArn())) {
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest, "Resource ARN cannot be empty");
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> proxy.initiate("AWS-WAFv2-LoggingConfiguration::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToDeleteRequest)
                        .makeServiceCall((cbRequest, cbProxyClient) -> cbProxyClient.injectCredentialsAndInvokeV2(cbRequest, cbProxyClient.client()::deleteLoggingConfiguration))
                        .stabilize((cbRequest, cbResponse, cbProxyClient, cbModel, cbContext) -> stabilizeDelete(cbProxyClient, cbModel))
                        .handleError(this::handleError)
                        .done(cbResponse -> ProgressEvent.<ResourceModel, CallbackContext> builder().status(OperationStatus.SUCCESS).build()));
    }

    /**
     * Method to stabilize the deletion of the specified Logging Configuration
     * @param proxyClient
     * @param model
     * @return boolean value indicating the status of the stabilization
     */
    private boolean stabilizeDelete(final ProxyClient<Wafv2Client> proxyClient, final ResourceModel model) {
        try {
            proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model), proxyClient.client()::getLoggingConfiguration);
            logger.log(MESSAGE_DID_NOT_STABILIZE);
            return false;
        } catch (Wafv2Exception e) {
            if (e instanceof WafNonexistentItemException) {
                logger.log(MESSAGE_STABILIZED);
                return true;
            }
            throw e;
        }
    }
}
