package com.amazonaws.wafv2.webacl.converters;

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
import software.amazon.awssdk.services.wafv2.model.ManagedRuleGroupStatement;
import software.amazon.awssdk.services.wafv2.model.NotStatement;
import software.amazon.awssdk.services.wafv2.model.OrStatement;
import software.amazon.awssdk.services.wafv2.model.RateBasedStatement;
import software.amazon.awssdk.services.wafv2.model.RegexPatternSetReferenceStatement;
import software.amazon.awssdk.services.wafv2.model.RuleGroupReferenceStatement;
import software.amazon.awssdk.services.wafv2.model.SizeConstraintStatement;
import software.amazon.awssdk.services.wafv2.model.SqliMatchStatement;
import software.amazon.awssdk.services.wafv2.model.Statement;
import software.amazon.awssdk.services.wafv2.model.XssMatchStatement;
import software.amazon.awssdk.services.wafv2.model.LabelMatchStatement;
import software.amazon.cloudformation.exceptions.TerminalException;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Converter for converting all statement types
 */
@Mapper(uses = StatementCommonsConverter.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface StatementConverter {
    StatementConverter INSTANCE = Mappers.getMapper(StatementConverter.class);

    //---------------------------------------------------------------------
    // AndStatement
    //---------------------------------------------------------------------
    AndStatement convertAndStatement(com.amazonaws.wafv2.webacl.AndStatement source);
    com.amazonaws.wafv2.webacl.AndStatement invertAndStatement(AndStatement source);

    //---------------------------------------------------------------------
    // GeoMatchStatement
    //---------------------------------------------------------------------
    GeoMatchStatement convert(com.amazonaws.wafv2.webacl.GeoMatchStatement source);
    com.amazonaws.wafv2.webacl.GeoMatchStatement invert(GeoMatchStatement source);

    //---------------------------------------------------------------------
    // IPSetReferenceStatement
    //---------------------------------------------------------------------
    @Mapping(target ="ipSetForwardedIPConfig", source = "IPSetForwardedIPConfig")
    IPSetReferenceStatement convert(com.amazonaws.wafv2.webacl.IPSetReferenceStatement source);
    @Mapping(target ="IPSetForwardedIPConfig", source = "ipSetForwardedIPConfig")
    com.amazonaws.wafv2.webacl.IPSetReferenceStatement invert(IPSetReferenceStatement source);

    //---------------------------------------------------------------------
    // ManagedRuleSetStatement
    //---------------------------------------------------------------------
    ManagedRuleGroupStatement convertManagedRuleGroupStatement(com.amazonaws.wafv2.webacl.ManagedRuleGroupStatement source);
    com.amazonaws.wafv2.webacl.ManagedRuleGroupStatement invertManagedRuleGroupStatement(ManagedRuleGroupStatement source);

    //---------------------------------------------------------------------
    // NotStatement
    //---------------------------------------------------------------------
    NotStatement convertNotStatement(com.amazonaws.wafv2.webacl.NotStatement source);
    com.amazonaws.wafv2.webacl.NotStatement invertNotStatement(NotStatement source);

    //---------------------------------------------------------------------
    // OrStatement
    //---------------------------------------------------------------------
    OrStatement convertOrStatement(com.amazonaws.wafv2.webacl.OrStatement source);
    com.amazonaws.wafv2.webacl.OrStatement invertOrStatement(OrStatement source);

    //---------------------------------------------------------------------
    // RateBasedStatement
    //---------------------------------------------------------------------
    RateBasedStatement convertRateBasedStatement(com.amazonaws.wafv2.webacl.RateBasedStatement source);
    com.amazonaws.wafv2.webacl.RateBasedStatement invertRateBasedStatement(RateBasedStatement source);

    //---------------------------------------------------------------------
    // RegexPatternSetReferenceStatement
    //---------------------------------------------------------------------
    RegexPatternSetReferenceStatement convert(com.amazonaws.wafv2.webacl.RegexPatternSetReferenceStatement source);
    com.amazonaws.wafv2.webacl.RegexPatternSetReferenceStatement invert(RegexPatternSetReferenceStatement source);

    //---------------------------------------------------------------------
    // RuleGroupReferenceStatement
    //---------------------------------------------------------------------
    RuleGroupReferenceStatement convert(com.amazonaws.wafv2.webacl.RuleGroupReferenceStatement source);
    com.amazonaws.wafv2.webacl.RuleGroupReferenceStatement invert(RuleGroupReferenceStatement source);

    //---------------------------------------------------------------------
    // SizeConstraintStatement
    //---------------------------------------------------------------------
    SizeConstraintStatement convert(com.amazonaws.wafv2.webacl.SizeConstraintStatement source);
    com.amazonaws.wafv2.webacl.SizeConstraintStatement invert(SizeConstraintStatement source);

    //---------------------------------------------------------------------
    // Statement
    //---------------------------------------------------------------------
    @Mapping(source = "IPSetReferenceStatement", target = "ipSetReferenceStatement")
    Statement convertStatement(com.amazonaws.wafv2.webacl.Statement source);
    @Mapping(source = "ipSetReferenceStatement", target = "IPSetReferenceStatement")
    com.amazonaws.wafv2.webacl.Statement invertStatement(Statement source);

    //---------------------------------------------------------------------
    // SqliMatchStatement
    //---------------------------------------------------------------------
    SqliMatchStatement convert(com.amazonaws.wafv2.webacl.SqliMatchStatement source);
    com.amazonaws.wafv2.webacl.SqliMatchStatement invert(SqliMatchStatement source);

    //---------------------------------------------------------------------
    // XssMatchStatement
    //---------------------------------------------------------------------
    XssMatchStatement convert(com.amazonaws.wafv2.webacl.XssMatchStatement source);
    com.amazonaws.wafv2.webacl.XssMatchStatement invert(XssMatchStatement source);

    //---------------------------------------------------------------------
    // LabelMatchStatement
    //---------------------------------------------------------------------
    LabelMatchStatement convert(com.amazonaws.wafv2.webacl.LabelMatchStatement source);
    com.amazonaws.wafv2.webacl.LabelMatchStatement invert(LabelMatchStatement source);


    // Custom Statement

    //---------------------------------------------------------------------
    // ByteMatchStatement
    //---------------------------------------------------------------------
    default ByteMatchStatement convert(com.amazonaws.wafv2.webacl.ByteMatchStatement source) {
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
    default com.amazonaws.wafv2.webacl.ByteMatchStatement invert(ByteMatchStatement source) {
        com.amazonaws.wafv2.webacl.ByteMatchStatement result =
                com.amazonaws.wafv2.webacl.ByteMatchStatement.builder().build();

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
