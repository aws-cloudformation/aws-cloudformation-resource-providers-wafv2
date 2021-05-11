package software.amazon.wafv2.loggingconfiguration;

import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {
    public static Wafv2Client getClient() {
        return Wafv2Client.builder()
                  .httpClient(LambdaWrapper.HTTP_CLIENT)
                  .build();
      }
}
