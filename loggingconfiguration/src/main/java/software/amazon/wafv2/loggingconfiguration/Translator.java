package software.amazon.wafv2.loggingconfiguration;

import software.amazon.awssdk.services.wafv2.model.DeleteLoggingConfigurationRequest;
import software.amazon.awssdk.services.wafv2.model.GetLoggingConfigurationRequest;
import software.amazon.awssdk.services.wafv2.model.GetLoggingConfigurationResponse;
import software.amazon.awssdk.services.wafv2.model.ListLoggingConfigurationsRequest;
import software.amazon.awssdk.services.wafv2.model.ListLoggingConfigurationsResponse;
import software.amazon.awssdk.services.wafv2.model.LoggingConfiguration;
import software.amazon.awssdk.services.wafv2.model.PutLoggingConfigurationRequest;
import software.amazon.awssdk.services.wafv2.model.Scope;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.util.StringUtils;

public class Translator {

  /**
   * Request to create a resource
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static PutLoggingConfigurationRequest translateToCreateRequest(final ResourceModel model) {
      return PutLoggingConfigurationRequest.builder()
              .loggingConfiguration(translateToLoggingConfiguration(model))
              .build();
  }
  
  /**
   * Construct Logging Configuration from the Resource Model
   * @param model
   * @return A LoggingConfiguration Object
   */
  static LoggingConfiguration translateToLoggingConfiguration(final ResourceModel model) {
      
      return LoggingConfiguration.builder()
              .resourceArn(model.getResourceArn())
              .logDestinationConfigs(model.getLogDestinationConfigs().get(0)) // TODO: What happens if there is more than 1?
              .loggingFilter(translateToLoggingFilter(model.getLoggingFilter()))
              .managedByFirewallManager(model.getManagedByFirewallManager())
              .redactedFields(translateToSDKRedactedFields(model.getRedactedFields()))
              .build();
  }
  
  /**
   * Construct a SDK Logging Filter from the Resource Model
   * @param model
   * @return A SDK LoggingFilter Object
   */
  static software.amazon.awssdk.services.wafv2.model.LoggingFilter translateToLoggingFilter(final LoggingFilter loggingFilter) {
      
      if(!Objects.isNull(loggingFilter)) {
          software.amazon.awssdk.services.wafv2.model.LoggingFilter.Builder loggingFilterBuilder = software.amazon.awssdk.services.wafv2.model.LoggingFilter.builder();
          
          if(!Objects.isNull(loggingFilter.getDefaultBehavior())) loggingFilterBuilder = loggingFilterBuilder.defaultBehavior(loggingFilter.getDefaultBehavior());
          
          return loggingFilterBuilder.filters(translateToSDKFilters(loggingFilter.getFilters())).build();
      }
      return null;
  }
  
  /**
   * Construct Filters from the Resource Model
   * @param model
   * @return An SDK List<Filter> Object
   */
  static List<software.amazon.awssdk.services.wafv2.model.Filter> translateToSDKFilters(final List<Filter> filters) {
      return streamOfOrEmpty(filters)
              .map(filter -> translateToSDKFilter(filter))
              .collect(Collectors.toList());
  }
  
  /**
   * Construct a single SDK Filter Object from the Model Filter
   * @param model
   * @return An SDK Filter Object
   */
  static software.amazon.awssdk.services.wafv2.model.Filter translateToSDKFilter(final Filter filter) {
      return software.amazon.awssdk.services.wafv2.model.Filter.builder()
              .behavior(filter.getBehavior())
              .requirement(filter.getRequirement())
              .conditions(translateToSDKConditions(filter.getConditions()))
              .build();
  }
  
  /**
   * Construct a List of SDK Conditions Objects for a given Model Condition
   * @param model
   * @return An SDK List<Condition> Object
   */
  static List<software.amazon.awssdk.services.wafv2.model.Condition> translateToSDKConditions(final List<Condition> conditions) {
      return streamOfOrEmpty(conditions)
              .map(condition -> translateToSDKCondition(condition))
              .collect(Collectors.toList());
  }
  
