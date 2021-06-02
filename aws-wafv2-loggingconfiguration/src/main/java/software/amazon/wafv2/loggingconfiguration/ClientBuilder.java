package software.amazon.wafv2.loggingconfiguration;

import software.amazon.awssdk.services.wafv2.Wafv2Client;
import com.amazonaws.wafv2.commons.CustomerAPIClientBuilder;

public class ClientBuilder {
    public static Wafv2Client getClient() { return CustomerAPIClientBuilder.getClient(); }
}
