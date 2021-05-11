# AWS::WAFv2::LoggingConfiguration Filter

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#behavior" title="Behavior">Behavior</a>" : <i>String</i>,
    "<a href="#conditions" title="Conditions">Conditions</a>" : <i>[ <a href="condition.md">Condition</a>, ... ]</i>,
    "<a href="#requirement" title="Requirement">Requirement</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#behavior" title="Behavior">Behavior</a>: <i>String</i>
<a href="#conditions" title="Conditions">Conditions</a>: <i>
      - <a href="condition.md">Condition</a></i>
<a href="#requirement" title="Requirement">Requirement</a>: <i>String</i>
</pre>

## Properties

#### Behavior

How to handle logs that satisfy the filter's conditions and requirement.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>KEEP</code> | <code>DROP</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Conditions

Match conditions for the filter.

_Required_: Yes

_Type_: List of <a href="condition.md">Condition</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Requirement

Logic to apply to the filtering conditions. You can specify that, in order to satisfy the filter, a log must match all conditions or must match at least one condition.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>MEETS_ALL</code> | <code>MEETS_ANY</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
