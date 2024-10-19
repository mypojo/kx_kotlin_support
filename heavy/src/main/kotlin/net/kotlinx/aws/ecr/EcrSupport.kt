package net.kotlinx.aws.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.ecr.batchGetImage
import aws.sdk.kotlin.services.ecr.model.Image
import aws.sdk.kotlin.services.ecr.model.ImageIdentifier
import aws.sdk.kotlin.services.ecr.model.PutImageResponse
import aws.sdk.kotlin.services.ecr.putImage
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist


val AwsClient.ecr: EcrClient
    get() = getOrCreateClient { EcrClient { awsConfig.build(this) }.regist(awsConfig) }

/** 태그로 조회 (ECR에서 태그는 유니크함) */
suspend fun EcrClient.findByTag(repositoryName: String, imageTag: String): Image = this.batchGetImage {
    this.repositoryName = repositoryName
    this.imageIds = listOf(
        ImageIdentifier {
            this.imageTag = imageTag
        }
    )
}.images!!.first()

/**
 * 태그 수정
 * 해당 태그가 전체 저장소에 없음 -> 신규 입력
 * 다른 이미지가 해당 태그 쓰고있음 -> 저장소 옵션에 immutable 이라면 오류. 아니라면 태그 타게팅이동  (AWS-Batch에서 jobDef의 수정을 최소화 하기 위해서 이렇게 써야함)
 *  */
suspend fun EcrClient.updateTag(repositoryName: String, imageManifest: String, imageTag: String): PutImageResponse = this.putImage {
    this.repositoryName = repositoryName
    this.imageTag = imageTag
    this.imageManifest = imageManifest
}

//==================================================== 응용 ======================================================

/** 기존 태그에 새 태그를 붙여줌 */
suspend fun EcrClient.findAndUpdateTag(repositoryName: String, existTag: String, newTag: String) {
    val images = findByTag(repositoryName, existTag)
    updateTag(repositoryName, images.imageManifest!!, newTag)
}
