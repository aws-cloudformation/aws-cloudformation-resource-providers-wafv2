package software.amazon.wafv2.loggingconfiguration;

import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.services.wafv2.model.GetLoggingConfigurationRequest;
import software.amazon.awssdk.services.wafv2.model.GetLoggingConfigurationResponse;
import software.amazon.awssdk.services.wafv2.model.LoggingConfiguration;
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
public class ReadHandlerTest extends AbstractTestBase {

    private ReadHandler handler;
    private LoggingConfiguration loggingConfiguration;
    private List<String> logDestinationConfigs = new ArrayList<String>();

    public void setupHandler(){
        handler = new ReadHandler();

        logDestinationConfigs.add("firehose-arn");
        loggingConfiguration = LoggingConfiguration.builder()
                .resourceArn("resourcearn")
                .logDestinationConfigs(logDestinationConfigs)
                .managedByFirewallManager(true)
                .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {

        // Build and return a Get Response
        GetLoggingConfigurationResponse getResponse = GetLoggingConfigurationResponse.builder().loggingConfiguration(loggingConfiguration).build();
        when(proxyClient.client().getLoggingConfiguration(any(GetLoggingConfigurationRequest.class))).thenReturn(getResponse);

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
