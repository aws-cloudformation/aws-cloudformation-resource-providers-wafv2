package com.amazonaws.wafv2.regexpatternset.converters;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;
import software.amazon.awssdk.services.wafv2.model.Tag;


/**
 * Common types to support RegexPatternSet conversion.
 */
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface RegexPatternSetCommonsConverter {
    RegexPatternSetCommonsConverter INSTANCE = Mappers.getMapper(RegexPatternSetCommonsConverter.class);

    //---------------------------------------------------------------------
    // Tag
    //---------------------------------------------------------------------
    Tag convert(com.amazonaws.wafv2.regexpatternset.Tag source);
    com.amazonaws.wafv2.regexpatternset.Tag invert(Tag source);
}
