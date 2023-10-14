package net.kotlinx.aws.ecs

import aws.sdk.kotlin.services.ecs.EcsClient
import aws.sdk.kotlin.services.ecs.model.UpdateServiceResponse
import aws.sdk.kotlin.services.ecs.updateService


/** 코드 빌드 후 터치 */
suspend fun EcsClient.touch(clusterName: String, serviceName: String): UpdateServiceResponse = this.updateService {
    this.service = serviceName
    this.cluster = clusterName
    this.forceNewDeployment = true
}
