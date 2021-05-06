package com.amazonaws.wafv2.commons;

import lombok.NonNull;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.wafv2.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.wafv2.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Helper containing common handler operations.
 */
public class HandlerHelper {

    private static final int MAX_LENGTH_RESOURCE_NAME = 50;

    /**
     * For named resources, use this method to safely generate a user friendly
     * resource name when the customer does not pass in an explicit name.
     *
     * @param <T>     the resource model type parameter
     * @param request the resource handler request, not null
     * @return generated name string, not null or empty
     */
    public static <T> String generateName(@NonNull final ResourceHandlerRequest<T> request) {
        return IdentifierUtils.generateResourceIdentifier(
                request.getLogicalResourceIdentifier(),
                request.getClientRequestToken(),
                MAX_LENGTH_RESOURCE_NAME
        );
    }

    /**
     * Returns the list of converted tags for a given resourceARN.
     *
     * @param <T>             the Tag type parameter
     * @param proxy           the AWSClient proxy, not null
     * @param client          the client, not null
     * @param resourceARN     the resource ARN, not null
     * @param convertFunction the convert function to convert from {@link Tag} to T
     * @return the list of tags for the resourceARN, will not be null
     */
    public static <T> List<T> getConvertedTags(@NonNull final AmazonWebServicesClientProxy proxy,
                                               @NonNull final Wafv2Client client,
                                               @NonNull final String resourceARN,
                                               @NonNull final Function<Tag, T> convertFunction) {
        ListTagsForResourceRequest request = ListTagsForResourceRequest.builder()
                .resourceARN(resourceARN)
                .build();
        ListTagsForResourceResponse response = proxy.injectCredentialsAndInvokeV2(
                        request, client::listTagsForResource);
        List<Tag> tags = response.tagInfoForResource().tagList();
        return tags.stream()
                .map(convertFunction)
                .collect(Collectors.toList());
    }
}
