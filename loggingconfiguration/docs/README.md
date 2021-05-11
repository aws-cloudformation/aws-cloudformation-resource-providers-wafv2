# AWS::WAFv2::LoggingConfiguration

A WAFv2 Logging Configuration Resource Provider

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::WAFv2::LoggingConfiguration",
    "Properties" : {
        "<a href="#resourcearn" title="ResourceArn">ResourceArn</a>" : <i>String</i>,
        "<a href="#logdestinationconfigs" title="LogDestinationConfigs">LogDestinationConfigs</a>" : <i>[ String, ... ]</i>,
        "<a href="#redactedfields" title="RedactedFields">RedactedFields</a>" : <i>[ <a href="fieldtomatch.md">FieldToMatch</a>, ... ]</i>,
        "<a href="#managedbyfirewallmanager" title="ManagedByFirewallManager">ManagedByFirewallManager</a>" : <i>Boolean</i>,
        "<a href="#loggingfilter" title="LoggingFilter">LoggingFilter</a>" : <i><a href="loggingfilter.md">LoggingFilter</a></i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::WAFv2::LoggingConfiguration
Properties:
    <a href="#resourcearn" title="ResourceArn">ResourceArn</a>: <i>String</i>
    <a href="#logdestinationconfigs" title="LogDestinationConfigs">LogDestinationConfigs</a>: <i>
      - String</i>
    <a href="#redactedfields" title="RedactedFields">RedactedFields</a>: <i>
      - <a href="fieldtomatch.md">FieldToMatch</a></i>
    <a href="#managedbyfirewallmanager" title="ManagedByFirewallManager">ManagedByFirewallManager</a>: <i>Boolean</i>
    <a href="#loggingfilter" title="LoggingFilter">LoggingFilter</a>: <i><a href="loggingfilter.md">LoggingFilter</a></i>
</pre>

## Properties

#### ResourceArn

The Amazon Resource Name (ARN) of the web ACL that you want to associate with LogDestinationConfigs.

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### LogDestinationConfigs

The Amazon Kinesis Data Firehose Amazon Resource Name (ARNs) that you want to associate with the web ACL.

_Required_: Yes

_Type_: List of String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### RedactedFields

The parts of the request that you want to keep out of the logs. For example, if you redact the HEADER field, the HEADER field in the firehose will be xxx.

_Required_: No

_Type_: List of <a href="fieldtomatch.md">FieldToMatch</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ManagedByFirewallManager

Indicates whether the logging configuration was created by AWS Firewall Manager, as part of an AWS WAF policy configuration. If true, only Firewall Manager can modify or delete the configuration.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LoggingFilter

Filtering that specifies which web requests are kept in the logs and which are dropped. You can filter on the rule action and on the web request labels that were applied by matching rules during web ACL evaluation.

_Required_: No

_Type_: <a href="loggingfilter.md">LoggingFilter</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ResourceArn.
