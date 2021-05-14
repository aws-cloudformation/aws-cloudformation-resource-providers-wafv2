package software.amazon.wafv2.loggingconfiguration;

import software.amazon.awssdk.services.wafv2.model.DeleteLoggingConfigurationRequest;
import software.amazon.awssdk.services.wafv2.model.GetLoggingConfigurationRequest;
import software.amazon.awssdk.services.wafv2.model.GetLoggingConfigurationResponse;
import software.amazon.awssdk.services.wafv2.model.ListLoggingConfigurationsRequest;
import software.amazon.awssdk.services.wafv2.model.ListLoggingConfigurationsResponse;
import software.amazon.awssdk.services.wafv2.model.LoggingConfiguration;
import software.amazon.awssdk.services.wafv2.model.PutLoggingConfigurationRequest;
import software.amazon.awssdk.services.wafv2.model.Scope;

import software.amazon.wafv2.converters.Converter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Translator {

  /**
   * Request to create a resource
   * @param model resource model
   * @return PutLoggingConfigurationRequest the aws service request to create a resource
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
              .logDestinationConfigs(model.getLogDestinationConfigs().get(0))
              .loggingFilter(Converter.INSTANCE.convert(model.getLoggingFilter()))
              .managedByFirewallManager(model.getManagedByFirewallManager())
              .redactedFields(translateToSDKRedactedFields(model.getRedactedFields()))
              .build();
  }

  /**
   * Construct a List of Field to Match Objects for Redacted Fields
   * @param model
   * @return A List<FieldToMatch> Object
   */
  static List<software.amazon.awssdk.services.wafv2.model.FieldToMatch> translateToSDKRedactedFields(final List<FieldToMatch> fieldsToMatch) {
      return streamOfOrEmpty(fieldsToMatch)
              .map(field -> Converter.INSTANCE.convert(field))
              .collect(Collectors.toList());
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return GetLoggingConfigurationRequest the aws service request to describe a resource
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
            .loggingFilter(Converter.INSTANCE.invert(getResponse.loggingConfiguration().loggingFilter()))
            .redactedFields(translateToModelRedactedFields(getResponse.loggingConfiguration().redactedFields()))
            .build();
  }

  /**
   * Construct a List of Model Field to Match Objects for from SDK FieldToMatch List
   * @param model
   * @return A List<FieldToMatch> Object
   */
  static List<FieldToMatch> translateToModelRedactedFields(final List<software.amazon.awssdk.services.wafv2.model.FieldToMatch> fieldsToMatch) {
      return streamOfOrEmpty(fieldsToMatch)
              .map(field -> Converter.INSTANCE.invert(field))
              .collect(Collectors.toList());
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
   * @return ListLoggingConfigurationsRequest the aws service request to list resources within aws account
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
