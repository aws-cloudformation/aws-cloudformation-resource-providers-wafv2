package software.amazon.wafv2.loggingconfiguration;

import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.awssdk.services.wafv2.model.WafInternalErrorException;
import software.amazon.awssdk.services.wafv2.model.WafInvalidOperationException;
import software.amazon.awssdk.services.wafv2.model.WafInvalidParameterException;
import software.amazon.awssdk.services.wafv2.model.WafLimitsExceededException;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
import software.amazon.awssdk.services.wafv2.model.WafServiceLinkedRoleErrorException;
import software.amazon.awssdk.services.wafv2.model.Wafv2Request;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
  @Override
  public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final Logger logger) {
    return handleRequest(
      proxy,
      request,
      callbackContext != null ? callbackContext : new CallbackContext(),
      proxy.newProxy(ClientBuilder::getClient),
      logger
    );
  }

  protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final ProxyClient<Wafv2Client> proxyClient,
    final Logger logger);


  /**
   * Create Logging Configurations since one doesnt exist already
   * @param proxy
   * @param proxyClient
   * @param model
   * @param callbackContext
   * @return Progress Event Object which will proceed to enable delete on termination
   */
  protected ProgressEvent<ResourceModel, CallbackContext> putLoggingConfiguration(final AmazonWebServicesClientProxy proxy,
          final ProxyClient<Wafv2Client> proxyClient,
          final ResourceModel model,
          final CallbackContext callbackContext) {

      return proxy.initiate("AWS-WAFv2-LoggingConfiguration::Create::PutLoggingConfiguration", proxyClient, model, callbackContext)
              .translateToServiceRequest(Translator::translateToCreateRequest)
              .makeServiceCall((cbRequest, cbProxyClient) -> cbProxyClient.injectCredentialsAndInvokeV2(cbRequest, cbProxyClient.client()::putLoggingConfiguration))
              .handleError(this::handleError)
              .progress();
  }

  public ProgressEvent<ResourceModel, CallbackContext> handleError(final Wafv2Request request,
          final Exception e,
          final ProxyClient<Wafv2Client> proxyClient,
          final ResourceModel resourceModel,
          final CallbackContext callbackContext) {

      BaseHandlerException ex;

      if (e instanceof WafLimitsExceededException || e instanceof WafServiceLinkedRoleErrorException) {
          // we can retry this because it's just throttling.
          throw new CfnThrottlingException(e);
          // Convert all other exceptions to CFN Exceptions so we can get an error code
      } else if (e instanceof WafNonexistentItemException ) {
          ex = new CfnNotFoundException(e);
      } else if (e instanceof WafInvalidParameterException || e instanceof WafInvalidOperationException ) {
          ex = new CfnInvalidRequestException(e);
      } else if (e instanceof WafInternalErrorException) {
          ex = new CfnInternalFailureException(e);
      } else {
          ex = new CfnGeneralServiceException(e);
      }
      return ProgressEvent.failed(resourceModel, callbackContext, ex.getErrorCode(), ex.getMessage());
  }
}
