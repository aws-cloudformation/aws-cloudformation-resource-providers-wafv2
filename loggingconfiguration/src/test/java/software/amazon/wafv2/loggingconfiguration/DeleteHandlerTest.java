package software.amazon.wafv2.loggingconfiguration;

import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.services.wafv2.model.DeleteLoggingConfigurationRequest;
import software.amazon.awssdk.services.wafv2.model.DeleteLoggingConfigurationResponse;
import software.amazon.awssdk.services.wafv2.model.GetLoggingConfigurationRequest;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
import software.amazon.awssdk.services.wafv2.model.Wafv2Exception;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

    private DeleteHandler handler;

    private List<String> logDestinationConfigs = new ArrayList<String>();

    public void setupHandler(){
        handler = new DeleteHandler();

        logDestinationConfigs.add("firehose-arn");
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        
        // Build and Return a Delete Response
        DeleteLoggingConfigurationResponse deleteResponse = DeleteLoggingConfigurationResponse.builder().build();
        when(proxyClient.client().deleteLoggingConfiguration(any(DeleteLoggingConfigurationRequest.class))).thenReturn(deleteResponse);
        
        // Build and return a Get Response 
        when(proxyClient.client().getLoggingConfiguration(any(GetLoggingConfigurationRequest.class))).thenThrow(WafNonexistentItemException.class);

        final ResourceModel model = ResourceModel.builder().resourceArn("resourcearn").build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(model).build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
    
    @Test
    public void handleRequest_Stabilization_Failure() {
        
        // Build and Return a Delete Response
        DeleteLoggingConfigurationResponse deleteResponse = DeleteLoggingConfigurationResponse.builder().build();
        when(proxyClient.client().deleteLoggingConfiguration(any(DeleteLoggingConfigurationRequest.class))).thenReturn(deleteResponse);
        
        // Build and return a Get Response 
        when(proxyClient.client().getLoggingConfiguration(any(GetLoggingConfigurationRequest.class))).thenThrow(Wafv2Exception.class);

        final ResourceModel model = ResourceModel.builder().resourceArn("resourcearn").build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(model).build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.GeneralServiceException);
    }
    
    @Test
    @org.junit.jupiter.api.Tag("noSdkInteraction")
    public void handleRequest_InvalidRequest_Failure() {

        //Pass a model which has no Resource ARN 
        final ResourceModel model1 = ResourceModel.builder().logDestinationConfigs(logDestinationConfigs).build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(model1).build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo("Resource ARN cannot be empty");   
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }
}
