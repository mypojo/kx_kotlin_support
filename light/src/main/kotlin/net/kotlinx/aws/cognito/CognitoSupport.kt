package net.kotlinx.aws.cognito

import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

val AwsClient.cognito: CognitoIdentityProviderClient
    get() = getOrCreateClient { CognitoIdentityProviderClient { awsConfig.build(this) }.regist(awsConfig) }