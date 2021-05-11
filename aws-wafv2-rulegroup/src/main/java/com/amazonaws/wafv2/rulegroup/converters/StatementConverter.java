package com.amazonaws.wafv2.rulegroup.converters;

import software.amazon.cloudformation.exceptions.TerminalException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import com.amazonaws.util.Base64;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.wafv2.model.AndStatement;
import software.amazon.awssdk.services.wafv2.model.ByteMatchStatement;
import software.amazon.awssdk.services.wafv2.model.GeoMatchStatement;
import software.amazon.awssdk.services.wafv2.model.IPSetReferenceStatement;
import software.amazon.awssdk.services.wafv2.model.NotStatement;
import software.amazon.awssdk.services.wafv2.model.OrStatement;
import software.amazon.awssdk.services.wafv2.model.RateBasedStatement;
import software.amazon.awssdk.services.wafv2.model.RegexPatternSetReferenceStatement;
import software.amazon.awssdk.services.wafv2.model.SizeConstraintStatement;
import software.amazon.awssdk.services.wafv2.model.SqliMatchStatement;
import software.amazon.awssdk.services.wafv2.model.Statement;
import software.amazon.awssdk.services.wafv2.model.XssMatchStatement;
import software.amazon.awssdk.services.wafv2.model.LabelMatchStatement;

/**
 * Converter for converting all statement types
 */
