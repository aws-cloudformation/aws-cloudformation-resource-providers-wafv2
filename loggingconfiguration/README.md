# AWS::WAFv2::LoggingConfiguration
This resource enables the creation of AWS WAFv2 Logging Configuration for WAFv2 WebACL via Cloudformation. In effect, you will now have the resource, AWS::WAFv2::LoggingConfiguration available to you to deploy using Cloudformation.  

This has been made possible using Cloudformation Registry. For more details about Cloudformation Registry - please look at: https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/registry.html

## Pre-Requisites

1. Java Version 8 or higher
2. Apache Maven - `brew install maven` - if you are on a Mac
3. Cloudformation CLI and Cloudformation Java Plugin - https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/what-is-cloudformation-cli.html
The Cloudformation CLI tool is needed to submit this resource to Cloudformation Registry as a Private Type. 

## Submitting this resource as a Private Type 

The following instructions help you to extract the code in this repository and submit this resource as a private type to your account.

1. Pull this code to your laptop using your favorite method. For me - `git clone git@github.com:advaj/aws-cloudformation-resource-providers-wafv2.git`
2. Change to the Logging Configuration directory - `cd aws-cloudformation-resource-providers-wafv2/loggingconfiguration`
3. Build this resource - `mvn clean package` - Make sure you are using Java 8 by running `java -version` on your laptop.
4. Submit this resource to your account using - `cfn submit -vv --set-default`
5. Go to Cloudformation -> Cloudformation Registry -> Private. You should see a new type - AWS::WAFv2::LoggingConfiguration 

Congratulations! You can now use AWS::WAFv2::LoggingConfiguration in your Cloudformation Templates. Example Templates are available in the repository

## A few things to note - 
1. The Kinesis Firehose name must begin with aws-waf-logs
2. Within RedactedFields, AllQueryArguments, Body and SingleQueryArgument are not supported
3. Only one Firehose Stream can be associated with a Wafv2 WebACL. More than 1 is not supported 
4. If a Logging Configuration already exists for a given resource, this Cloudformation resource will not replace it. 
