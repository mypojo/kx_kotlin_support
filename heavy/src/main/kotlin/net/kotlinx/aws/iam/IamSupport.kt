package net.kotlinx.aws.iam

import aws.sdk.kotlin.services.iam.IamClient
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

val AwsClient.iam: IamClient
    get() = getOrCreateClient { IamClient { awsConfig.build(this) }.regist(awsConfig) }

