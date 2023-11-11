package net.kotlinx.aws.ecs

import aws.sdk.kotlin.services.ecs.EcsClient
import aws.sdk.kotlin.services.ecs.model.UpdateServiceResponse
import aws.sdk.kotlin.services.ecs.updateService


/** 코드 빌드 후 터치 */
suspend fun EcsClient.touch(clusterName: String, serviceName: String): UpdateServiceResponse = this.updateService {
    this.cluster = clusterName
    this.service = serviceName
    this.forceNewDeployment = true
}


/** 테스트 필요! */
suspend fun EcsClient.updateServiceCount(clusterName: String, serviceName: String, cnt: Int): UpdateServiceResponse = this.updateService {
    this.cluster = clusterName
    this.service = serviceName
    this.desiredCount = cnt
}
