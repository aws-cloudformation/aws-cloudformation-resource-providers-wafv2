package software.amazon.wafv2.loggingconfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.common.collect.ImmutableList;

import software.amazon.awssdk.services.wafv2.model.All;
import software.amazon.awssdk.services.wafv2.model.GetLoggingConfigurationRequest;
import software.amazon.awssdk.services.wafv2.model.GetLoggingConfigurationResponse;
import software.amazon.awssdk.services.wafv2.model.LoggingConfiguration;
import software.amazon.awssdk.services.wafv2.model.PutLoggingConfigurationRequest;
import software.amazon.awssdk.services.wafv2.model.PutLoggingConfigurationResponse;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {
    
    private CreateHandler handler;
    private LoggingConfiguration loggingConfiguration1;
    private LoggingConfiguration loggingConfiguration2;
    private List<String> logDestinationConfigs = new ArrayList<String>();
    private List<software.amazon.awssdk.services.wafv2.model.Condition> conditions = new ArrayList<>();
    private List<software.amazon.awssdk.services.wafv2.model.Filter> filters = new ArrayList<>();
    private List<software.amazon.awssdk.services.wafv2.model.FieldToMatch> fieldsToMatch1 = new ArrayList<>();
    private List<software.amazon.awssdk.services.wafv2.model.FieldToMatch> fieldsToMatch2 = new ArrayList<>();

    public void setupHandler(){
        handler = new CreateHandler();
        
        // Setup both type of Conditions to add to Filters. 
        software.amazon.awssdk.services.wafv2.model.Condition condition1 = software.amazon.awssdk.services.wafv2.model.Condition.builder()
                .actionCondition(software.amazon.awssdk.services.wafv2.model.ActionCondition.builder().action("ALLOW").build())
                .build();
        
        software.amazon.awssdk.services.wafv2.model.Condition condition2 = software.amazon.awssdk.services.wafv2.model.Condition.builder()
                .labelNameCondition(software.amazon.awssdk.services.wafv2.model.LabelNameCondition.builder().labelName("testlabel").build())
                .build();
        
        conditions.add(condition1);
        conditions.add(condition2);
        
        // Setup the Logging Filter
        software.amazon.awssdk.services.wafv2.model.Filter filter = software.amazon.awssdk.services.wafv2.model.Filter.builder()
                .conditions(conditions)
                .behavior("KEEP")
                .requirement("MEETS_ANY")
                .build();
        filters.add(filter);
        
        software.amazon.awssdk.services.wafv2.model.LoggingFilter logFilter = software.amazon.awssdk.services.wafv2.model.LoggingFilter.builder()
                .defaultBehavior("KEEP")
                .filters(filters)
                .build();
        
        // Setup Redacted Fields
        fieldsToMatch1.add(software.amazon.awssdk.services.wafv2.model.FieldToMatch.builder()
                .uriPath(software.amazon.awssdk.services.wafv2.model.UriPath.builder().build())
                .method(software.amazon.awssdk.services.wafv2.model.Method.builder().build())
                .queryString(software.amazon.awssdk.services.wafv2.model.QueryString.builder().build())
                .singleHeader(software.amazon.awssdk.services.wafv2.model.SingleHeader.builder().name("password").build())
                .jsonBody(software.amazon.awssdk.services.wafv2.model.JsonBody.builder()
                        .invalidFallbackBehavior("EVALUATE_AS_STRING")
                        .matchPattern(software.amazon.awssdk.services.wafv2.model.JsonMatchPattern.builder()
                                .includedPaths(ImmutableList.of("/dogs/0/name", "/dogs/1/name"))
                                .build())
                        .matchScope(software.amazon.awssdk.services.wafv2.model.JsonMatchScope.ALL)
                        .build())
                .build());
        
        // A variation of Field to Match with null values
        fieldsToMatch2.add(software.amazon.awssdk.services.wafv2.model.FieldToMatch.builder()
                .jsonBody(software.amazon.awssdk.services.wafv2.model.JsonBody.builder()
                        .matchPattern(software.amazon.awssdk.services.wafv2.model.JsonMatchPattern.builder()
                                .includedPaths(ImmutableList.of("/dogs/0/name", "/dogs/1/name"))
                                .all(All.builder().build())
                                .build())
                        .matchScope(software.amazon.awssdk.services.wafv2.model.JsonMatchScope.ALL)
                        .build())
                .build());

        logDestinationConfigs.add("firehose-arn");
        loggingConfiguration1 = LoggingConfiguration.builder()
                .resourceArn("resourcearn")
                .logDestinationConfigs(logDestinationConfigs)
                .managedByFirewallManager(true)
                .loggingFilter(logFilter)
                .redactedFields(fieldsToMatch1)
                .build(); 
        
        loggingConfiguration2 = LoggingConfiguration.builder()
                .resourceArn("resourcearn")
                .logDestinationConfigs(logDestinationConfigs)
                .managedByFirewallManager(true)
                .loggingFilter(logFilter)
                .redactedFields(fieldsToMatch2)
                .build(); 
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        
        // Build and return a Get Response 
        GetLoggingConfigurationResponse getResponse = GetLoggingConfigurationResponse.builder().loggingConfiguration(loggingConfiguration1).build();
        when(proxyClient.client().getLoggingConfiguration(any(GetLoggingConfigurationRequest.class))).thenThrow(WafNonexistentItemException.class).thenReturn(getResponse);
        
        // Build and return a Put Response 
        PutLoggingConfigurationResponse putResponse = PutLoggingConfigurationResponse.builder().loggingConfiguration(loggingConfiguration1).build();
        when(proxyClient.client().putLoggingConfiguration(any(PutLoggingConfigurationRequest.class))).thenReturn(putResponse);

        List<Condition> modelConditions = new ArrayList<>();
        // Setup both type of Conditions to return. 
        Condition modelCondition1 = Condition.builder().actionCondition(ActionCondition.builder().action("ALLOW").build()).build();
        Condition modelCondition2 = Condition.builder().labelNameCondition(LabelNameCondition.builder().labelName("testlabel").build()).build();
        
        modelConditions.add(modelCondition1);
        modelConditions.add(modelCondition2);
        
        // Setup the Filter
        List<Filter> modelFilters = new ArrayList<>();
        Filter modelFilter = Filter.builder().conditions(modelConditions).behavior("KEEP").requirement("MEETS_ANY").build();
        modelFilters.add(modelFilter);
        
        LoggingFilter modelLogFilter = LoggingFilter.builder().defaultBehavior("KEEP").filters(modelFilters).build();
        
        // Setup Fields to Match 
        List<FieldToMatch> modelFieldsToMatch = new ArrayList<>();
        modelFieldsToMatch.add(FieldToMatch.builder()
                .uriPath(new HashMap<String, Object>())
                .method(new HashMap<String, Object>())
                .queryString(new HashMap<String, Object>())
                .singleHeader(SingleHeader.builder().name("password").build())
                .jsonBody(JsonBody.builder()
                        .invalidFallbackBehavior("EVALUATE_AS_STRING")
                        .matchPattern(MatchPattern.builder()
                                .includedPaths(ImmutableList.of("/dogs/0/name", "/dogs/1/name"))
                                .build())
                        .matchScope("ALL")
                        .build())
                .build());
        
        final ResourceModel model = ResourceModel.builder()
                .resourceArn("resourcearn")
                .managedByFirewallManager(true)
                .logDestinationConfigs(logDestinationConfigs)
                .loggingFilter(modelLogFilter)
                .redactedFields(modelFieldsToMatch)
                .build();

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
    public void handleRequest_SimpleRedactedFields_SimpleSuccess() {
        
        // Build and return a Get Response 
        GetLoggingConfigurationResponse getResponse = GetLoggingConfigurationResponse.builder().loggingConfiguration(loggingConfiguration2).build();
        when(proxyClient.client().getLoggingConfiguration(any(GetLoggingConfigurationRequest.class))).thenThrow(WafNonexistentItemException.class).thenReturn(getResponse);
        
        // Build and return a Put Response 
        PutLoggingConfigurationResponse putResponse = PutLoggingConfigurationResponse.builder().loggingConfiguration(loggingConfiguration2).build();
        when(proxyClient.client().putLoggingConfiguration(any(PutLoggingConfigurationRequest.class))).thenReturn(putResponse);

        List<Condition> modelConditions = new ArrayList<>();
        // Setup both type of Conditions to return. 
        Condition modelCondition1 = Condition.builder().actionCondition(ActionCondition.builder().action("ALLOW").build()).build();
        Condition modelCondition2 = Condition.builder().labelNameCondition(LabelNameCondition.builder().labelName("testlabel").build()).build();
        
        modelConditions.add(modelCondition1);
        modelConditions.add(modelCondition2);
        
        // Setup the Filter
        List<Filter> modelFilters = new ArrayList<>();
        Filter modelFilter = Filter.builder().conditions(modelConditions).behavior("KEEP").requirement("MEETS_ANY").build();
        modelFilters.add(modelFilter);
        
        LoggingFilter modelLogFilter = LoggingFilter.builder().defaultBehavior("KEEP").filters(modelFilters).build();
        
        // Setup Fields to Match 
        List<FieldToMatch> modelFieldsToMatch = new ArrayList<>();
        modelFieldsToMatch.add(FieldToMatch.builder()
                .jsonBody(JsonBody.builder()
                        .matchPattern(MatchPattern.builder()
                                .includedPaths(ImmutableList.of("/dogs/0/name", "/dogs/1/name"))
                                .all(new HashMap<String, Object>())
                                .build())
                        .matchScope("ALL")
                        .build())
                .build());
        
        final ResourceModel model = ResourceModel.builder()
                .resourceArn("resourcearn")
                .managedByFirewallManager(true)
                .logDestinationConfigs(logDestinationConfigs)
                .loggingFilter(modelLogFilter)
                .redactedFields(modelFieldsToMatch)
                .build();

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
    public void handleRequest_PreExistence_Failure() {
        
        // Build and return a Get Response 
        GetLoggingConfigurationResponse getResponse = GetLoggingConfigurationResponse.builder().loggingConfiguration(loggingConfiguration1).build();
        when(proxyClient.client().getLoggingConfiguration(any(GetLoggingConfigurationRequest.class))).thenReturn(getResponse);

        final ResourceModel model = ResourceModel.builder()
                .resourceArn("resourcearn")
                .managedByFirewallManager(true)
                .logDestinationConfigs(logDestinationConfigs)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(model).build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AlreadyExists);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo("Logging Configuration for resourcearn already exists");
    }
    
    @Test
    @org.junit.jupiter.api.Tag("noSdkInteraction")
    public void handleRequest_InvalidRequest_Failure() {
        
        List<ResourceModel> models = new ArrayList<ResourceModel>();

        //Pass a model which has no Resource ARN 
        final ResourceModel model1 = ResourceModel.builder().logDestinationConfigs(logDestinationConfigs).build();
        
        //Pass a model which has no Log Destination Configs
        final ResourceModel model2 = ResourceModel.builder().resourceArn("resourcearn").build();
        
        models.add(model1);
        models.add(model2);

        for(int i = 0; i < models.size(); i++) { 
            final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(models.get(i)).build();

            final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
            assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
            assertThat(response.getResourceModels()).isNull();
            if (i == 0) {
                assertThat(response.getMessage()).isEqualTo("Resource ARN cannot be empty");   
            } else {
                assertThat(response.getMessage()).isEqualTo("LogDestinationConfigs cannot be empty");
            }
            assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
        }
    }
}
