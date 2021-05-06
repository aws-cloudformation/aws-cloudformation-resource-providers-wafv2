package com.amazonaws.wafv2.commons;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;
import software.amazon.awssdk.core.retry.backoff.EqualJitterBackoffStrategy;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.cloudformation.LambdaWrapper;

import java.time.Duration;

public class CustomerAPIClientBuilder {

    private static final BackoffStrategy BACKOFF_THROTTLING_STRATEGY =
            EqualJitterBackoffStrategy.builder()
                    .baseDelay(Duration.ofMillis(1500)) //account for jitter so 1st retry is ~1 sec
                    .maxBackoffTime(SdkDefaultRetrySetting.MAX_BACKOFF)
                    .build();

    private static final RetryPolicy RETRY_POLICY =
            RetryPolicy.builder()
                    .numRetries(5) //average delay is ~30 sec if all retries attempted
                    .retryCondition(RetryCondition.defaultRetryCondition())
                    .throttlingBackoffStrategy(BACKOFF_THROTTLING_STRATEGY)
                    .build();

    private CustomerAPIClientBuilder() {
    }

    public static Wafv2Client getClient() {
        return Wafv2Client.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryPolicy(RETRY_POLICY)
                        .build())
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}
