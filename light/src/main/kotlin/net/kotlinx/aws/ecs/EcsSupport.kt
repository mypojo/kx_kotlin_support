package net.kotlinx.aws.ecs

import aws.sdk.kotlin.services.ecs.EcsClient
import aws.sdk.kotlin.services.ecs.model.UpdateServiceResponse
import aws.sdk.kotlin.services.ecs.updateService
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

val AwsClient.ecs: EcsClient
    get() = getOrCreateClient { EcsClient { awsConfig.build(this) }.regist(awsConfig) }

/** 코드 빌드 후 터치 */
suspend fun EcsClient.touch(clusterName: String, serviceName: String): UpdateServiceResponse = this.updateService {
    this.cluster = clusterName
    this.service = serviceName
    this.forceNewDeployment = true
}


/**
 * desiredCount 수만 변경한다
 * 만약 Min tasks 이상으로 늘리더라도 오토스케일링이 작동하면 Min tasks 로 돌아간다
 *  */
suspend fun EcsClient.updateServiceCount(clusterName: String, serviceName: String, cnt: Int): UpdateServiceResponse = this.updateService {
    this.cluster = clusterName
    this.service = serviceName
    this.desiredCount = cnt
}
