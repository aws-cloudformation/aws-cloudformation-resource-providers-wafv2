package software.amazon.wafv2.loggingconfiguration.converters;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import software.amazon.wafv2.converters.Converter;
import software.amazon.wafv2.loggingconfiguration.FieldToMatch;
import software.amazon.wafv2.loggingconfiguration.LoggingFilter;

public class ConvertersTests {

    @Test
    public void testFieldToMatch() {
        FieldToMatch fieldToMatch = ObjectMapperHelper.getObject("test-data/test-field-to-match-single-header.json", FieldToMatch.class);

        software.amazon.awssdk.services.wafv2.model.FieldToMatch sdkFieldToMatch = Converter.INSTANCE.convert(fieldToMatch);
        assertThat(fieldToMatch.getSingleHeader()).isNotNull();
        assertThat(fieldToMatch.getMethod()).isNull();
        assertThat(fieldToMatch.getQueryString()).isNull();
        assertThat(fieldToMatch.getUriPath()).isNull();

        FieldToMatch cloudFormationFieldToMatch = Converter.INSTANCE.invert(sdkFieldToMatch);
        assertThat(cloudFormationFieldToMatch.getSingleHeader()).isNotNull();
        assertThat(cloudFormationFieldToMatch.getMethod()).isNull();
        assertThat(cloudFormationFieldToMatch.getQueryString()).isNull();
        assertThat(cloudFormationFieldToMatch.getUriPath()).isNull();
    }
    
    @Test
    public void testFieldToMatchWithJSONBody() {
        FieldToMatch fieldToMatch = ObjectMapperHelper.getObject("test-data/test-field-to-match-json-body.json", FieldToMatch.class);

        software.amazon.awssdk.services.wafv2.model.FieldToMatch sdkFieldToMatch = Converter.INSTANCE.convert(fieldToMatch);
        assertThat(fieldToMatch.getJsonBody()).isNotNull();
        assertThat(fieldToMatch.getJsonBody().getInvalidFallbackBehavior()).isEqualTo("EVALUATE_AS_STRING");
        assertThat(fieldToMatch.getJsonBody().getMatchPattern().getAll()).isNotNull();
        assertThat(fieldToMatch.getJsonBody().getMatchScope()).isEqualTo("ALL");
        assertThat(fieldToMatch.getSingleHeader()).isNull();
        assertThat(fieldToMatch.getMethod()).isNull();
        assertThat(fieldToMatch.getQueryString()).isNull();
        assertThat(fieldToMatch.getUriPath()).isNull();

        FieldToMatch cloudFormationFieldToMatch = Converter.INSTANCE.invert(sdkFieldToMatch);
        assertThat(cloudFormationFieldToMatch.getJsonBody()).isNotNull();
        assertThat(cloudFormationFieldToMatch.getJsonBody().getInvalidFallbackBehavior()).isEqualTo("EVALUATE_AS_STRING");
        assertThat(cloudFormationFieldToMatch.getJsonBody().getMatchPattern().getAll()).isNotNull();
        assertThat(cloudFormationFieldToMatch.getJsonBody().getMatchScope()).isEqualTo("ALL");
        assertThat(cloudFormationFieldToMatch.getSingleHeader()).isNull();
        assertThat(cloudFormationFieldToMatch.getMethod()).isNull();
        assertThat(cloudFormationFieldToMatch.getQueryString()).isNull();
        assertThat(cloudFormationFieldToMatch.getUriPath()).isNull();
    }
    
    @Test
    public void testFieldToMatchWithUriPath() {
        FieldToMatch fieldToMatch = ObjectMapperHelper.getObject("test-data/test-field-to-match-uri-path.json", FieldToMatch.class);

        software.amazon.awssdk.services.wafv2.model.FieldToMatch sdkFieldToMatch = Converter.INSTANCE.convert(fieldToMatch);
        assertThat(fieldToMatch.getJsonBody()).isNull();
        assertThat(fieldToMatch.getSingleHeader()).isNull();
        assertThat(fieldToMatch.getMethod()).isNull();
        assertThat(fieldToMatch.getQueryString()).isNull();
        assertThat(fieldToMatch.getUriPath()).isNotNull();

        FieldToMatch cloudFormationFieldToMatch = Converter.INSTANCE.invert(sdkFieldToMatch);
        assertThat(cloudFormationFieldToMatch.getJsonBody()).isNull();
        assertThat(cloudFormationFieldToMatch.getSingleHeader()).isNull();
        assertThat(cloudFormationFieldToMatch.getMethod()).isNull();
        assertThat(cloudFormationFieldToMatch.getQueryString()).isNull();
        assertThat(cloudFormationFieldToMatch.getUriPath()).isNotNull();
    }
    
