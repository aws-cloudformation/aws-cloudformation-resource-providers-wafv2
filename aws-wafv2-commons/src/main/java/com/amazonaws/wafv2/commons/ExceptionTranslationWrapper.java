package com.amazonaws.wafv2.commons;

import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.services.wafv2.model.WafAssociatedItemException;
import software.amazon.awssdk.services.wafv2.model.WafDuplicateItemException;
import software.amazon.awssdk.services.wafv2.model.WafInternalErrorException;
import software.amazon.awssdk.services.wafv2.model.WafInvalidParameterException;
import software.amazon.awssdk.services.wafv2.model.WafInvalidResourceException;
import software.amazon.awssdk.services.wafv2.model.WafLimitsExceededException;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
import software.amazon.awssdk.services.wafv2.model.WafOptimisticLockException;
import software.amazon.awssdk.services.wafv2.model.WafServiceLinkedRoleErrorException;
import software.amazon.awssdk.services.wafv2.model.WafUnavailableEntityException;
import software.amazon.awssdk.services.wafv2.model.Wafv2Exception;
import software.amazon.cloudformation.proxy.HandlerErrorCode;

import java.util.Map;

public abstract class ExceptionTranslationWrapper<T> {

    private static final String MISSING_CRITICAL_INFO = "Critical information is missing in your request";
    private static final Map<Class<? extends Wafv2Exception>, HandlerErrorCode> MAPPING;

    static {
        MAPPING = ImmutableMap.<Class<? extends Wafv2Exception>, HandlerErrorCode>builder()
                .put(WafAssociatedItemException.class, HandlerErrorCode.ResourceConflict)
                .put(WafDuplicateItemException.class, HandlerErrorCode.AlreadyExists)
                .put(WafInternalErrorException.class, HandlerErrorCode.ServiceInternalError)
                .put(WafInvalidParameterException.class, HandlerErrorCode.InvalidRequest)
                .put(WafInvalidResourceException.class, HandlerErrorCode.InvalidRequest)
                .put(WafLimitsExceededException.class, HandlerErrorCode.ServiceLimitExceeded)
                .put(WafNonexistentItemException.class, HandlerErrorCode.NotFound)
                .put(WafOptimisticLockException.class, HandlerErrorCode.ResourceConflict)
                .put(WafServiceLinkedRoleErrorException.class, HandlerErrorCode.InvalidCredentials)
                .put(WafUnavailableEntityException.class, HandlerErrorCode.NotStabilized)
                .build();
    }

    /**
     * Execute code with exception translation.
     *
     * @return T result of the function.
     * @throws RuntimeException WAFAPIException.
     */
    public final T execute() throws RuntimeException {
        try {
            return doWithTranslation();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Override with code to run and have exceptions translated.
     *
     * @return T result of the function.
     * @throws RuntimeException RuntimeException
     */
    public abstract T doWithTranslation() throws RuntimeException;

    /**
     * Translate exception to CloudFormation HandlerErrorCode.
     *
     * @param error error thrown by Customer API.
     * @return HandlerErrorCode code gets returned to customer.
     */
    public static HandlerErrorCode translateExceptionIntoErrorCode(RuntimeException error) {
        //based on CFN contract, we should throw HandlerErrorCode.NotFound if we are missing critical info
        if (error instanceof Wafv2Exception
                && error.getMessage() != null
                && error.getMessage().startsWith(MISSING_CRITICAL_INFO)) {
            return HandlerErrorCode.NotFound;
        }

        HandlerErrorCode translatedErrorCode = MAPPING.get(error.getClass());

        return translatedErrorCode == null ? HandlerErrorCode.GeneralServiceException : translatedErrorCode;
    }
}