@Mapper(uses = StatementCommonsConverter.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface StatementConverter {
    StatementConverter INSTANCE = Mappers.getMapper(StatementConverter.class);

    //---------------------------------------------------------------------
    // AndStatement
    //---------------------------------------------------------------------
    AndStatement convertAndStatement(com.amazonaws.wafv2.rulegroup.AndStatement source);
    com.amazonaws.wafv2.rulegroup.AndStatement invertAndStatement(AndStatement source);

    //---------------------------------------------------------------------
    // GeoMatchStatement
    //---------------------------------------------------------------------
    GeoMatchStatement convert(com.amazonaws.wafv2.rulegroup.GeoMatchStatement source);
    com.amazonaws.wafv2.rulegroup.GeoMatchStatement invert(GeoMatchStatement source);

    //---------------------------------------------------------------------
    // IPSetReferenceStatement
    //---------------------------------------------------------------------
    @Mapping(target ="ipSetForwardedIPConfig", source = "IPSetForwardedIPConfig")
    IPSetReferenceStatement convert(com.amazonaws.wafv2.rulegroup.IPSetReferenceStatement source);
    @Mapping(target ="IPSetForwardedIPConfig", source = "ipSetForwardedIPConfig")
    com.amazonaws.wafv2.rulegroup.IPSetReferenceStatement invert(IPSetReferenceStatement source);

    //---------------------------------------------------------------------
    // NotStatement
    //---------------------------------------------------------------------
    NotStatement convertNotStatement(com.amazonaws.wafv2.rulegroup.NotStatement source);
    com.amazonaws.wafv2.rulegroup.NotStatement invertNotStatement(NotStatement source);

    //---------------------------------------------------------------------
    // OrStatement
    //---------------------------------------------------------------------
    OrStatement convertOrStatement(com.amazonaws.wafv2.rulegroup.OrStatement source);
    com.amazonaws.wafv2.rulegroup.OrStatement invertOrStatement(OrStatement source);

    //---------------------------------------------------------------------
    // RateBasedStatement
    //---------------------------------------------------------------------
    RateBasedStatement convertRateBasedStatement(com.amazonaws.wafv2.rulegroup.RateBasedStatement source);
    com.amazonaws.wafv2.rulegroup.RateBasedStatement invertRateBasedStatement(RateBasedStatement source);

    //---------------------------------------------------------------------
    // RegexPatternReferenceStatement
    //---------------------------------------------------------------------
    RegexPatternSetReferenceStatement convert(com.amazonaws.wafv2.rulegroup.RegexPatternSetReferenceStatement source);
    com.amazonaws.wafv2.rulegroup.RegexPatternSetReferenceStatement invert(RegexPatternSetReferenceStatement source);

    //---------------------------------------------------------------------
    // SizeConstraintStatement
    //---------------------------------------------------------------------
    SizeConstraintStatement convert(com.amazonaws.wafv2.rulegroup.SizeConstraintStatement source);
    com.amazonaws.wafv2.rulegroup.SizeConstraintStatement invert(SizeConstraintStatement source);

    //---------------------------------------------------------------------
    // Statement
    //---------------------------------------------------------------------
    @Mapping(target = "ruleGroupReferenceStatement", ignore = true)
    @Mapping(target = "managedRuleGroupStatement", ignore = true)
    @Mapping(source = "IPSetReferenceStatement", target = "ipSetReferenceStatement")
    Statement convertStatement(com.amazonaws.wafv2.rulegroup.Statement source);
    @Mapping(source = "ipSetReferenceStatement", target = "IPSetReferenceStatement")
    com.amazonaws.wafv2.rulegroup.Statement invertStatement(Statement source);

    //---------------------------------------------------------------------
    // SqliMatchStatement
    //---------------------------------------------------------------------
    SqliMatchStatement convert(com.amazonaws.wafv2.rulegroup.SqliMatchStatement source);
    com.amazonaws.wafv2.rulegroup.SqliMatchStatement invert(SqliMatchStatement source);

    //---------------------------------------------------------------------
    // XssMatchStatement
    //---------------------------------------------------------------------
    XssMatchStatement convert(com.amazonaws.wafv2.rulegroup.XssMatchStatement source);
    com.amazonaws.wafv2.rulegroup.XssMatchStatement invert(XssMatchStatement source);

    //---------------------------------------------------------------------
    // LabelMatchStatement
    //---------------------------------------------------------------------
    LabelMatchStatement convert(com.amazonaws.wafv2.rulegroup.LabelMatchStatement source);
    com.amazonaws.wafv2.rulegroup.LabelMatchStatement invert(LabelMatchStatement source);

    // Custom Statement

    //---------------------------------------------------------------------
    // ByteMatchStatement
    //---------------------------------------------------------------------
    default ByteMatchStatement convert(com.amazonaws.wafv2.rulegroup.ByteMatchStatement source) {
        ByteMatchStatement.Builder result = ByteMatchStatement.builder();

        if (!(source.getSearchStringBase64() == null ^ source.getSearchString() == null)) {
            throw new TerminalException("You must only specify exactly one of SearchString and SearchStringBase64");
        } else if (source.getSearchString() != null) {
            result.searchString(StatementCommonsConverter.INSTANCE.convertSearchString(
                    source.getSearchString()));
        } else if (source.getSearchStringBase64() != null) {
            result.searchString(SdkBytes.fromByteArray(Base64.decode(source.getSearchStringBase64())));
        }

        if (source.getTextTransformations() != null) {
            result.textTransformations(source.getTextTransformations().stream()
                    .map(textTransformation -> StatementCommonsConverter.INSTANCE.convert(textTransformation))
                    .collect(Collectors.toList()));
        }

        result.positionalConstraint(source.getPositionalConstraint());
        result.fieldToMatch(StatementCommonsConverter.INSTANCE.convert(source.getFieldToMatch()));

        return result.build();
    }

    @Mapping(target = "searchStringBase64", ignore = true)
    default com.amazonaws.wafv2.rulegroup.ByteMatchStatement invert(ByteMatchStatement source) {
        com.amazonaws.wafv2.rulegroup.ByteMatchStatement result =
                com.amazonaws.wafv2.rulegroup.ByteMatchStatement.builder().build();

        // WAF SDK will return non-Base64-encoded SdkBytes (raw byte array)
        // keep the raw bytes as is for searchString
        result.setSearchString(new String(source.searchString().asByteArray(), StandardCharsets.UTF_8));

        // base64 encode raw bytes for searchStringBase64
        result.setSearchStringBase64(
                new String(Base64.encode(source.searchString().asByteArray()), StandardCharsets.UTF_8));
        result.setTextTransformations(source.textTransformations().stream()
                .map(textTransformation -> StatementCommonsConverter.INSTANCE.invert(textTransformation))
                .collect(Collectors.toList()));
        result.setPositionalConstraint(source.positionalConstraintAsString());
        result.setFieldToMatch(StatementCommonsConverter.INSTANCE.invert(source.fieldToMatch()));

        return result;
    }
}
