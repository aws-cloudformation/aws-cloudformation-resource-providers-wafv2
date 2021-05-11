package software.amazon.wafv2.loggingconfiguration;

import org.apache.commons.lang3.StringUtils;

import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


public class CreateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Wafv2Client> proxyClient,
        final Logger logger) {

        this.logger = logger;
        
        ResourceModel model = request.getDesiredResourceState();
        
        if(StringUtils.isBlank(model.getResourceArn())) {
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest, "Resource ARN cannot be empty");
        }
        
        if(model.getLogDestinationConfigs() == null || model.getLogDestinationConfigs().isEmpty()) {
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest, "LogDestinationConfigs cannot be empty");
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> preExistanceCheck(proxy, proxyClient, model, callbackContext))
                .then(progress -> putLoggingConfiguration(proxy, proxyClient, model, callbackContext))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }
    
    /**
     * Logging Configurations dont throw an AlreadyExists exception. Hence making a pre-existence check
     * @param proxy
     * @param proxyClient
     * @param model
     * @param callbackContext
     * @return Progress Event Object which will proceed to enable delete on termination
     */
    protected ProgressEvent<ResourceModel, CallbackContext> preExistanceCheck(final AmazonWebServicesClientProxy proxy,
            final ProxyClient<Wafv2Client> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        return proxy.initiate("AWS-WAFv2-LoggingConfiguration::Create::PreExistanceCheck", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((cbRequest, cbProxyClient) -> cbProxyClient.injectCredentialsAndInvokeV2(cbRequest, cbProxyClient.client()::getLoggingConfiguration))
                .handleError((cbRequest, cbException, cbProxyClient, cbModel, cbContext) -> {
                     if (cbException instanceof WafNonexistentItemException)
                         return ProgressEvent.progress(cbModel, cbContext);
                     throw cbException;
                })
                .done((cbRequest, cbResponse, cbProxyClient, cbModel, cbContext) -> {
                    if(cbResponse.loggingConfiguration() != null && cbResponse.loggingConfiguration().hasLogDestinationConfigs()) {
                        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .status(OperationStatus.FAILED)
                            .errorCode(HandlerErrorCode.AlreadyExists)
                            .message(String.format("Logging Configuration for %s already exists", cbModel.getResourceArn()))
                            .build();
                    }
                    return ProgressEvent.progress(cbModel, cbContext);
                });
    }
}
