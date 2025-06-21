package net.kotlinx.aws.rdsdata

import aws.sdk.kotlin.services.rdsdata.RdsDataClient
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

val AwsClient.rdsData: RdsDataClient
    get() = getOrCreateClient { RdsDataClient { awsConfig.build(this) }.regist(awsConfig) }