  /**
   * Construct a single SDK Condition given a Model Condition type
   * @param A Model Condition Object
   * @return An SDK Condition Object
   */
  static software.amazon.awssdk.services.wafv2.model.Condition translateToSDKCondition(final Condition condition) {
      
      software.amazon.awssdk.services.wafv2.model.Condition.Builder conditionBuilder = software.amazon.awssdk.services.wafv2.model.Condition.builder();
      
      if (!Objects.isNull(condition.getActionCondition())) 
          conditionBuilder = conditionBuilder.actionCondition(software.amazon.awssdk.services.wafv2.model.ActionCondition.builder()
                  .action(condition.getActionCondition().getAction())
                  .build());
      
      if (!Objects.isNull(condition.getLabelNameCondition())) 
          conditionBuilder = conditionBuilder.labelNameCondition(software.amazon.awssdk.services.wafv2.model.LabelNameCondition.builder()
                  .labelName(condition.getLabelNameCondition().getLabelName())
                  .build());
      
      return conditionBuilder.build();
  }
  
  /**
   * Construct a List of Field to Match Objects for Redacted Fields
   * @param model
   * @return A List<FieldToMatch> Object
   */
  static List<software.amazon.awssdk.services.wafv2.model.FieldToMatch> translateToSDKRedactedFields(final List<FieldToMatch> fieldsToMatch) {
      return streamOfOrEmpty(fieldsToMatch)
              .map(field -> translateToSDKFieldToMatch(field))
              .collect(Collectors.toList());
  }
  
  /**
   * Construct a single SDK FieldToMatch Object from a Model FieldToMatch
   * @param model
   * @return A FieldToMatch Object
   */
  static software.amazon.awssdk.services.wafv2.model.FieldToMatch translateToSDKFieldToMatch(final FieldToMatch field) {
      
      software.amazon.awssdk.services.wafv2.model.FieldToMatch.Builder fieldBuilder = software.amazon.awssdk.services.wafv2.model.FieldToMatch.builder();
      
      if (!Objects.isNull(field.getMethod())) fieldBuilder = fieldBuilder.method(software.amazon.awssdk.services.wafv2.model.Method.builder().build());
      if (!Objects.isNull(field.getQueryString())) fieldBuilder = fieldBuilder.queryString(software.amazon.awssdk.services.wafv2.model.QueryString.builder().build());
      if (!Objects.isNull(field.getUriPath())) fieldBuilder = fieldBuilder.uriPath(software.amazon.awssdk.services.wafv2.model.UriPath.builder().build());
      
      if (!Objects.isNull(field.getSingleHeader()) && !StringUtils.isNullOrEmpty(field.getSingleHeader().getName())) 
          fieldBuilder = fieldBuilder.singleHeader(software.amazon.awssdk.services.wafv2.model.SingleHeader.builder().name(field.getSingleHeader().getName()).build());
      
      return fieldBuilder.jsonBody(translateToSDKJsonBody(field.getJsonBody())).build();
  }
  
