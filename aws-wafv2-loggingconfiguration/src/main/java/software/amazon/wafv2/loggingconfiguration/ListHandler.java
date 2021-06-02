package software.amazon.wafv2.loggingconfiguration;

import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Wafv2Client> proxyClient,
        final Logger logger) {

        return proxy.initiate("AWS-WAFv2-LoggingConfiguration::List", proxyClient, request.getDesiredResourceState(), callbackContext)
            .translateToServiceRequest((cbModel) -> Translator.translateToListRequest(request.getNextToken()))
            .makeServiceCall((cbRequest, cbProxyClient) -> cbProxyClient.injectCredentialsAndInvokeV2(cbRequest, cbProxyClient.client()::listLoggingConfigurations))
            .handleError(this::handleError)
            .done((cbRequest, cbResponse, cbClient, cbModel, cbContext) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModels(Translator.translateFromListResponse(cbResponse))
                    .status(OperationStatus.SUCCESS)
                    .nextToken(cbResponse.nextMarker())
                    .build());
    }
}
