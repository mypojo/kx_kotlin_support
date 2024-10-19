package net.kotlinx.aws.sts

import aws.sdk.kotlin.services.sts.StsClient
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

val AwsClient.sts: StsClient
    get() = getOrCreateClient { StsClient { awsConfig.build(this) }.regist(awsConfig) }

