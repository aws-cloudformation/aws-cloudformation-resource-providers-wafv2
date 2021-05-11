package software.amazon.wafv2.loggingconfiguration;

import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.wafv2.model.All;
import software.amazon.awssdk.services.wafv2.model.ListLoggingConfigurationsRequest;
import software.amazon.awssdk.services.wafv2.model.ListLoggingConfigurationsResponse;
import software.amazon.awssdk.services.wafv2.model.LoggingConfiguration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    private ListHandler handler;
    private LoggingConfiguration loggingConfiguration1;
    private LoggingConfiguration loggingConfiguration2;
    private List<LoggingConfiguration> loggingConfigurations = new ArrayList<>();
    private List<String> logDestinationConfigs = new ArrayList<String>();
    private List<software.amazon.awssdk.services.wafv2.model.Condition> conditions = new ArrayList<>();
    private List<software.amazon.awssdk.services.wafv2.model.Filter> filters = new ArrayList<>();
    private List<software.amazon.awssdk.services.wafv2.model.FieldToMatch> fieldsToMatch1 = new ArrayList<>();
    private List<software.amazon.awssdk.services.wafv2.model.FieldToMatch> fieldsToMatch2 = new ArrayList<>();

    public void setupHandler(){
        handler = new ListHandler();

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

        loggingConfigurations.add(loggingConfiguration1);
        loggingConfigurations.add(loggingConfiguration2);
    }

    @Test
    public void handleRequest_SimpleSuccess() {

        // Build and return a List Response
        ListLoggingConfigurationsResponse getResponse = ListLoggingConfigurationsResponse.builder().loggingConfigurations(loggingConfigurations).build();
        when(proxyClient.client().listLoggingConfigurations(any(ListLoggingConfigurationsRequest.class))).thenReturn(getResponse);

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(model).build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
