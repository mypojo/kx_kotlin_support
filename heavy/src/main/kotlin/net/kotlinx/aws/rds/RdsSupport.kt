package net.kotlinx.aws.rds

import aws.sdk.kotlin.services.rds.RdsClient
import aws.sdk.kotlin.services.rds.model.ServerlessV2ScalingConfiguration
import aws.sdk.kotlin.services.rds.modifyDbCluster
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

val AwsClient.rds: RdsClient
    get() = getOrCreateClient { RdsClient { awsConfig.build(this) }.regist(awsConfig) }

/**
 * serverlessV2 에서 스케일링 조절하기
 * https://docs.aws.amazon.com/ko_kr/AmazonRDS/latest/AuroraUserGuide/aurora-serverless-v2.setting-capacity.html
 *  */
suspend fun RdsClient.modifyCurrentDbClusterCapacity(clusterIdentifier: String, capacity: Pair<Double, Double>) {
    this.modifyDbCluster {
        this.dbClusterIdentifier = clusterIdentifier
        this.serverlessV2ScalingConfiguration = ServerlessV2ScalingConfiguration {
            this.minCapacity = capacity.first
            this.maxCapacity = capacity.second
        }
    }
}
