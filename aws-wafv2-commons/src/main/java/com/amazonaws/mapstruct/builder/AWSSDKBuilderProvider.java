package com.amazonaws.mapstruct.builder;

import org.mapstruct.ap.spi.DefaultBuilderProvider;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Custom Builder Provider for AWS SDK generated model objects.
 */
public class AWSSDKBuilderProvider extends DefaultBuilderProvider {

    private static final String BUILDER_IMPL_CLASS_NAME = "BuilderImpl";

    @Override
    protected Collection<ExecutableElement> findBuildMethods(TypeElement builderElement, TypeElement typeElement) {
        if (shouldIgnore(builderElement)) {
            return Collections.emptyList();
        }

        List<ExecutableElement> builderMethods = ElementFilter.methodsIn(builderElement.getEnclosedElements());
        List<ExecutableElement> buildMethods = new ArrayList<>();
        for (ExecutableElement buildMethod : builderMethods) {
            if (isBuildMethod(buildMethod, typeElement)) {
                buildMethods.add(buildMethod);
            }
        }

        if (buildMethods.isEmpty()) {
            Optional<? extends Element> builderImpl = typeElement.getEnclosedElements().stream()
                    .filter(e -> e.getSimpleName().toString().equals(BUILDER_IMPL_CLASS_NAME))
                    .findFirst();

            builderMethods = ElementFilter.methodsIn(builderImpl.get().getEnclosedElements());
            buildMethods = builderMethods.stream()
                    .filter(m -> isBuildMethod(m, typeElement))
                    .collect(Collectors.toList());
        }

        if (buildMethods.isEmpty()) {
            return findBuildMethods(
                    getTypeElement(builderElement.getSuperclass()),
                    typeElement
            );
        }

        return buildMethods;
    }

}
