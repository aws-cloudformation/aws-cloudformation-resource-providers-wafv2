package com.amazonaws.wafv2.rulegroup.converters;

import com.amazonaws.util.Base64;
import com.amazonaws.wafv2.rulegroup.AndStatement;
import com.amazonaws.wafv2.rulegroup.ByteMatchStatement;
import com.amazonaws.wafv2.rulegroup.FieldToMatch;
import com.amazonaws.wafv2.rulegroup.GeoMatchStatement;
import com.amazonaws.wafv2.rulegroup.IPSetReferenceStatement;
import com.amazonaws.wafv2.rulegroup.LabelSummary;
import com.amazonaws.wafv2.rulegroup.NotStatement;
import com.amazonaws.wafv2.rulegroup.OrStatement;
import com.amazonaws.wafv2.rulegroup.RateBasedStatement;
import com.amazonaws.wafv2.rulegroup.RegexPatternSetReferenceStatement;
import com.amazonaws.wafv2.rulegroup.Rule;
import com.amazonaws.wafv2.rulegroup.RuleAction;
import com.amazonaws.wafv2.rulegroup.SingleHeader;
import com.amazonaws.wafv2.rulegroup.SingleQueryArgument;
import com.amazonaws.wafv2.rulegroup.SizeConstraintStatement;
import com.amazonaws.wafv2.rulegroup.SqliMatchStatement;
import com.amazonaws.wafv2.rulegroup.Statement;
import com.amazonaws.wafv2.rulegroup.TextTransformation;
import com.amazonaws.wafv2.rulegroup.VisibilityConfig;
import com.amazonaws.wafv2.rulegroup.XssMatchStatement;
import com.amazonaws.wafv2.rulegroup.helpers.ObjectMapperHelper;
import com.amazonaws.wafv2.rulegroup.LabelMatchStatement;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import software.amazon.awssdk.services.wafv2.model.BodyParsingFallbackBehavior;
import software.amazon.awssdk.services.wafv2.model.ComparisonOperator;
import software.amazon.awssdk.services.wafv2.model.JsonBody;
import software.amazon.awssdk.services.wafv2.model.JsonMatchScope;
import software.amazon.awssdk.services.wafv2.model.RateBasedStatementAggregateKeyType;
import software.amazon.awssdk.services.wafv2.model.TextTransformationType;
import software.amazon.awssdk.services.wafv2.model.LabelMatchScope;
import software.amazon.cloudformation.exceptions.TerminalException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ConvertersTests {

    @Test
    public void testAndStatementWithNesting() {
        AndStatement andStatement = ObjectMapperHelper.getObject(
            "test-data/test-and-or-statement-two-levels-nested.json", AndStatement.class);

        software.amazon.awssdk.services.wafv2.model.AndStatement sdkAndStatement = StatementConverter.INSTANCE
            .convertAndStatement(andStatement);
        Assert.assertEquals(3, sdkAndStatement.statements().size());
        Assert.assertNotNull(sdkAndStatement.statements().get(2).rateBasedStatement());

        AndStatement cloudFormationAndStatement =
            StatementConverter.INSTANCE.invertAndStatement(sdkAndStatement);
        Assert.assertEquals(3, cloudFormationAndStatement.getStatements().size());
        Assert.assertNotNull(cloudFormationAndStatement.getStatements().get(2).getRateBasedStatement());
    }

    @Test
    public void testAndStatement() {
        AndStatement andStatement = ObjectMapperHelper.getObject(
            "test-data/test-and-or-statement.json", AndStatement.class);

        software.amazon.awssdk.services.wafv2.model.AndStatement sdkAndStatement = StatementConverter.INSTANCE
            .convertAndStatement(andStatement);
        Assert.assertEquals(2, sdkAndStatement.statements().size());

        AndStatement cloudFormationAndStatement =
            StatementConverter.INSTANCE.invertAndStatement(sdkAndStatement);
        Assert.assertEquals(2, cloudFormationAndStatement.getStatements().size());
    }

    @Test
    public void testByteMatchStatement() {
        ByteMatchStatement byteMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-byte-match-statement.json", ByteMatchStatement.class);

        software.amazon.awssdk.services.wafv2.model.ByteMatchStatement sdkByteMatchStatement =
                StatementConverter.INSTANCE.convert(byteMatchStatement);
        // SdkBytes should be same as searchString since user inputs 'SearchString' field
        Assert.assertArrayEquals(byteMatchStatement.getSearchString().getBytes(),
                sdkByteMatchStatement.searchString().asByteArray());
        Assert.assertEquals(software.amazon.awssdk.services.wafv2.model.PositionalConstraint.CONTAINS_WORD,
                sdkByteMatchStatement.positionalConstraint());
        Assert.assertNotNull(sdkByteMatchStatement.fieldToMatch());
        Assert.assertEquals(1, sdkByteMatchStatement.textTransformations().size());

        ByteMatchStatement cloudFormationByteMatchStatement =
                StatementConverter.INSTANCE.invert(sdkByteMatchStatement);
        // searchString should be as user input
        Assert.assertEquals(byteMatchStatement.getSearchString(), cloudFormationByteMatchStatement.getSearchString());
        Assert.assertEquals(Base64.encodeAsString(byteMatchStatement.getSearchString().getBytes()),
                cloudFormationByteMatchStatement.getSearchStringBase64());
        Assert.assertEquals("CONTAINS_WORD", cloudFormationByteMatchStatement.getPositionalConstraint());
        Assert.assertNotNull(cloudFormationByteMatchStatement.getFieldToMatch());
        Assert.assertEquals(1, cloudFormationByteMatchStatement.getTextTransformations().size());
    }

    @Test
    public void testByteMatchStatementWithoutTextTransformations() {
        ByteMatchStatement byteMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-byte-match-statement.json", ByteMatchStatement.class);
        byteMatchStatement.setTextTransformations(null);

        software.amazon.awssdk.services.wafv2.model.ByteMatchStatement sdkByteMatchStatement =
                StatementConverter.INSTANCE.convert(byteMatchStatement);
        // SdkBytes should be same as searchString since user inputs 'SearchString' field
        Assert.assertArrayEquals(byteMatchStatement.getSearchString().getBytes(),
                sdkByteMatchStatement.searchString().asByteArray());
        Assert.assertEquals(software.amazon.awssdk.services.wafv2.model.PositionalConstraint.CONTAINS_WORD,
                sdkByteMatchStatement.positionalConstraint());
        Assert.assertNotNull(sdkByteMatchStatement.fieldToMatch());
        Assert.assertEquals(0, sdkByteMatchStatement.textTransformations().size());

        ByteMatchStatement cloudFormationByteMatchStatement =
                StatementConverter.INSTANCE.invert(sdkByteMatchStatement);
        // searchString should be as user input
        Assert.assertEquals(byteMatchStatement.getSearchString(), cloudFormationByteMatchStatement.getSearchString());
        Assert.assertEquals(Base64.encodeAsString(byteMatchStatement.getSearchString().getBytes()),
                cloudFormationByteMatchStatement.getSearchStringBase64());
        Assert.assertEquals("CONTAINS_WORD", cloudFormationByteMatchStatement.getPositionalConstraint());
        Assert.assertNotNull(cloudFormationByteMatchStatement.getFieldToMatch());
        Assert.assertEquals(0, cloudFormationByteMatchStatement.getTextTransformations().size());
    }

    @Test
    public void testByteMatchStatementWithBase64Encoded() {
        ByteMatchStatement byteMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-byte-match-statement.json", ByteMatchStatement.class);
        // JSON file already contains base64-encoded string, the encoded string is base64Encode("needle")
        byteMatchStatement.setSearchStringBase64(byteMatchStatement.getSearchString());
        byteMatchStatement.setSearchString(null);

        software.amazon.awssdk.services.wafv2.model.ByteMatchStatement sdkByteMatchStatement =
                StatementConverter.INSTANCE.convert(byteMatchStatement);
        // SdkBytes should be base64 decoded
        Assert.assertArrayEquals(Base64.decode(byteMatchStatement.getSearchStringBase64()),
                sdkByteMatchStatement.searchString().asByteArray());
        Assert.assertEquals(software.amazon.awssdk.services.wafv2.model.PositionalConstraint.CONTAINS_WORD,
                sdkByteMatchStatement.positionalConstraint());
        Assert.assertNotNull(sdkByteMatchStatement.fieldToMatch());
        Assert.assertEquals(1, sdkByteMatchStatement.textTransformations().size());

        ByteMatchStatement cloudFormationByteMatchStatement =
                StatementConverter.INSTANCE.invert(sdkByteMatchStatement);

        // searchString should be base64 decoded
        Assert.assertEquals("needle", cloudFormationByteMatchStatement.getSearchString());
        // searchStringBase64 should be base64 encoded
        Assert.assertEquals(byteMatchStatement.getSearchStringBase64(),
                cloudFormationByteMatchStatement.getSearchStringBase64());
        Assert.assertEquals("CONTAINS_WORD", cloudFormationByteMatchStatement.getPositionalConstraint());
        Assert.assertNotNull(cloudFormationByteMatchStatement.getFieldToMatch());
        Assert.assertEquals(1, cloudFormationByteMatchStatement.getTextTransformations().size());
    }

    @Test
    public void testByteMatchStatementWithNonAsciiText() {
        ByteMatchStatement byteMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-byte-match-statement.json", ByteMatchStatement.class);
        // non-ascii text
        String searchString = "防火墙";
        byteMatchStatement.setTextTransformations(null);
        byteMatchStatement.setSearchString(searchString);

        software.amazon.awssdk.services.wafv2.model.ByteMatchStatement sdkByteMatchStatement =
                StatementConverter.INSTANCE.convert(byteMatchStatement);

        // without base64 encoding
        Assert.assertEquals(searchString,
                sdkByteMatchStatement.searchString().asUtf8String());
        Assert.assertEquals(software.amazon.awssdk.services.wafv2.model.PositionalConstraint.CONTAINS_WORD,
                sdkByteMatchStatement.positionalConstraint());
        Assert.assertNotNull(sdkByteMatchStatement.fieldToMatch());
        Assert.assertEquals(0, sdkByteMatchStatement.textTransformations().size());

        ByteMatchStatement cloudFormationByteMatchStatement =
                StatementConverter.INSTANCE.invert(sdkByteMatchStatement);
        // searchString should be as is user has input
        Assert.assertEquals(searchString, cloudFormationByteMatchStatement.getSearchString());
        // searchString should be base64 encoded
        Assert.assertEquals(Base64.encodeAsString(searchString.getBytes(StandardCharsets.UTF_8)),
                cloudFormationByteMatchStatement.getSearchStringBase64());
        Assert.assertEquals("CONTAINS_WORD", cloudFormationByteMatchStatement.getPositionalConstraint());
        Assert.assertNotNull(cloudFormationByteMatchStatement.getFieldToMatch());
        Assert.assertEquals(0, cloudFormationByteMatchStatement.getTextTransformations().size());
    }

    @Test
    public void testByteMatchStatementWithNonUTF8EncodedWithBase64Encoded() {
        ByteMatchStatement byteMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-byte-match-statement.json", ByteMatchStatement.class);
        // invalid UTF8 encoded text -> \xE8\x18\x27\x87
        byte[] searchStringInBytes = {-0x18, 0x18, 0x27, -0x79};
        byteMatchStatement.setTextTransformations(null);
        byteMatchStatement.setSearchString(null);
        // perform base64 encode
        byteMatchStatement.setSearchStringBase64(Base64.encodeAsString(searchStringInBytes));

        software.amazon.awssdk.services.wafv2.model.ByteMatchStatement sdkByteMatchStatement =
                StatementConverter.INSTANCE.convert(byteMatchStatement);

        // searchString blob should not be encoded
        Assert.assertArrayEquals(searchStringInBytes, sdkByteMatchStatement.searchString().asByteArray());
        Assert.assertEquals(software.amazon.awssdk.services.wafv2.model.PositionalConstraint.CONTAINS_WORD,
                sdkByteMatchStatement.positionalConstraint());
        Assert.assertNotNull(sdkByteMatchStatement.fieldToMatch());
        Assert.assertEquals(0, sdkByteMatchStatement.textTransformations().size());

        ByteMatchStatement cloudFormationByteMatchStatement =
                StatementConverter.INSTANCE.invert(sdkByteMatchStatement);
        // searchStringBase64 should be base64 encoded
        Assert.assertEquals(Base64.encodeAsString(searchStringInBytes),
                cloudFormationByteMatchStatement.getSearchStringBase64());
        Assert.assertEquals("CONTAINS_WORD", cloudFormationByteMatchStatement.getPositionalConstraint());
        Assert.assertNotNull(cloudFormationByteMatchStatement.getFieldToMatch());
        Assert.assertEquals(0, cloudFormationByteMatchStatement.getTextTransformations().size());
    }

    @Test(expected = TerminalException.class)
    public void testByteMatchStatementWithoutSearchStringOrSearchStringBase64() {
        ByteMatchStatement byteMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-byte-match-statement.json", ByteMatchStatement.class);
        byteMatchStatement.setSearchStringBase64(null);
        byteMatchStatement.setSearchString(null);

        StatementConverter.INSTANCE.convert(byteMatchStatement);
    }

    @Test(expected = TerminalException.class)
    public void testByteMatchStatementWithBothSearchStringAndSearchStringBase64() {
        ByteMatchStatement byteMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-byte-match-statement.json", ByteMatchStatement.class);
        byteMatchStatement.setSearchStringBase64(byteMatchStatement.getSearchString());

        StatementConverter.INSTANCE.convert(byteMatchStatement);
    }

    @Test
    public void testFieldToMatch() {
        FieldToMatch fieldToMatch = ObjectMapperHelper.getObject(
                "test-data/test-field-to-match.json", FieldToMatch.class);

        software.amazon.awssdk.services.wafv2.model.FieldToMatch sdkFieldToMatch =
                StatementCommonsConverter.INSTANCE.convert(fieldToMatch);
        Assert.assertNotNull(fieldToMatch.getSingleHeader());
        Assert.assertNull(fieldToMatch.getAllQueryArguments());
        Assert.assertNull(fieldToMatch.getBody());
        Assert.assertNull(fieldToMatch.getMethod());
        Assert.assertNull(fieldToMatch.getQueryString());
        Assert.assertNull(fieldToMatch.getUriPath());

        FieldToMatch cloudFormationFieldToMatch = StatementCommonsConverter.INSTANCE.invert(sdkFieldToMatch);
        Assert.assertNotNull(cloudFormationFieldToMatch.getSingleHeader());
        Assert.assertNull(cloudFormationFieldToMatch.getAllQueryArguments());
        Assert.assertNull(cloudFormationFieldToMatch.getBody());
        Assert.assertNull(cloudFormationFieldToMatch.getMethod());
        Assert.assertNull(cloudFormationFieldToMatch.getQueryString());
        Assert.assertNull(cloudFormationFieldToMatch.getUriPath());
    }

    @Test
    public void testGeoMatchStatement() {
        GeoMatchStatement geoMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-geo-match-statement.json", GeoMatchStatement.class);

        software.amazon.awssdk.services.wafv2.model.GeoMatchStatement sdkGeoMatchStatement =
                StatementConverter.INSTANCE.convert(geoMatchStatement);

        Assert.assertEquals(2, sdkGeoMatchStatement.countryCodes().size());

        GeoMatchStatement cloudFormationGeoMatchStatement =
                StatementConverter.INSTANCE.invert(sdkGeoMatchStatement);
        Assert.assertEquals(2, cloudFormationGeoMatchStatement.getCountryCodes().size());
    }

    @Test
    public void testGeoMatchStatementWithForwardedIPConfig() {
        GeoMatchStatement geoMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-geo-match-statement-with-forwarded-ip-config.json", GeoMatchStatement.class);

        software.amazon.awssdk.services.wafv2.model.GeoMatchStatement sdkGeoMatchStatement =
                StatementConverter.INSTANCE.convert(geoMatchStatement);

        Assert.assertEquals(2, sdkGeoMatchStatement.countryCodes().size());
        Assert.assertNotNull(sdkGeoMatchStatement.forwardedIPConfig());

        GeoMatchStatement cloudFormationGeoMatchStatement =
                StatementConverter.INSTANCE.invert(sdkGeoMatchStatement);
        Assert.assertEquals(2, cloudFormationGeoMatchStatement.getCountryCodes().size());
        Assert.assertEquals("testHeader", cloudFormationGeoMatchStatement.getForwardedIPConfig().getHeaderName());
        Assert.assertEquals("MATCH", cloudFormationGeoMatchStatement.getForwardedIPConfig().getFallbackBehavior());
    }

    @Test
    public void testIPSetReferenceStatement() {
        IPSetReferenceStatement geoMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-ip-set-reference-statement.json", IPSetReferenceStatement.class);

        software.amazon.awssdk.services.wafv2.model.IPSetReferenceStatement sdkIPSetReferenceStatement =
                StatementConverter.INSTANCE.convert(geoMatchStatement);
        Assert.assertNotNull(sdkIPSetReferenceStatement.arn());

        IPSetReferenceStatement cloudFormationIPSetReferenceStatement =
                StatementConverter.INSTANCE.invert(sdkIPSetReferenceStatement);
        Assert.assertNotNull(cloudFormationIPSetReferenceStatement.getArn());
    }

    @Test
    public void testIPSetReferenceStatementWithForwardedIPConfig() {
        IPSetReferenceStatement ipSetReferenceStatement = ObjectMapperHelper.getObject(
                "test-data/test-ip-set-reference-statement-with-forwarded-ip-config.json", IPSetReferenceStatement.class);

        software.amazon.awssdk.services.wafv2.model.IPSetReferenceStatement sdkIPSetReferenceStatement =
                StatementConverter.INSTANCE.convert(ipSetReferenceStatement);
        Assert.assertNotNull(sdkIPSetReferenceStatement.arn());
        Assert.assertNotNull(sdkIPSetReferenceStatement.ipSetForwardedIPConfig());

        IPSetReferenceStatement cloudFormationIPSetReferenceStatement =
                StatementConverter.INSTANCE.invert(sdkIPSetReferenceStatement);
        Assert.assertNotNull(cloudFormationIPSetReferenceStatement.getArn());
        Assert.assertNotNull(cloudFormationIPSetReferenceStatement.getIPSetForwardedIPConfig());
        Assert.assertEquals("MATCH", cloudFormationIPSetReferenceStatement.getIPSetForwardedIPConfig().getFallbackBehavior());
        Assert.assertEquals("testHeader", cloudFormationIPSetReferenceStatement.getIPSetForwardedIPConfig().getHeaderName());
        Assert.assertEquals("LAST", cloudFormationIPSetReferenceStatement.getIPSetForwardedIPConfig().getPosition());
    }

    @Test
    public void testNotStatementWithNesting() {
        NotStatement notStatement = ObjectMapperHelper.getObject(
            "test-data/test-not-statement-two-levels-nested.json", NotStatement.class);

        software.amazon.awssdk.services.wafv2.model.NotStatement sdkNotStatement = StatementConverter.INSTANCE
            .convertNotStatement(notStatement);
        Assert.assertNotNull(sdkNotStatement.statement());
        Assert.assertNotNull(sdkNotStatement.statement().rateBasedStatement());
        Assert
            .assertNotNull(sdkNotStatement.statement().rateBasedStatement().scopeDownStatement().byteMatchStatement());

        NotStatement cloudFormationNotStatement =
            StatementConverter.INSTANCE.invertNotStatement(sdkNotStatement);
        Assert.assertNotNull(cloudFormationNotStatement.getStatement());
        Assert.assertNotNull(cloudFormationNotStatement.getStatement().getRateBasedStatement());
        Assert.assertNotNull(cloudFormationNotStatement.getStatement().getRateBasedStatement()
            .getScopeDownStatement().getByteMatchStatement());
    }

    @Test
    public void testNotStatement() {
        NotStatement notStatement = ObjectMapperHelper.getObject(
            "test-data/test-not-statement.json", NotStatement.class);

        software.amazon.awssdk.services.wafv2.model.NotStatement sdkNotStatement = StatementConverter.INSTANCE
            .convertNotStatement(notStatement);
        Assert.assertNotNull(sdkNotStatement.statement());

        NotStatement cloudFormationNotStatement =
            StatementConverter.INSTANCE.invertNotStatement(sdkNotStatement);
        Assert.assertNotNull(cloudFormationNotStatement.getStatement());
    }

    @Test
    public void testOrStatementWithNesting() {
        OrStatement orStatement = ObjectMapperHelper.getObject(
            "test-data/test-and-or-statement-two-levels-nested.json", OrStatement.class);

        software.amazon.awssdk.services.wafv2.model.OrStatement sdkOrStatement = StatementConverter.INSTANCE
            .convertOrStatement(orStatement);
        Assert.assertEquals(3, sdkOrStatement.statements().size());
        Assert.assertNotNull(sdkOrStatement.statements().get(2).rateBasedStatement());

        OrStatement cloudFormationOrStatement =
            StatementConverter.INSTANCE.invertOrStatement(sdkOrStatement);
        Assert.assertEquals(3, cloudFormationOrStatement.getStatements().size());
        Assert.assertNotNull(cloudFormationOrStatement.getStatements().get(2).getRateBasedStatement());
    }

    @Test
    public void testOrStatement() {
        OrStatement orStatement = ObjectMapperHelper.getObject(
            "test-data/test-and-or-statement.json", OrStatement.class);

        software.amazon.awssdk.services.wafv2.model.OrStatement sdkOrStatement = StatementConverter.INSTANCE
            .convertOrStatement(orStatement);
        Assert.assertEquals(2, sdkOrStatement.statements().size());

        OrStatement cloudFormationOrStatement =
            StatementConverter.INSTANCE.invertOrStatement(sdkOrStatement);
        Assert.assertEquals(2, cloudFormationOrStatement.getStatements().size());
    }

    @Test
    public void testRateBasedStatementWithNesting() {
        RateBasedStatement rateBasedStatement = ObjectMapperHelper.getObject(
            "test-data/test-rate-based-statement-two-levels-nested.json", RateBasedStatement.class);

        software.amazon.awssdk.services.wafv2.model.RateBasedStatement sdkRateBasedStatement =
            StatementConverter.INSTANCE.convertRateBasedStatement(rateBasedStatement);
        Assert.assertNotNull(sdkRateBasedStatement.scopeDownStatement());
        Assert.assertNotNull(sdkRateBasedStatement.scopeDownStatement().notStatement());
        Assert.assertNotNull(
            sdkRateBasedStatement.scopeDownStatement().notStatement().statement().sizeConstraintStatement());
        Assert.assertEquals(new Long(2000), sdkRateBasedStatement.limit());
        Assert.assertEquals(RateBasedStatementAggregateKeyType.IP, sdkRateBasedStatement.aggregateKeyType());

        RateBasedStatement cloudFormationSdkRateBasedStatement =
            StatementConverter.INSTANCE.invertRateBasedStatement(sdkRateBasedStatement);
        Assert.assertNotNull(cloudFormationSdkRateBasedStatement.getScopeDownStatement());
        Assert.assertNotNull(cloudFormationSdkRateBasedStatement.getScopeDownStatement());
        Assert.assertNotNull(cloudFormationSdkRateBasedStatement.getScopeDownStatement().getNotStatement());
        Assert.assertNotNull(cloudFormationSdkRateBasedStatement.getScopeDownStatement().getNotStatement()
            .getStatement().getSizeConstraintStatement());
        Assert.assertEquals(new Integer(2000), cloudFormationSdkRateBasedStatement.getLimit());
        Assert.assertEquals("IP", cloudFormationSdkRateBasedStatement.getAggregateKeyType());
    }

    @Test
    public void testRateBasedStatement() {
        RateBasedStatement rateBasedStatement = ObjectMapperHelper.getObject(
                "test-data/test-rate-based-statement.json", RateBasedStatement.class);


        software.amazon.awssdk.services.wafv2.model.RateBasedStatement sdkRateBasedStatement =
                StatementConverter.INSTANCE.convertRateBasedStatement(rateBasedStatement);
        Assert.assertNotNull(sdkRateBasedStatement.scopeDownStatement());
        Assert.assertEquals(new Long(2000), sdkRateBasedStatement.limit());
        Assert.assertEquals(RateBasedStatementAggregateKeyType.IP, sdkRateBasedStatement.aggregateKeyType());

        RateBasedStatement cloudFormationSdkRateBasedStatement =
                StatementConverter.INSTANCE.invertRateBasedStatement(sdkRateBasedStatement);
        Assert.assertNotNull(cloudFormationSdkRateBasedStatement.getScopeDownStatement());
        Assert.assertEquals(new Integer(2000), cloudFormationSdkRateBasedStatement.getLimit());
        Assert.assertEquals("IP", cloudFormationSdkRateBasedStatement.getAggregateKeyType());
    }

    @Test
    public void testRateBasedStatementWithForwardedIPConfigAndNesting() {
        RateBasedStatement rateBasedStatement = ObjectMapperHelper.getObject(
            "test-data/test-rate-based-statement-two-levels-nested-with-forwarded-ip-config.json",
            RateBasedStatement.class);

        software.amazon.awssdk.services.wafv2.model.RateBasedStatement sdkRateBasedStatement =
            StatementConverter.INSTANCE.convertRateBasedStatement(rateBasedStatement);
        Assert.assertNotNull(sdkRateBasedStatement.scopeDownStatement());
        Assert.assertNotNull(sdkRateBasedStatement.scopeDownStatement().notStatement());
        Assert.assertNotNull(
            sdkRateBasedStatement.scopeDownStatement().notStatement().statement().sizeConstraintStatement());
        Assert.assertEquals(new Long(2000), sdkRateBasedStatement.limit());
        Assert.assertEquals(RateBasedStatementAggregateKeyType.FORWARDED_IP, sdkRateBasedStatement.aggregateKeyType());
        Assert.assertNotNull(sdkRateBasedStatement.forwardedIPConfig());

        RateBasedStatement cloudFormationSdkRateBasedStatement =
            StatementConverter.INSTANCE.invertRateBasedStatement(sdkRateBasedStatement);
        Assert.assertNotNull(cloudFormationSdkRateBasedStatement.getScopeDownStatement());
        Assert.assertNotNull(cloudFormationSdkRateBasedStatement.getScopeDownStatement());
        Assert.assertNotNull(cloudFormationSdkRateBasedStatement.getScopeDownStatement().getNotStatement());
        Assert.assertNotNull(cloudFormationSdkRateBasedStatement.getScopeDownStatement().getNotStatement()
            .getStatement().getSizeConstraintStatement());
        Assert.assertEquals(new Integer(2000), cloudFormationSdkRateBasedStatement.getLimit());
        Assert.assertEquals("FORWARDED_IP", cloudFormationSdkRateBasedStatement.getAggregateKeyType());
        Assert.assertNotNull(cloudFormationSdkRateBasedStatement.getForwardedIPConfig());
        Assert.assertEquals(cloudFormationSdkRateBasedStatement.getForwardedIPConfig().getHeaderName(), "testHeader");
        Assert.assertEquals(cloudFormationSdkRateBasedStatement.getForwardedIPConfig().getFallbackBehavior(), "MATCH");
    }

    @Test
    public void testRateBasedStatementWithForwardedIPConfig() {
        RateBasedStatement rateBasedStatement = ObjectMapperHelper.getObject(
            "test-data/test-rate-based-statement-with-forwarded-ip-config.json", RateBasedStatement.class);

        software.amazon.awssdk.services.wafv2.model.RateBasedStatement sdkRateBasedStatement =
            StatementConverter.INSTANCE.convertRateBasedStatement(rateBasedStatement);
        Assert.assertNotNull(sdkRateBasedStatement.scopeDownStatement());
        Assert.assertEquals(new Long(2000), sdkRateBasedStatement.limit());
        Assert.assertEquals(RateBasedStatementAggregateKeyType.FORWARDED_IP, sdkRateBasedStatement.aggregateKeyType());
        Assert.assertNotNull(sdkRateBasedStatement.forwardedIPConfig());

        RateBasedStatement cloudFormationSdkRateBasedStatement =
            StatementConverter.INSTANCE.invertRateBasedStatement(sdkRateBasedStatement);
        Assert.assertNotNull(cloudFormationSdkRateBasedStatement.getScopeDownStatement());
        Assert.assertEquals(new Integer(2000), cloudFormationSdkRateBasedStatement.getLimit());
        Assert.assertEquals("FORWARDED_IP", cloudFormationSdkRateBasedStatement.getAggregateKeyType());
        Assert.assertNotNull(cloudFormationSdkRateBasedStatement.getForwardedIPConfig());
        Assert.assertEquals(cloudFormationSdkRateBasedStatement.getForwardedIPConfig().getHeaderName(), "testHeader");
        Assert.assertEquals(cloudFormationSdkRateBasedStatement.getForwardedIPConfig().getFallbackBehavior(), "MATCH");
    }

    @Test
    public void testRegexPatternSetReferenceStatement() {
        RegexPatternSetReferenceStatement regexPatternReferenceStatement = ObjectMapperHelper.getObject(
                "test-data/test-regex-pattern-reference-statement.json", RegexPatternSetReferenceStatement.class);

        software.amazon.awssdk.services.wafv2.model.RegexPatternSetReferenceStatement sdkRegexPatternSetReferenceStatement =
                StatementConverter.INSTANCE.convert(regexPatternReferenceStatement);
        Assert.assertNotNull(sdkRegexPatternSetReferenceStatement.fieldToMatch());
        Assert.assertNotNull(sdkRegexPatternSetReferenceStatement.arn());
        Assert.assertEquals(2, sdkRegexPatternSetReferenceStatement.textTransformations().size());

        RegexPatternSetReferenceStatement cloudFormationRegexPatternSetReferenceStatement =
                StatementConverter.INSTANCE.invert(sdkRegexPatternSetReferenceStatement);
        Assert.assertNotNull(cloudFormationRegexPatternSetReferenceStatement.getFieldToMatch());
        Assert.assertNotNull(cloudFormationRegexPatternSetReferenceStatement.getArn());
        Assert.assertEquals(2, cloudFormationRegexPatternSetReferenceStatement.getTextTransformations().size());
    }

    @Test
    public void testRegexPatternReferenceStatementWithoutTextTransformations() {
        RegexPatternSetReferenceStatement regexPatternSetReferenceStatement = ObjectMapperHelper.getObject(
                "test-data/test-regex-pattern-reference-statement.json", RegexPatternSetReferenceStatement.class);
        regexPatternSetReferenceStatement.setTextTransformations(null);

        software.amazon.awssdk.services.wafv2.model.RegexPatternSetReferenceStatement sdkRegexPatternReferenceStatement =
                StatementConverter.INSTANCE.convert(regexPatternSetReferenceStatement);
        Assert.assertNotNull(sdkRegexPatternReferenceStatement.fieldToMatch());
        Assert.assertNotNull(sdkRegexPatternReferenceStatement.arn());

        RegexPatternSetReferenceStatement cloudFormationRegexPatternSetReferenceStatement =
                StatementConverter.INSTANCE.invert(sdkRegexPatternReferenceStatement);
        Assert.assertNotNull(cloudFormationRegexPatternSetReferenceStatement.getFieldToMatch());
        Assert.assertNotNull(cloudFormationRegexPatternSetReferenceStatement.getArn());
    }

    @Test
    public void testRuleAction() {
        RuleAction ruleAction = ObjectMapperHelper.getObject(
                "test-data/test-rule-action.json", RuleAction.class);

        software.amazon.awssdk.services.wafv2.model.RuleAction sdkRuleAction =
                StatementCommonsConverter.INSTANCE.convert(ruleAction);
        Assert.assertNotNull(sdkRuleAction.allow());
        Assert.assertNull(sdkRuleAction.block());
        Assert.assertNull(sdkRuleAction.count());

        RuleAction cloudFormationRuleAction =
                StatementCommonsConverter.INSTANCE.invert(sdkRuleAction);
        Assert.assertNotNull(cloudFormationRuleAction.getAllow());
        Assert.assertNull(cloudFormationRuleAction.getBlock());
        Assert.assertNull(cloudFormationRuleAction.getCount());
    }

    @Test
    public void testRuleActionCustomRequest() {
        RuleAction ruleAction = ObjectMapperHelper.getObject(
            "test-data/test-rule-action-custom-request.json", RuleAction.class);

        software.amazon.awssdk.services.wafv2.model.RuleAction sdkRuleAction =
            StatementCommonsConverter.INSTANCE.convert(ruleAction);
        Assert.assertNotNull(sdkRuleAction.allow());
        Assert.assertNull(sdkRuleAction.block());
        Assert.assertNull(sdkRuleAction.count());
        Assert.assertNotNull(sdkRuleAction.allow().customRequestHandling());
        Assert.assertTrue(sdkRuleAction.allow().customRequestHandling().hasInsertHeaders());
        Assert.assertEquals(2, sdkRuleAction.allow().customRequestHandling().insertHeaders().size());
        Assert.assertEquals("ruleAllowActionHeader1Name", sdkRuleAction.allow().customRequestHandling().insertHeaders().get(0).name());
        Assert.assertEquals("ruleAllowActionHeader1Value", sdkRuleAction.allow().customRequestHandling().insertHeaders().get(0).value());
        Assert.assertEquals("ruleAllowActionHeader2Name", sdkRuleAction.allow().customRequestHandling().insertHeaders().get(1).name());
        Assert.assertEquals("ruleAllowActionHeader2Value", sdkRuleAction.allow().customRequestHandling().insertHeaders().get(1).value());

        RuleAction cloudFormationRuleAction =
            StatementCommonsConverter.INSTANCE.invert(sdkRuleAction);
        Assert.assertNotNull(cloudFormationRuleAction.getAllow());
        Assert.assertNull(cloudFormationRuleAction.getBlock());
        Assert.assertNull(cloudFormationRuleAction.getCount());
        Assert.assertNotNull(cloudFormationRuleAction.getAllow().getCustomRequestHandling());
        Assert.assertEquals(2, cloudFormationRuleAction.getAllow().getCustomRequestHandling().getInsertHeaders().size());
        Assert.assertEquals("ruleAllowActionHeader1Name", cloudFormationRuleAction.getAllow().getCustomRequestHandling().getInsertHeaders().get(0).getName());
        Assert.assertEquals("ruleAllowActionHeader1Value", cloudFormationRuleAction.getAllow().getCustomRequestHandling().getInsertHeaders().get(0).getValue());
        Assert.assertEquals("ruleAllowActionHeader2Name", cloudFormationRuleAction.getAllow().getCustomRequestHandling().getInsertHeaders().get(1).getName());
        Assert.assertEquals("ruleAllowActionHeader2Value", cloudFormationRuleAction.getAllow().getCustomRequestHandling().getInsertHeaders().get(1).getValue());
    }

    @Test
    public void testRuleActionCustomResponse() {
        RuleAction ruleAction = ObjectMapperHelper.getObject(
            "test-data/test-rule-action-custom-response.json", RuleAction.class);

        software.amazon.awssdk.services.wafv2.model.RuleAction sdkRuleAction =
            StatementCommonsConverter.INSTANCE.convert(ruleAction);
        Assert.assertNull(sdkRuleAction.allow());
        Assert.assertNotNull(sdkRuleAction.block());
        Assert.assertNull(sdkRuleAction.count());
        Assert.assertNotNull(sdkRuleAction.block().customResponse());
        Assert.assertEquals(503, sdkRuleAction.block().customResponse().responseCode().intValue());
        Assert.assertEquals("CustomResponseBodyKey1", sdkRuleAction.block().customResponse().customResponseBodyKey());
        Assert.assertTrue(sdkRuleAction.block().customResponse().hasResponseHeaders());
        Assert.assertEquals(2, sdkRuleAction.block().customResponse().responseHeaders().size());
        Assert.assertEquals("ruleBlockActionHeader1Name", sdkRuleAction.block().customResponse().responseHeaders().get(0).name());
        Assert.assertEquals("ruleBlockActionHeader1Value", sdkRuleAction.block().customResponse().responseHeaders().get(0).value());
        Assert.assertEquals("ruleBlockActionHeader2Name", sdkRuleAction.block().customResponse().responseHeaders().get(1).name());
        Assert.assertEquals("ruleBlockActionHeader2Value", sdkRuleAction.block().customResponse().responseHeaders().get(1).value());

        RuleAction cloudFormationRuleAction =
            StatementCommonsConverter.INSTANCE.invert(sdkRuleAction);
        Assert.assertNull(cloudFormationRuleAction.getAllow());
        Assert.assertNotNull(cloudFormationRuleAction.getBlock());
        Assert.assertNull(cloudFormationRuleAction.getCount());
        Assert.assertNotNull(cloudFormationRuleAction.getBlock().getCustomResponse());
        Assert.assertEquals(503, cloudFormationRuleAction.getBlock().getCustomResponse().getResponseCode().intValue());
        Assert.assertEquals("CustomResponseBodyKey1", cloudFormationRuleAction.getBlock().getCustomResponse().getCustomResponseBodyKey());
        Assert.assertEquals(2, cloudFormationRuleAction.getBlock().getCustomResponse().getResponseHeaders().size());
        Assert.assertEquals("ruleBlockActionHeader1Name", cloudFormationRuleAction.getBlock().getCustomResponse().getResponseHeaders().get(0).getName());
        Assert.assertEquals("ruleBlockActionHeader1Value", cloudFormationRuleAction.getBlock().getCustomResponse().getResponseHeaders().get(0).getValue());
        Assert.assertEquals("ruleBlockActionHeader2Name", cloudFormationRuleAction.getBlock().getCustomResponse().getResponseHeaders().get(1).getName());
        Assert.assertEquals("ruleBlockActionHeader2Value", cloudFormationRuleAction.getBlock().getCustomResponse().getResponseHeaders().get(1).getValue());
    }

    @Test
    public void testRule() {
        Rule rule = ObjectMapperHelper.getObject("test-data/test-rule.json", Rule.class);

        software.amazon.awssdk.services.wafv2.model.Rule sdkRule = Converter.INSTANCE.convert(rule);
        Assert.assertNotNull(sdkRule.action());
        Assert.assertNotNull(sdkRule.statement());
        Assert.assertNotNull(sdkRule.visibilityConfig());
        Assert.assertEquals("testName", sdkRule.name());
        Assert.assertEquals(new Integer(100), sdkRule.priority());
        Assert.assertNull(sdkRule.overrideAction());

        Rule cloudFormationRule = Converter.INSTANCE.invert(sdkRule);
        Assert.assertNotNull(cloudFormationRule.getAction());
        Assert.assertNotNull(cloudFormationRule.getStatement());
        Assert.assertNotNull(cloudFormationRule.getVisibilityConfig());
        Assert.assertEquals("testName", cloudFormationRule.getName());
        Assert.assertEquals(new Integer(100), cloudFormationRule.getPriority());
    }

    @Test
    public void testRuleWithRuleLabels() {
        Rule rule = ObjectMapperHelper.getObject("test-data/test-rule-with-rule-labels.json", Rule.class);

        software.amazon.awssdk.services.wafv2.model.Rule sdkRule = Converter.INSTANCE.convert(rule);
        Assert.assertNotNull(sdkRule.action());
        Assert.assertNotNull(sdkRule.statement());
        Assert.assertNotNull(sdkRule.visibilityConfig());
        Assert.assertEquals("testName", sdkRule.name());
        Assert.assertEquals(new Integer(100), sdkRule.priority());
        Assert.assertNull(sdkRule.overrideAction());
        Assert.assertNotNull(sdkRule.ruleLabels());
        Assert.assertEquals(2,sdkRule.ruleLabels().size());
        Assert.assertNotNull(sdkRule.ruleLabels().get(0).name());
        Assert.assertEquals("testRuleLabel1", sdkRule.ruleLabels().get(0).name());
        Assert.assertNotNull(sdkRule.ruleLabels().get(1).name());
        Assert.assertEquals("testRuleLabel2", sdkRule.ruleLabels().get(1).name());

        Rule cloudFormationRule = Converter.INSTANCE.invert(sdkRule);
        Assert.assertNotNull(cloudFormationRule.getAction());
        Assert.assertNotNull(cloudFormationRule.getStatement());
        Assert.assertNotNull(cloudFormationRule.getVisibilityConfig());
        Assert.assertEquals("testName", cloudFormationRule.getName());
        Assert.assertEquals(new Integer(100), cloudFormationRule.getPriority());
        Assert.assertNotNull(cloudFormationRule.getRuleLabels());
        Assert.assertEquals(2,cloudFormationRule.getRuleLabels().size());
        Assert.assertNotNull(cloudFormationRule.getRuleLabels().get(0).getName());
        Assert.assertEquals("testRuleLabel1", cloudFormationRule.getRuleLabels().get(0).getName());
        Assert.assertNotNull(cloudFormationRule.getRuleLabels().get(1).getName());
        Assert.assertEquals("testRuleLabel2", cloudFormationRule.getRuleLabels().get(1).getName());
    }

    @Test
    public void testSingleHeader() {
        SingleHeader singleHeader = ObjectMapperHelper.getObject(
                "test-data/test-single-header.json", SingleHeader.class);

        software.amazon.awssdk.services.wafv2.model.SingleHeader sdkSingleHeader =
                StatementCommonsConverter.INSTANCE.convert(singleHeader);
        Assert.assertEquals("haystack", sdkSingleHeader.name());

        SingleHeader cloudFormationSingeHeader =
                StatementCommonsConverter.INSTANCE.invert(sdkSingleHeader);
        Assert.assertEquals("haystack", cloudFormationSingeHeader.getName());
    }

    @Test
    public void testSingleQueryArgument() {
        SingleQueryArgument singleQueryArgument = ObjectMapperHelper.getObject(
                "test-data/test-single-query-argument.json", SingleQueryArgument.class);

        software.amazon.awssdk.services.wafv2.model.SingleQueryArgument sdkSingleQueryArgument =
                StatementCommonsConverter.INSTANCE.convert(singleQueryArgument);
        Assert.assertEquals("haystack", sdkSingleQueryArgument.name());

        SingleQueryArgument cloudFormationSingleQueryArgument =
                StatementCommonsConverter.INSTANCE.invert(sdkSingleQueryArgument);
        Assert.assertEquals("haystack", cloudFormationSingleQueryArgument.getName());
    }

    @Test
    public void testSizeConstraintStatement() {
        SizeConstraintStatement sizeConstraintStatement = ObjectMapperHelper.getObject(
                "test-data/test-size-constraint-statement.json", SizeConstraintStatement.class);

        software.amazon.awssdk.services.wafv2.model.SizeConstraintStatement sdkSizeConstraintStatement =
                StatementConverter.INSTANCE.convert(sizeConstraintStatement);
        Assert.assertEquals(ComparisonOperator.LT, sdkSizeConstraintStatement.comparisonOperator());
        Assert.assertEquals(new Long(1024), sdkSizeConstraintStatement.size());
        Assert.assertEquals(2, sdkSizeConstraintStatement.textTransformations().size());
        Assert.assertNotNull(sdkSizeConstraintStatement.fieldToMatch());

        SizeConstraintStatement cloudFormationSizeConstraintStatement =
                StatementConverter.INSTANCE.invert(sdkSizeConstraintStatement);
        Assert.assertEquals("LT", cloudFormationSizeConstraintStatement.getComparisonOperator());
        Assert.assertEquals(new Double(1024), cloudFormationSizeConstraintStatement.getSize());
        Assert.assertEquals(2, cloudFormationSizeConstraintStatement.getTextTransformations().size());
        Assert.assertNotNull(cloudFormationSizeConstraintStatement.getFieldToMatch());
    }

    @Test
    public void testSizeConstraintStatementWithoutTextTransformations() {
        SizeConstraintStatement sizeConstraintStatement = ObjectMapperHelper.getObject(
                "test-data/test-size-constraint-statement.json", SizeConstraintStatement.class);
        sizeConstraintStatement.setTextTransformations(null);

        software.amazon.awssdk.services.wafv2.model.SizeConstraintStatement sdkSizeConstraintStatement =
                StatementConverter.INSTANCE.convert(sizeConstraintStatement);
        Assert.assertEquals(ComparisonOperator.LT, sdkSizeConstraintStatement.comparisonOperator());
        Assert.assertEquals(new Long(1024), sdkSizeConstraintStatement.size());
        Assert.assertNotNull(sdkSizeConstraintStatement.fieldToMatch());

        SizeConstraintStatement cloudFormationSizeConstraintStatement =
                StatementConverter.INSTANCE.invert(sdkSizeConstraintStatement);
        Assert.assertEquals("LT", cloudFormationSizeConstraintStatement.getComparisonOperator());
        Assert.assertEquals(new Double(1024), cloudFormationSizeConstraintStatement.getSize());
        Assert.assertNotNull(cloudFormationSizeConstraintStatement.getFieldToMatch());
    }

    @Test
    public void testSqliMatchStatement() {
        SqliMatchStatement sqliMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-sqli-match-statement.json", SqliMatchStatement.class);

        software.amazon.awssdk.services.wafv2.model.SqliMatchStatement sdkSqliMatchStatement =
                StatementConverter.INSTANCE.convert(sqliMatchStatement);
        Assert.assertEquals(2, sdkSqliMatchStatement.textTransformations().size());
        Assert.assertNotNull(sdkSqliMatchStatement.fieldToMatch());

        SqliMatchStatement cloudFormationSqliMatchStatement =
                StatementConverter.INSTANCE.invert(sdkSqliMatchStatement);
        Assert.assertEquals(2, cloudFormationSqliMatchStatement.getTextTransformations().size());
        Assert.assertNotNull(cloudFormationSqliMatchStatement.getFieldToMatch());
    }

    @Test
    public void testSqliMatchStatementWithoutTextTransformations() {
        SqliMatchStatement sqliMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-sqli-match-statement.json", SqliMatchStatement.class);
        sqliMatchStatement.setTextTransformations(null);

        software.amazon.awssdk.services.wafv2.model.SqliMatchStatement sdkSqliMatchStatement =
                StatementConverter.INSTANCE.convert(sqliMatchStatement);
        Assert.assertNotNull(sdkSqliMatchStatement.fieldToMatch());

        SqliMatchStatement cloudFormationSqliMatchStatement =
                StatementConverter.INSTANCE.invert(sdkSqliMatchStatement);
        Assert.assertNotNull(cloudFormationSqliMatchStatement.getFieldToMatch());
    }

    @Test
    public void testStatementWithThreeLevelNesting() {
        Statement statement = ObjectMapperHelper.getObject(
                "test-data/test-statement-three-levels-nested.json", Statement.class);

        software.amazon.awssdk.services.wafv2.model.Statement sdkStatement =
                StatementConverter.INSTANCE.convertStatement(statement);
        Assert.assertNotNull(sdkStatement.rateBasedStatement());
        Assert.assertNotNull(sdkStatement.rateBasedStatement().scopeDownStatement().notStatement());
        Assert.assertNotNull(sdkStatement.rateBasedStatement().scopeDownStatement()
                .notStatement().statement().sizeConstraintStatement());
        Assert.assertNull(sdkStatement.sizeConstraintStatement());
        Assert.assertNull(sdkStatement.andStatement());
        Assert.assertNull(sdkStatement.byteMatchStatement());
        Assert.assertNull(sdkStatement.geoMatchStatement());
        Assert.assertNull(sdkStatement.ipSetReferenceStatement());
        Assert.assertNull(sdkStatement.managedRuleGroupStatement());
        Assert.assertNull(sdkStatement.notStatement());
        Assert.assertNull(sdkStatement.orStatement());
        Assert.assertNull(sdkStatement.regexPatternSetReferenceStatement());
        Assert.assertNull(sdkStatement.ruleGroupReferenceStatement());
        Assert.assertNull(sdkStatement.sqliMatchStatement());
        Assert.assertNull(sdkStatement.xssMatchStatement());

        Statement cloudFormationStatement = StatementConverter.INSTANCE.invertStatement(sdkStatement);
        Assert.assertNotNull(cloudFormationStatement.getRateBasedStatement());
        Assert.assertNotNull(cloudFormationStatement.getRateBasedStatement()
                .getScopeDownStatement().getNotStatement());
        Assert.assertNotNull(cloudFormationStatement.getRateBasedStatement()
                .getScopeDownStatement().getNotStatement().getStatement().getSizeConstraintStatement());
        Assert.assertNull(cloudFormationStatement.getSizeConstraintStatement());
        Assert.assertNull(cloudFormationStatement.getAndStatement());
        Assert.assertNull(cloudFormationStatement.getByteMatchStatement());
        Assert.assertNull(cloudFormationStatement.getGeoMatchStatement());
        Assert.assertNull(cloudFormationStatement.getIPSetReferenceStatement());
        Assert.assertNull(cloudFormationStatement.getNotStatement());
        Assert.assertNull(cloudFormationStatement.getOrStatement());
        Assert.assertNull(cloudFormationStatement.getRegexPatternSetReferenceStatement());
        Assert.assertNull(cloudFormationStatement.getSqliMatchStatement());
        Assert.assertNull(cloudFormationStatement.getXssMatchStatement());
    }

    @Test
    public void testStatementWithFiveLevelNesting() {
        Statement statement = ObjectMapperHelper.getObject(
            "test-data/test-statement-five-levels-nested.json", Statement.class);

        software.amazon.awssdk.services.wafv2.model.Statement sdkStatement = StatementConverter.INSTANCE
            .convertStatement(statement);
        Assert.assertNotNull(sdkStatement.rateBasedStatement());
        Assert.assertNotNull(sdkStatement.rateBasedStatement().scopeDownStatement());
        software.amazon.awssdk.services.wafv2.model.Statement sdkNestedStatement = sdkStatement.rateBasedStatement()
            .scopeDownStatement();
        Assert.assertNotNull(sdkNestedStatement.notStatement());
        Assert.assertNotNull(sdkNestedStatement.notStatement().statement());
        sdkNestedStatement = sdkNestedStatement.notStatement().statement();
        Assert.assertNotNull(sdkNestedStatement.andStatement());
        Assert.assertNotNull(sdkNestedStatement.andStatement().statements());
        Assert.assertEquals(2, sdkNestedStatement.andStatement().statements().size());
        Assert.assertNotNull(sdkNestedStatement.andStatement().statements().get(0));
        Assert.assertNotNull(sdkNestedStatement.andStatement().statements().get(1));
        software.amazon.awssdk.services.wafv2.model.OrStatement sdkOrStatement = sdkNestedStatement.andStatement()
            .statements().get(0).orStatement();
        Assert.assertEquals(2, sdkOrStatement.statements().size());
        Assert.assertNotNull(sdkOrStatement.statements().get(0).byteMatchStatement());
        Assert.assertNotNull(sdkOrStatement.statements().get(1).sizeConstraintStatement());

        Statement cloudFormationStatement = StatementConverter.INSTANCE.invertStatement(sdkStatement);
        Assert.assertNotNull(cloudFormationStatement.getRateBasedStatement());
        Assert.assertNotNull(cloudFormationStatement.getRateBasedStatement().getScopeDownStatement());
        Statement nestedCloudFormationStatement = cloudFormationStatement.getRateBasedStatement()
            .getScopeDownStatement();
        Assert.assertNotNull(nestedCloudFormationStatement.getNotStatement());
        Assert.assertNotNull(nestedCloudFormationStatement.getNotStatement().getStatement());
        nestedCloudFormationStatement = nestedCloudFormationStatement.getNotStatement().getStatement();
        Assert.assertNotNull(nestedCloudFormationStatement.getAndStatement());
        Assert.assertEquals(2, nestedCloudFormationStatement.getAndStatement().getStatements().size());
        Assert.assertNotNull(nestedCloudFormationStatement.getAndStatement().getStatements().get(0).getOrStatement());
        Assert.assertNotNull(
            nestedCloudFormationStatement.getAndStatement().getStatements().get(1).getByteMatchStatement());
        OrStatement orStatement = nestedCloudFormationStatement.getAndStatement().getStatements().get(0)
            .getOrStatement();
        Assert.assertEquals(2, orStatement.getStatements().size());
        Assert.assertNotNull(orStatement.getStatements().get(0).getByteMatchStatement());
        Assert.assertNotNull(orStatement.getStatements().get(1).getSizeConstraintStatement());

    }

    @Test
    public void testStatement() {
        Statement statement = ObjectMapperHelper.getObject(
                "test-data/test-statement.json", Statement.class);

        software.amazon.awssdk.services.wafv2.model.Statement sdkStatement =
                StatementConverter.INSTANCE.convertStatement(statement);
        Assert.assertNotNull(sdkStatement.sizeConstraintStatement());
        Assert.assertNull(sdkStatement.andStatement());
        Assert.assertNull(sdkStatement.byteMatchStatement());
        Assert.assertNull(sdkStatement.geoMatchStatement());
        Assert.assertNull(sdkStatement.ipSetReferenceStatement());
        Assert.assertNull(sdkStatement.managedRuleGroupStatement());
        Assert.assertNull(sdkStatement.notStatement());
        Assert.assertNull(sdkStatement.orStatement());
        Assert.assertNull(sdkStatement.rateBasedStatement());
        Assert.assertNull(sdkStatement.regexPatternSetReferenceStatement());
        Assert.assertNull(sdkStatement.ruleGroupReferenceStatement());
        Assert.assertNull(sdkStatement.sqliMatchStatement());
        Assert.assertNull(sdkStatement.xssMatchStatement());

        Statement cloudFormationStatement = StatementConverter.INSTANCE.invertStatement(sdkStatement);
        Assert.assertNotNull(cloudFormationStatement.getSizeConstraintStatement());
        Assert.assertNull(cloudFormationStatement.getByteMatchStatement());
        Assert.assertNull(cloudFormationStatement.getGeoMatchStatement());
        Assert.assertNull(cloudFormationStatement.getIPSetReferenceStatement());
        Assert.assertNull(cloudFormationStatement.getRegexPatternSetReferenceStatement());
        Assert.assertNull(cloudFormationStatement.getSqliMatchStatement());
        Assert.assertNull(cloudFormationStatement.getXssMatchStatement());
    }

    @Test
    public void testTextTransformation() {
        TextTransformation textTransformation = ObjectMapperHelper.getObject(
                "test-data/test-text-transformation.json", TextTransformation.class);

        software.amazon.awssdk.services.wafv2.model.TextTransformation sdkTextTransformation =
                StatementCommonsConverter.INSTANCE.convert(textTransformation);
        Assert.assertEquals(new Integer(100), sdkTextTransformation.priority());
        Assert.assertEquals(TextTransformationType.COMPRESS_WHITE_SPACE, sdkTextTransformation.type());

        TextTransformation cloudFormationTextTransformation =
                StatementCommonsConverter.INSTANCE.invert(sdkTextTransformation);
        Assert.assertEquals(new Integer(100), cloudFormationTextTransformation.getPriority());
        Assert.assertEquals("COMPRESS_WHITE_SPACE", cloudFormationTextTransformation.getType());
    }

    @Test
    public void testVisibilityConfig() {
        VisibilityConfig visibilityConfig = ObjectMapperHelper.getObject(
                "test-data/test-visibility-config.json", VisibilityConfig.class);

        software.amazon.awssdk.services.wafv2.model.VisibilityConfig sdkVisibilityConfig =
                StatementCommonsConverter.INSTANCE.convert(visibilityConfig);
        Assert.assertTrue(sdkVisibilityConfig.sampledRequestsEnabled());
        Assert.assertTrue(sdkVisibilityConfig.cloudWatchMetricsEnabled());
        Assert.assertEquals("testMetricName", sdkVisibilityConfig.metricName());

        VisibilityConfig cloudFormationVisibilityConfig =
                StatementCommonsConverter.INSTANCE.invert(sdkVisibilityConfig);
        Assert.assertTrue(cloudFormationVisibilityConfig.getSampledRequestsEnabled());
        Assert.assertTrue(cloudFormationVisibilityConfig.getCloudWatchMetricsEnabled());
        Assert.assertEquals("testMetricName", cloudFormationVisibilityConfig.getMetricName());
    }

    @Test
    public void testXssMatchStatement() {
        XssMatchStatement xssMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-xss-match-statement.json", XssMatchStatement.class);

        software.amazon.awssdk.services.wafv2.model.XssMatchStatement sdkXssMatchStatement =
                StatementConverter.INSTANCE.convert(xssMatchStatement);
        Assert.assertEquals(2, sdkXssMatchStatement.textTransformations().size());
        Assert.assertNotNull(sdkXssMatchStatement.fieldToMatch());

        XssMatchStatement cloudXssMatchStatement =
                StatementConverter.INSTANCE.invert(sdkXssMatchStatement);
        Assert.assertEquals(2, cloudXssMatchStatement.getTextTransformations().size());
        Assert.assertNotNull(cloudXssMatchStatement.getFieldToMatch());
    }

    @Test
    public void testXssMatchStatementWithoutTextTransformations() {
        XssMatchStatement xssMatchStatement = ObjectMapperHelper.getObject(
                "test-data/test-xss-match-statement.json", XssMatchStatement.class);
        xssMatchStatement.setTextTransformations(null);

        software.amazon.awssdk.services.wafv2.model.XssMatchStatement sdkXssMatchStatement =
                StatementConverter.INSTANCE.convert(xssMatchStatement);
        Assert.assertNotNull(sdkXssMatchStatement.fieldToMatch());

        XssMatchStatement cloudXssMatchStatement =
                StatementConverter.INSTANCE.invert(sdkXssMatchStatement);
        Assert.assertNotNull(cloudXssMatchStatement.getFieldToMatch());
    }

    @Test
    public void testCustomResponseBodies() {
        Map<String, com.amazonaws.wafv2.rulegroup.CustomResponseBody> customResponseBodies =
            ObjectMapperHelper.getObjectForCustomResponseBodyMap("test-data/test-custom-response-bodies.json");

        Map<String, software.amazon.awssdk.services.wafv2.model.CustomResponseBody> sdkCustomResponseBodies =
            Converter.INSTANCE.convert(customResponseBodies);

        Assert.assertEquals(3, sdkCustomResponseBodies.size());

        Assert.assertTrue(sdkCustomResponseBodies.containsKey("CustomResponseBodyKey1"));
        software.amazon.awssdk.services.wafv2.model.CustomResponseBody sdkCustomResponseBody1 =
            sdkCustomResponseBodies.get("CustomResponseBodyKey1");
        Assert.assertEquals("TEXT_PLAIN", sdkCustomResponseBody1.contentType().toString());
        Assert.assertEquals("this is a plain text", sdkCustomResponseBody1.content());

        Assert.assertTrue(sdkCustomResponseBodies.containsKey("CustomResponseBodyKey2"));
        software.amazon.awssdk.services.wafv2.model.CustomResponseBody sdkCustomResponseBody2 =
            sdkCustomResponseBodies.get("CustomResponseBodyKey2");
        Assert.assertEquals("APPLICATION_JSON", sdkCustomResponseBody2.contentType().toString());
        Assert.assertEquals("{\"jsonfieldname\": \"jsonfieldvalue\"}", sdkCustomResponseBody2.content());

        Assert.assertTrue(sdkCustomResponseBodies.containsKey("CustomResponseBodyKey3"));
        software.amazon.awssdk.services.wafv2.model.CustomResponseBody sdkCustomResponseBody3 =
            sdkCustomResponseBodies.get("CustomResponseBodyKey3");
        Assert.assertEquals("TEXT_HTML", sdkCustomResponseBody3.contentType().toString());
        Assert.assertEquals("<html>HTML text content<html>", sdkCustomResponseBody3.content());

        Map<String, com.amazonaws.wafv2.rulegroup.CustomResponseBody> cloudFormationCustomResponseBodies =
            Converter.INSTANCE.invert(sdkCustomResponseBodies);

        Assert.assertEquals(customResponseBodies, cloudFormationCustomResponseBodies);
    }

    @Test
    public void testByteMatchStatementWithJsonIncludes() {
        com.amazonaws.wafv2.rulegroup.ByteMatchStatement byteMatchStatement = com.amazonaws.wafv2.rulegroup.helpers.ObjectMapperHelper
                .getObject(
                "test-data/test-byte-match-statement-and-json-include.json", com.amazonaws.wafv2.rulegroup.ByteMatchStatement.class);

        software.amazon.awssdk.services.wafv2.model.ByteMatchStatement sdkByteMatchStatement =
                com.amazonaws.wafv2.rulegroup.converters.StatementConverter.INSTANCE.convert(byteMatchStatement);
        Assert.assertArrayEquals(byteMatchStatement.getSearchString().getBytes(),
                sdkByteMatchStatement.searchString().asByteArray());
        Assert.assertEquals(software.amazon.awssdk.services.wafv2.model.PositionalConstraint.CONTAINS_WORD,
                sdkByteMatchStatement.positionalConstraint());
        Assert.assertNotNull(sdkByteMatchStatement.fieldToMatch());
        Assert.assertEquals(1, sdkByteMatchStatement.textTransformations().size());

        JsonBody jsonBody = sdkByteMatchStatement.fieldToMatch().jsonBody();
        Assert.assertNotNull(jsonBody);
        Assert.assertNull(jsonBody.matchPattern().all());
        Assert.assertEquals(ImmutableList.of("/foo", "/bar"), jsonBody.matchPattern().includedPaths());
        Assert.assertEquals(JsonMatchScope.VALUE, jsonBody.matchScope());
        Assert.assertEquals(BodyParsingFallbackBehavior.NO_MATCH, jsonBody.invalidFallbackBehavior());

        com.amazonaws.wafv2.rulegroup.ByteMatchStatement cloudFormationByteMatchStatement =
                com.amazonaws.wafv2.rulegroup.converters.StatementConverter.INSTANCE.invert(sdkByteMatchStatement);

        com.amazonaws.wafv2.rulegroup.JsonBody cfJsonBody = cloudFormationByteMatchStatement.getFieldToMatch()
                .getJsonBody();

        Assert.assertNotNull(cfJsonBody);
        Assert.assertNull(cfJsonBody.getMatchPattern().getAll());
        Assert.assertEquals(ImmutableList.of("/foo", "/bar"), cfJsonBody.getMatchPattern().getIncludedPaths());
        Assert.assertEquals(JsonMatchScope.VALUE.toString(), cfJsonBody.getMatchScope());
        Assert.assertEquals(BodyParsingFallbackBehavior.NO_MATCH.toString(), cfJsonBody.getInvalidFallbackBehavior());
    }

    @Test
    public void testByteMatchStatementWithJsonAll() {
        com.amazonaws.wafv2.rulegroup.ByteMatchStatement byteMatchStatement = com.amazonaws.wafv2.rulegroup.helpers.ObjectMapperHelper
                .getObject(
                "test-data/test-byte-match-statement-and-json-all.json", com.amazonaws.wafv2.rulegroup.ByteMatchStatement.class);

        software.amazon.awssdk.services.wafv2.model.ByteMatchStatement sdkByteMatchStatement =
                com.amazonaws.wafv2.rulegroup.converters.StatementConverter.INSTANCE.convert(byteMatchStatement);
        Assert.assertArrayEquals(byteMatchStatement.getSearchString().getBytes(),
                sdkByteMatchStatement.searchString().asByteArray());
        Assert.assertEquals(software.amazon.awssdk.services.wafv2.model.PositionalConstraint.CONTAINS_WORD,
                sdkByteMatchStatement.positionalConstraint());
        Assert.assertNotNull(sdkByteMatchStatement.fieldToMatch());
        Assert.assertEquals(1, sdkByteMatchStatement.textTransformations().size());

        JsonBody jsonBody = sdkByteMatchStatement.fieldToMatch().jsonBody();
        Assert.assertNotNull(jsonBody);
        Assert.assertNotNull(jsonBody.matchPattern().all());
        Assert.assertTrue(CollectionUtils.isEmpty(jsonBody.matchPattern().includedPaths()));
        Assert.assertEquals(JsonMatchScope.ALL, jsonBody.matchScope());
        Assert.assertEquals(BodyParsingFallbackBehavior.MATCH, jsonBody.invalidFallbackBehavior());

        com.amazonaws.wafv2.rulegroup.ByteMatchStatement cloudFormationByteMatchStatement =
                com.amazonaws.wafv2.rulegroup.converters.StatementConverter.INSTANCE.invert(sdkByteMatchStatement);

        com.amazonaws.wafv2.rulegroup.JsonBody cfJsonBody = cloudFormationByteMatchStatement.getFieldToMatch()
                .getJsonBody();

        Assert.assertNotNull(cfJsonBody);
        Assert.assertNotNull(cfJsonBody.getMatchPattern().getAll());
        Assert.assertTrue(CollectionUtils.isEmpty(cfJsonBody.getMatchPattern().getIncludedPaths()));
        Assert.assertEquals(JsonMatchScope.ALL.toString(), cfJsonBody.getMatchScope());
        Assert.assertEquals(BodyParsingFallbackBehavior.MATCH.toString(), cfJsonBody.getInvalidFallbackBehavior());
    }

    @Test
    public void testLabelMatchStatement() {
        LabelMatchStatement labelMatchStatement = com.amazonaws.wafv2.rulegroup.helpers.ObjectMapperHelper.getObject(
                "test-data/test-label-match-statement.json", LabelMatchStatement.class);

        software.amazon.awssdk.services.wafv2.model.LabelMatchStatement sdkLabelMatchStatement =
                com.amazonaws.wafv2.rulegroup.converters.StatementConverter.INSTANCE.convert(labelMatchStatement);
        Assert.assertEquals(LabelMatchScope.LABEL, sdkLabelMatchStatement.scope());
        Assert.assertEquals("awswaf:botdetection:search-index:amazon", sdkLabelMatchStatement.key());

        LabelMatchStatement cloudFormationLabelMatchStatment =
                com.amazonaws.wafv2.rulegroup.converters.StatementConverter.INSTANCE.invert(sdkLabelMatchStatement);
        Assert.assertEquals(LabelMatchScope.LABEL.toString(), cloudFormationLabelMatchStatment.getScope());
        Assert.assertEquals("awswaf:botdetection:search-index:amazon", cloudFormationLabelMatchStatment.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLabelMatchStatement() {
        LabelMatchStatement labelMatchStatement = com.amazonaws.wafv2.rulegroup.helpers.ObjectMapperHelper.getObject(
                "test-data/test-invalid-label-match-statement.json", LabelMatchStatement.class);

        software.amazon.awssdk.services.wafv2.model.LabelMatchStatement sdkLabelMatchStatement =
                com.amazonaws.wafv2.rulegroup.converters.StatementConverter.INSTANCE.convert(labelMatchStatement);
        Assert.assertEquals(LabelMatchScope.LABEL, sdkLabelMatchStatement.scope());
    }

    @Test
    public void testAvailableLabels() {
        LabelSummary availableLabel = com.amazonaws.wafv2.rulegroup.helpers.ObjectMapperHelper.getObject(
                "test-data/test-available-label.json", LabelSummary.class);
        LabelSummary availableLabelWithColon = com.amazonaws.wafv2.rulegroup.helpers.ObjectMapperHelper.getObject(
                "test-data/test-available-label-with-colon.json", LabelSummary.class);

        List<software.amazon.awssdk.services.wafv2.model.LabelSummary> sdkAvailableLabels =
                com.amazonaws.wafv2.rulegroup.converters.Converter.INSTANCE.convert(
                        ImmutableList.of(availableLabel,availableLabelWithColon));

        Assert.assertEquals(2, sdkAvailableLabels.size());
        Assert.assertEquals("AvailableLabel1", sdkAvailableLabels.get(0).name());
        Assert.assertEquals("AvailableLabel:2", sdkAvailableLabels.get(1).name());

        List<LabelSummary> cloudFormationAvailableLabels =
                com.amazonaws.wafv2.rulegroup.converters.Converter.INSTANCE.invert(sdkAvailableLabels);
        Assert.assertEquals(2, cloudFormationAvailableLabels.size());
        Assert.assertEquals("AvailableLabel1", cloudFormationAvailableLabels.get(0).getName());
        Assert.assertEquals("AvailableLabel:2", cloudFormationAvailableLabels.get(1).getName());
    }

    @Test
    public void testConsumedLabels() {
        LabelSummary consumedLabel = com.amazonaws.wafv2.rulegroup.helpers.ObjectMapperHelper.getObject(
                "test-data/test-consumed-label.json", LabelSummary.class);
        LabelSummary consumedLabelWithColon = com.amazonaws.wafv2.rulegroup.helpers.ObjectMapperHelper.getObject(
                "test-data/test-consumed-label-with-colon.json", LabelSummary.class);

        List<software.amazon.awssdk.services.wafv2.model.LabelSummary> sdkConsumedLabels =
                com.amazonaws.wafv2.rulegroup.converters.Converter.INSTANCE.convert(
                        ImmutableList.of(consumedLabel,consumedLabelWithColon));

        Assert.assertEquals(2, sdkConsumedLabels.size());
        Assert.assertEquals("ConsumedLabel1", sdkConsumedLabels.get(0).name());
        Assert.assertEquals("ConsumedLabel:2", sdkConsumedLabels.get(1).name());

        List<LabelSummary> cloudFormationConsumedLabels =
                com.amazonaws.wafv2.rulegroup.converters.Converter.INSTANCE.invert(sdkConsumedLabels);
        Assert.assertEquals(2, cloudFormationConsumedLabels.size());
        Assert.assertEquals("ConsumedLabel1", cloudFormationConsumedLabels.get(0).getName());
        Assert.assertEquals("ConsumedLabel:2", cloudFormationConsumedLabels.get(1).getName());
    }
}