  /**
   * Construct a single SDK JsonBody from a Model JsonBody Object
   * @param model
   * @return A FieldToMatch Object
   */
  static software.amazon.awssdk.services.wafv2.model.JsonBody translateToSDKJsonBody(final JsonBody jsonBody) {
      
      if (!Objects.isNull(jsonBody)) {
          software.amazon.awssdk.services.wafv2.model.JsonBody.Builder jsonBodyBuilder = software.amazon.awssdk.services.wafv2.model.JsonBody.builder();
          
          jsonBodyBuilder = jsonBodyBuilder.invalidFallbackBehavior(jsonBody.getInvalidFallbackBehavior()).matchScope(jsonBody.getMatchScope());
          
          if (Objects.isNull(jsonBody.getMatchPattern().getAll())) 
              return jsonBodyBuilder.matchPattern(software.amazon.awssdk.services.wafv2.model.JsonMatchPattern.builder()
                      .includedPaths(jsonBody.getMatchPattern().getIncludedPaths())
                      .build()).build();
          
          return jsonBodyBuilder.matchPattern(software.amazon.awssdk.services.wafv2.model.JsonMatchPattern.builder()
                      .includedPaths(jsonBody.getMatchPattern().getIncludedPaths())
                      .all(software.amazon.awssdk.services.wafv2.model.All.builder().build())
                      .build())
                  .build();
      }
      return null;

  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static GetLoggingConfigurationRequest translateToReadRequest(final ResourceModel model) {
    return GetLoggingConfigurationRequest.builder().resourceArn(model.getResourceArn()).build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param awsResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final GetLoggingConfigurationResponse getResponse) {
    return ResourceModel.builder()
            .resourceArn(getResponse.loggingConfiguration().resourceArn())
            .managedByFirewallManager(getResponse.loggingConfiguration().managedByFirewallManager())
            .logDestinationConfigs(getResponse.loggingConfiguration().logDestinationConfigs())
            .loggingFilter(translateToModelLoggingFilter(getResponse.loggingConfiguration().loggingFilter()))
            .redactedFields(translateToModelRedactedFields(getResponse.loggingConfiguration().redactedFields()))
            .build();
  }
  
  /**
   * Construct a single Logging Filter Object from the SDK Logging Filter
   * @param model
   * @return A Condition Object
   */
  static LoggingFilter translateToModelLoggingFilter(final software.amazon.awssdk.services.wafv2.model.LoggingFilter loggingFilter) {
      
      if(!Objects.isNull(loggingFilter)) {
          LoggingFilter.LoggingFilterBuilder loggingFilterBuilder = LoggingFilter.builder();
          
          if(!Objects.isNull(loggingFilter.defaultBehavior())) loggingFilterBuilder = loggingFilterBuilder.defaultBehavior(loggingFilter.defaultBehaviorAsString());
          
          return loggingFilterBuilder.filters(translateToModelFilters(loggingFilter.filters())).build();
      }
      return null;
  }
  
  /**
   * Translates resource object from sdk into a resource model
   * @param awsResponse the aws service describe resource response
   * @return model resource model
   */
  static List<Filter> translateToModelFilters(final List<software.amazon.awssdk.services.wafv2.model.Filter> filters) {
      return streamOfOrEmpty(filters)
              .map(filter -> translateToModelFilter(filter))
              .collect(Collectors.toList());
  }
  
  /**
   * Construct a single Model Filter Object from the SDK Filter
   * @param model
   * @return A Filter Object
   */
  static Filter translateToModelFilter(final software.amazon.awssdk.services.wafv2.model.Filter filter) {
      return Filter.builder()
              .behavior(filter.behaviorAsString())
              .requirement(filter.requirementAsString())
              .conditions(translateToModelConditions(filter.conditions()))
              .build();
  }
  
  /**
   * Translates SDK List of Conditions into Model Conditions
   * @param List of SDK Conditions
   * @return List<Condition>
   */
  static List<Condition> translateToModelConditions(final List<software.amazon.awssdk.services.wafv2.model.Condition> conditions) {
      return streamOfOrEmpty(conditions)
              .map(filter -> translateToModelCondition(filter))
              .collect(Collectors.toList());
  }
  
  /**
   * Construct a single Model Condition Object from the SDK Condition
   * @param model
   * @return A Condition Object
   */
  static Condition translateToModelCondition(final software.amazon.awssdk.services.wafv2.model.Condition condition) {
      
      Condition.ConditionBuilder conditionBuilder = Condition.builder();
      
      if(!Objects.isNull(condition.actionCondition())) 
          conditionBuilder = conditionBuilder.actionCondition(ActionCondition.builder().action(condition.actionCondition().actionAsString()).build());
      
      if(!Objects.isNull(condition.labelNameCondition())) 
          conditionBuilder = conditionBuilder.labelNameCondition(LabelNameCondition.builder().labelName(condition.labelNameCondition().labelName()).build());
      
      return conditionBuilder.build();
  }
  
  /**
   * Construct a List of Model Field to Match Objects for from SDK FieldToMatch List
   * @param model
   * @return A List<FieldToMatch> Object
   */
  static List<FieldToMatch> translateToModelRedactedFields(final List<software.amazon.awssdk.services.wafv2.model.FieldToMatch> fieldsToMatch) {
      return streamOfOrEmpty(fieldsToMatch)
              .map(field -> translateToModelFieldToMatch(field))
              .collect(Collectors.toList());
  }
  
  /**
   * Construct a single Model FieldToMatch Object
   * @param model
   * @return A FieldToMatch Object
   */
  static FieldToMatch translateToModelFieldToMatch(final software.amazon.awssdk.services.wafv2.model.FieldToMatch field) {
      
      FieldToMatch.FieldToMatchBuilder fieldBuilder = FieldToMatch.builder();
      
      if (!Objects.isNull(field.method())) fieldBuilder = fieldBuilder.method(new HashMap<String, Object>());
      if (!Objects.isNull(field.queryString())) fieldBuilder = fieldBuilder.queryString(new HashMap<String, Object>());
      if (!Objects.isNull(field.uriPath())) fieldBuilder = fieldBuilder.uriPath(new HashMap<String, Object>());
      
      
      if (!Objects.isNull(field.singleHeader()) && !StringUtils.isNullOrEmpty(field.singleHeader().name())) 
          fieldBuilder = fieldBuilder.singleHeader(SingleHeader.builder().name(field.singleHeader().name()).build());
      
      return fieldBuilder.jsonBody(translateToModelJsonBody(field.jsonBody())).build();
  }
  
  /**
   * Construct a single Model JsonBody from a SDK JsonBody Object
   * @param model
   * @return A FieldToMatch Object
   */
  static JsonBody translateToModelJsonBody(final software.amazon.awssdk.services.wafv2.model.JsonBody jsonBody) {
      
      if (!Objects.isNull(jsonBody)) {
          JsonBody.JsonBodyBuilder jsonBodyBuilder = JsonBody.builder();
          
          jsonBodyBuilder = jsonBodyBuilder.invalidFallbackBehavior(jsonBody.invalidFallbackBehaviorAsString()).matchScope(jsonBody.matchScopeAsString());
          
          // All is null, build and return without it
          if (Objects.isNull(jsonBody.matchPattern().all())) return jsonBodyBuilder.matchPattern(MatchPattern.builder().includedPaths(jsonBody.matchPattern().includedPaths()).build()).build();
          
          // All is not null, build and return with it
          return jsonBodyBuilder.matchPattern(MatchPattern.builder().includedPaths(jsonBody.matchPattern().includedPaths()).all(new HashMap<String, Object>()).build()).build();
      }
      return null;
  }
  
  /**
   * Request to delete a resource
   * @param model resource model
   * @return DeleteLoggingConfigurationRequest the aws service request to delete a resource
   */
  static DeleteLoggingConfigurationRequest translateToDeleteRequest(final ResourceModel model) {
      return DeleteLoggingConfigurationRequest.builder()
              .resourceArn(model.getResourceArn())
              .build();
  }

  /**
   * Request to list resources
   * @param nextToken token passed to the aws service list resources request
   * @return awsRequest the aws service request to list resources within aws account
   */
  static ListLoggingConfigurationsRequest translateToListRequest(final String nextToken) {
    return ListLoggingConfigurationsRequest.builder()
            .scope(Scope.REGIONAL)
            .nextMarker(nextToken)
            .build();
  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param listResponse the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListResponse(final ListLoggingConfigurationsResponse listResponse) {
    return streamOfOrEmpty(listResponse.loggingConfigurations())
        .map(resource -> ResourceModel.builder()
                .resourceArn(resource.resourceArn())
                .build())
        .collect(Collectors.toList());
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
