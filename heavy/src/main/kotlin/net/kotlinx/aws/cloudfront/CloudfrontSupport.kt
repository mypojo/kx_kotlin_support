package net.kotlinx.aws.cloudfront

import aws.sdk.kotlin.services.cloudfront.CloudFrontClient
import aws.sdk.kotlin.services.cloudfront.createInvalidation
import aws.sdk.kotlin.services.cloudfront.getDistribution
import aws.sdk.kotlin.services.cloudfront.model.Distribution
import aws.sdk.kotlin.services.cloudfront.model.InvalidationBatch
import aws.sdk.kotlin.services.cloudfront.model.Paths
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

val AwsClient.cloudFront: CloudFrontClient
    get() = getOrCreateClient { CloudFrontClient { awsConfig.build(this) }.regist(awsConfig) }

/**
 * 클라우드 프론트 캐시 제거
 * ex) static hosting 배포 후 캐시 제거
 * "/images/\*"
 *  주의!!   "/aa/bb/\*.png" 이렇게는 작동을 안함!!  접미어만 쓸것
 * */
suspend fun CloudFrontClient.clear(distributionId: String, paths: List<String>) {
    this.createInvalidation {
        this.distributionId = distributionId
        this.invalidationBatch = InvalidationBatch {
            this.paths = Paths {
                quantity = paths.size
                items = paths
            }
            this.callerReference = System.currentTimeMillis().toString()
        }
    }
}

/** 단축 메소드  */
suspend fun CloudFrontClient.getDistribution(distributionId: String): Distribution {
    return this.getDistribution {
        id = distributionId
    }.distribution!!
}