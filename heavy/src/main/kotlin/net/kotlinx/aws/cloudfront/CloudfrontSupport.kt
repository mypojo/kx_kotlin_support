package net.kotlinx.aws.cloudfront

import aws.sdk.kotlin.services.cloudfront.CloudFrontClient
import aws.sdk.kotlin.services.cloudfront.createInvalidation
import aws.sdk.kotlin.services.cloudfront.model.Paths


/**
 * 클라우드 프론트 캐시 제거
 * ex) static hosting 배포 후 캐시 제거
 * */
suspend fun CloudFrontClient.clear(distributionId: String, paths: List<String>) {
    this.createInvalidation {
        this.distributionId = distributionId
        invalidationBatch {
            this.paths = Paths {
                items = paths
            }
        }
    }
}