    @Test
    public void testLoggingFilterWithActionCondition() {
        LoggingFilter loggingFilter = ObjectMapperHelper.getObject("test-data/test-logging-filter-action-condition.json", LoggingFilter.class);

        software.amazon.awssdk.services.wafv2.model.LoggingFilter sdkLoggingFilter = Converter.INSTANCE.convert(loggingFilter);
        assertThat(loggingFilter.getDefaultBehavior()).isEqualTo("KEEP");
        assertThat(loggingFilter.getFilters().get(0).getBehavior()).isEqualTo("KEEP");
        assertThat(loggingFilter.getFilters().get(0).getRequirement()).isEqualTo("MEETS_ANY");
        assertThat(loggingFilter.getFilters().get(0).getConditions().get(0).getActionCondition().getAction()).isEqualTo("BLOCK");
        assertThat(loggingFilter.getFilters().get(0).getConditions().get(0).getLabelNameCondition()).isNull();

        LoggingFilter cloudFormationLoggingFilter = Converter.INSTANCE.invert(sdkLoggingFilter);
        assertThat(cloudFormationLoggingFilter.getDefaultBehavior()).isEqualTo("KEEP");
        assertThat(cloudFormationLoggingFilter.getFilters().get(0).getBehavior()).isEqualTo("KEEP");
        assertThat(cloudFormationLoggingFilter.getFilters().get(0).getRequirement()).isEqualTo("MEETS_ANY");
        assertThat(cloudFormationLoggingFilter.getFilters().get(0).getConditions().get(0).getActionCondition().getAction()).isEqualTo("BLOCK");
        assertThat(cloudFormationLoggingFilter.getFilters().get(0).getConditions().get(0).getLabelNameCondition()).isNull();
    }
    
    @Test
    public void testLoggingFilterWithLabelNameCondition() {
        LoggingFilter loggingFilter = ObjectMapperHelper.getObject("test-data/test-logging-filter-label-name-condition.json", LoggingFilter.class);

        software.amazon.awssdk.services.wafv2.model.LoggingFilter sdkLoggingFilter = Converter.INSTANCE.convert(loggingFilter);
        assertThat(loggingFilter.getDefaultBehavior()).isEqualTo("KEEP");
        assertThat(loggingFilter.getFilters().get(0).getBehavior()).isEqualTo("KEEP");
        assertThat(loggingFilter.getFilters().get(0).getRequirement()).isEqualTo("MEETS_ANY");
        assertThat(loggingFilter.getFilters().get(0).getConditions().get(0).getLabelNameCondition().getLabelName()).isEqualTo("testlabel");
        assertThat(loggingFilter.getFilters().get(0).getConditions().get(0).getActionCondition()).isNull();

        LoggingFilter cloudFormationLoggingFilter = Converter.INSTANCE.invert(sdkLoggingFilter);
        assertThat(cloudFormationLoggingFilter.getDefaultBehavior()).isEqualTo("KEEP");
        assertThat(cloudFormationLoggingFilter.getFilters().get(0).getBehavior()).isEqualTo("KEEP");
        assertThat(cloudFormationLoggingFilter.getFilters().get(0).getRequirement()).isEqualTo("MEETS_ANY");
        assertThat(cloudFormationLoggingFilter.getFilters().get(0).getConditions().get(0).getLabelNameCondition().getLabelName()).isEqualTo("testlabel");
        assertThat(cloudFormationLoggingFilter.getFilters().get(0).getConditions().get(0).getActionCondition()).isNull();
    }
}
