package software.amazon.wafv2.loggingconfiguration;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Wafv2Client> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();

        if (StringUtils.isBlank(model.getResourceArn())) {
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.NotFound, "Resource ARN cannot be empty");
        }

        if (model.getLogDestinationConfigs() == null || model.getLogDestinationConfigs().isEmpty()) {
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest, "LogDestinationConfigs cannot be empty");
        }

        if (!previousModel.getLogDestinationConfigs().get(0).equals(model.getLogDestinationConfigs().get(0))) {
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest, "LogDestinationConfigs is a create only property");
        }
        
        if (!Objects.isNull(model.getManagedByFirewallManager())) {
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest, "ManagedByFirewallManager is a Read-Only property");
        }

        // Check if the Logging Configuration exists, upon a successful read, proceed to change the configuration
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger))
                .onSuccess(progress -> putLoggingConfiguration(proxy, proxyClient, model, callbackContext))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }
}
