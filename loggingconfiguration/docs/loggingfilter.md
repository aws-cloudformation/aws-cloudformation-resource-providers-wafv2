# AWS::WAFv2::LoggingConfiguration LoggingFilter

Filtering that specifies which web requests are kept in the logs and which are dropped. You can filter on the rule action and on the web request labels that were applied by matching rules during web ACL evaluation.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#defaultbehavior" title="DefaultBehavior">DefaultBehavior</a>" : <i>String</i>,
    "<a href="#filters" title="Filters">Filters</a>" : <i>[ <a href="filter.md">Filter</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#defaultbehavior" title="DefaultBehavior">DefaultBehavior</a>: <i>String</i>
<a href="#filters" title="Filters">Filters</a>: <i>
      - <a href="filter.md">Filter</a></i>
</pre>

## Properties

#### DefaultBehavior

Default handling for logs that don't match any of the specified filtering conditions.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>KEEP</code> | <code>DROP</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Filters

The filters that you want to apply to the logs.

_Required_: Yes

_Type_: List of <a href="filter.md">Filter</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
