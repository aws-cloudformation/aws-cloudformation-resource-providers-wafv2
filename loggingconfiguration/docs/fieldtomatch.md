# AWS::WAFv2::LoggingConfiguration FieldToMatch

A key-value pair to associate with a resource.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#jsonbody" title="JsonBody">JsonBody</a>" : <i><a href="fieldtomatch.md">FieldToMatch</a></i>,
    "<a href="#method" title="Method">Method</a>" : <i>Map</i>,
    "<a href="#querystring" title="QueryString">QueryString</a>" : <i>Map</i>,
    "<a href="#singleheader" title="SingleHeader">SingleHeader</a>" : <i><a href="fieldtomatch.md">FieldToMatch</a></i>,
    "<a href="#uripath" title="UriPath">UriPath</a>" : <i>Map</i>
}
</pre>

### YAML

<pre>
<a href="#jsonbody" title="JsonBody">JsonBody</a>: <i><a href="fieldtomatch.md">FieldToMatch</a></i>
<a href="#method" title="Method">Method</a>: <i>Map</i>
<a href="#querystring" title="QueryString">QueryString</a>: <i>Map</i>
<a href="#singleheader" title="SingleHeader">SingleHeader</a>: <i><a href="fieldtomatch.md">FieldToMatch</a></i>
<a href="#uripath" title="UriPath">UriPath</a>: <i>Map</i>
</pre>

## Properties

#### JsonBody

_Required_: No

_Type_: <a href="fieldtomatch.md">FieldToMatch</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Method

Inspect the HTTP method. The method indicates the type of operation that the request is asking the origin to perform. 

_Required_: No

_Type_: Map

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### QueryString

Inspect the query string. This is the part of a URL that appears after a ? character, if any. 

_Required_: No

_Type_: Map

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SingleHeader

_Required_: No

_Type_: <a href="fieldtomatch.md">FieldToMatch</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### UriPath

Inspect the request URI path. This is the part of a web request that identifies a resource, for example, /images/daily-ad.jpg. 

_Required_: No

_Type_: Map

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

