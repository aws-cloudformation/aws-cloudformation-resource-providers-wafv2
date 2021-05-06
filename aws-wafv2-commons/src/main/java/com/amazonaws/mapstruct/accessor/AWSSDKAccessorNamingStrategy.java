package com.amazonaws.mapstruct.accessor;

import org.mapstruct.ap.spi.DefaultAccessorNamingStrategy;
import org.mapstruct.ap.spi.util.IntrospectorUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Accessor naming strategy to work with fluent getters from AWS SDK Models
 */
public class AWSSDKAccessorNamingStrategy extends DefaultAccessorNamingStrategy {
    private static final Set<String> NON_GETTER_NAMES;

    static {
        Set<String> nonGetters = new HashSet<>();
        nonGetters.add("toString");
        nonGetters.add("hashCode");
        nonGetters.add("getClass");
        nonGetters.add("clone");
        nonGetters.add("toBuilder");
        nonGetters.add("sdkFields");
        nonGetters.add("copy");
        NON_GETTER_NAMES = Collections.unmodifiableSet(nonGetters);
    }

    @Override
    public boolean isSetterMethod(ExecutableElement method) {
        return isEligibleSetter(method) && super.isSetterMethod(method);
    }

    @Override
    protected boolean isFluentSetter(ExecutableElement method) {
        return isEligibleSetter(method) && super.isFluentSetter(method);
    }

    @Override
    public boolean isGetterMethod(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        if (!method.getParameters().isEmpty()
                || method.isDefault()
                || method.getReturnType().getKind() == TypeKind.VOID
                || NON_GETTER_NAMES.contains(methodName)) {
            return false;
        }

        return true;
    }

    @Override
    public String getPropertyName(ExecutableElement getterOrSetterMethod) {
        String methodName = getterOrSetterMethod.getSimpleName().toString();

        if (this.isFluentSetter(getterOrSetterMethod)) {
            return methodName.startsWith("set") && methodName.length() > 3 && Character
                    .isUpperCase(methodName.charAt(3)) ? IntrospectorUtils
                    .decapitalize(methodName.substring(3)) : methodName;
        } else {
            int startIndex = 0;
            if (methodName.startsWith("is")) {
                startIndex = 2;
            } else if (methodName.startsWith("set") || methodName.startsWith("get")) {
                startIndex = 3;
            }

            return IntrospectorUtils.decapitalize(methodName.substring(startIndex));
        }
    }

    private boolean isEligibleSetter(ExecutableElement method) {
        if (method.isDefault()) {
            return false;
        }

        //prefer collection over array parameters as valid setters from AWS SDK
        if (method.getParameters().size() == 1
                && method.getParameters().get(0).asType().getKind() == TypeKind.ARRAY) {
            return false;
        }

        return true;
    }
}
