package com.amazonaws.wafv2.ipset.converters;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import software.amazon.awssdk.services.wafv2.model.Tag;

import java.util.List;

@Mapper
public interface TagConverter {
    TagConverter INSTANCE = Mappers.getMapper(TagConverter.class);

    //---------------------------------------------------------------------
    // Tag
    //---------------------------------------------------------------------
    software.amazon.awssdk.services.wafv2.model.Tag convert(com.amazonaws.wafv2.ipset.Tag source);
    com.amazonaws.wafv2.ipset.Tag invert(Tag source);

}
