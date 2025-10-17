package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.QuickSightClient
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

/**
 * https://docs.aws.amazon.com/quicksight/latest/APIReference/qs-data.html
 * */
val AwsClient.quicksight: QuickSightClient
    get() = getOrCreateClient { QuickSightClient { awsConfig.build(this) }.regist(awsConfig) }