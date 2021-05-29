# AWS::WAFv2::LoggingConfiguration
This resource enables the creation of AWS WAFv2 Logging Configuration for WAFv2 WebACL via Cloudformation. In effect, you will now have the resource, AWS::WAFv2::LoggingConfiguration available to you to deploy using Cloudformation.

This has been made possible using Cloudformation Registry. For more details about Cloudformation Registry - please look at: https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/registry.html

## Pre-Requisites

1. Java Version 8 or higher. Make sure you are using Java 8 by running `java -version`.
2. Apache Maven - `brew install maven` - if you are on a Mac
3. Cloudformation CLI and Cloudformation Java Plugin - https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/what-is-cloudformation-cli.html

The Cloudformation CLI tool is needed to submit this resource to Cloudformation Registry as a Private Type. You can also look at - https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-wafv2/blob/bda16eaff1b6d5dd3d8cf3f8ef6abec34ff3e7b3/.github/workflows/pri-ci.yml - for build steps

## Submitting this resource as a Private Type

The following instructions help you to extract the code in this repository and submit this resource as a private type to your account. You will also need the `aws-wafv2-commons` libraries in your local acccount before you build this package.  

1. Pull the `aws-wafv2-commons` code from - https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-wafv2/tree/main/aws-wafv2-commons
2. Change to the `aws-wafv2-commons` directory - `cd aws-wafv2-commons`
3. Install the the library into your local maven path using the command - `mvn clean install`
4. Pull Logging Configuration code from - https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-wafv2
5. Change to the Logging Configuration directory - `cd aws-cloudformation-resource-providers-wafv2/loggingconfiguration`
6. Build this resource - `mvn clean package`
7. Submit this resource to your account using - `cfn submit -vv --set-default`
8. Go to Cloudformation -> Cloudformation Registry -> Private. You should see a new type - AWS::WAFv2::LoggingConfiguration

Congratulations! You can now use AWS::WAFv2::LoggingConfiguration in your Cloudformation Templates. Example Templates are available in the repository

## A few things to note -
1. The Kinesis Firehose name must begin with aws-waf-logs
2. Within RedactedFields, AllQueryArguments, Body and SingleQueryArgument are not supported
3. Only one Firehose Stream can be associated with a Wafv2 WebACL. More than 1 is not supported
4. If a Logging Configuration already exists for a given resource, this Cloudformation resource will not replace an existing Logging Configuration during a Create operation.
