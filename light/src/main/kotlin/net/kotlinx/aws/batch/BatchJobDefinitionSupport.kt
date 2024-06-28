package net.kotlinx.aws.batch

import aws.sdk.kotlin.services.batch.BatchClient
import aws.sdk.kotlin.services.batch.model.JobDefinitionType
import aws.sdk.kotlin.services.batch.registerJobDefinition

/** 업데이트에 필요한 필수값만 포함한다. */
suspend fun BatchClient.revisionJobDefinition(jobDefinitionName: String, image: String) {
    this.registerJobDefinition {
        this.jobDefinitionName = jobDefinitionName
        this.type = JobDefinitionType.Container
        this.containerProperties {
            this.image = image
        }
        //.,,,
    }
    throw UnsupportedOperationException()
}