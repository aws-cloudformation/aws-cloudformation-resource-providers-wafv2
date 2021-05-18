package software.amazon.wafv2.loggingconfiguration;

import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.services.wafv2.model.GetLoggingConfigurationRequest;
import software.amazon.awssdk.services.wafv2.model.GetLoggingConfigurationResponse;
import software.amazon.awssdk.services.wafv2.model.LoggingConfiguration;
import software.amazon.awssdk.services.wafv2.model.PutLoggingConfigurationRequest;
import software.amazon.awssdk.services.wafv2.model.PutLoggingConfigurationResponse;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
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
public class UpdateHandlerTest extends AbstractTestBase {

    private UpdateHandler handler;

    private LoggingConfiguration loggingConfiguration;
    private ResourceModel previousModel;
    private List<String> logDestinationConfigs = new ArrayList<String>();

    public void setupHandler(){
        handler = new UpdateHandler();

        logDestinationConfigs.add("firehose-arn");
        loggingConfiguration = LoggingConfiguration.builder()
                .resourceArn("resourcearn")
                .logDestinationConfigs(logDestinationConfigs)
                .build();

        // Previous Model
        previousModel = ResourceModel.builder().resourceArn("resourcearn").logDestinationConfigs(logDestinationConfigs).build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {

        // Build and return a Get Response
        GetLoggingConfigurationResponse getResponse = GetLoggingConfigurationResponse.builder().loggingConfiguration(loggingConfiguration).build();
        when(proxyClient.client().getLoggingConfiguration(any(GetLoggingConfigurationRequest.class))).thenReturn(getResponse);

        // Build and return a Put Response
        PutLoggingConfigurationResponse putResponse = PutLoggingConfigurationResponse.builder().loggingConfiguration(loggingConfiguration).build();
        when(proxyClient.client().putLoggingConfiguration(any(PutLoggingConfigurationRequest.class))).thenReturn(putResponse);

        final ResourceModel model = ResourceModel.builder()
                .resourceArn("resourcearn")
                .logDestinationConfigs(logDestinationConfigs)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .previousResourceState(previousModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_PreExistence_Failure() {

        // Build and return a Get Response
        //GetLoggingConfigurationResponse getResponse = GetLoggingConfigurationResponse.builder().loggingConfiguration(loggingConfiguration).build();
        when(proxyClient.client().getLoggingConfiguration(any(GetLoggingConfigurationRequest.class))).thenThrow(WafNonexistentItemException.class);

        final ResourceModel model = ResourceModel.builder()
                .resourceArn("resourcearn")
                .logDestinationConfigs(logDestinationConfigs)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(previousModel)
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
    }

    @Test
    @org.junit.jupiter.api.Tag("noSdkInteraction")
    public void handleRequest_InvalidRequest_Failure() {

        List<ResourceModel> models = new ArrayList<ResourceModel>();
        List<String> newLogDestinationConfigs = new ArrayList<String>();
        newLogDestinationConfigs.add("firehose-arn-changed");

        //Pass a model which has no Resource ARN
        final ResourceModel model1 = ResourceModel.builder().logDestinationConfigs(logDestinationConfigs).build();

        //Pass a model which has no Log Destination Configs
        final ResourceModel model2 = ResourceModel.builder().resourceArn("resourcearn").build();

        //Pass a model which changes Log Destination Configs
        final ResourceModel model3 = ResourceModel.builder().resourceArn("resourcearn").logDestinationConfigs(newLogDestinationConfigs).build();

        //Pass a model which changes Log Destination Configs
        final ResourceModel model4 = ResourceModel.builder().resourceArn("resourcearn").logDestinationConfigs(logDestinationConfigs).managedByFirewallManager(true).build();
        
        models.add(model1);
        models.add(model2);
        models.add(model3);
        models.add(model4);

        for(int i = 0; i < models.size(); i++) {

            final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                    .previousResourceState(previousModel)
                    .desiredResourceState(models.get(i))
                    .build();

            final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
            assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
            assertThat(response.getResourceModels()).isNull();
            if (i == 0) {
                assertThat(response.getMessage()).isEqualTo("Resource ARN cannot be empty");
                assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
            } else if (i == 1) {
                assertThat(response.getMessage()).isEqualTo("LogDestinationConfigs cannot be empty");
                assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
            } else if (i == 2) {
                assertThat(response.getMessage()).isEqualTo("LogDestinationConfigs is a create only property");
                assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
            } else {
                assertThat(response.getMessage()).isEqualTo("ManagedByFirewallManager is a Read-Only property");
                assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);                
            }
        }
    }
}
