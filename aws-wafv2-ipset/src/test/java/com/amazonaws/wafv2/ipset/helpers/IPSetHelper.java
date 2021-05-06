package com.amazonaws.wafv2.ipset.helpers;

import software.amazon.awssdk.services.wafv2.model.GetIpSetResponse;
import software.amazon.awssdk.services.wafv2.model.IPAddressVersion;
import software.amazon.awssdk.services.wafv2.model.IPSet;

import java.util.Arrays;

public class IPSetHelper {

    public static String name = "testIpSet";
    public static  String description = "test description";
    public static String id = "8208f8fc-bf93-4607-9aed-81c497c9eae0";
    public static String arn = "arn:aws:wafv2:us-west-2:148466149608:regional/ipset/foo/8208f8fc-bf93-4607-9aed-81c497c9eae0";
    public static String ipAddressVersion = "IPV4";
    public static String[] address = {"1.1.1.1/32", "1.2.1.1/32"};

    public static GetIpSetResponse getReadIPSetResponse(){
        IPSet ipset = IPSet.builder()
                .arn(arn)
                .description(description)
                .id(id)
                .ipAddressVersion(IPAddressVersion.IPV4)
                .addresses(Arrays.asList(address))
                .name(name)
                .build();
        return  GetIpSetResponse.builder().ipSet(ipset).build();

    }
}
