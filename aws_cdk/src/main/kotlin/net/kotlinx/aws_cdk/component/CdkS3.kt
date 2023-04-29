package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkDeploymentType
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core1.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.s3.*

/**
 * 디폴트로 자동 암호화임
 * */
open class CdkS3(
    val project: CdkProject,
    val name: String,
) : CdkDeploymentType {

    override var deploymentType: DeploymentType = DeploymentType.dev

    override val logicalName: String
        get() = "${project.projectName}-${name}-${deploymentType}"

    lateinit var iBucket: IBucket

    fun create(stack: Stack, bucketProps: BucketProps): CdkS3 {
        iBucket = Bucket(stack, logicalName, bucketProps)
        TagUtil.tag(iBucket, deploymentType)
        return this
    }

    fun load(stack: Stack): CdkS3 {
        iBucket = Bucket.fromBucketName(stack, logicalName, logicalName)
        return this
    }

    /** 디폴트 CORS (Cross-Origin Resource Sharing). 필요하면 더 추가해서 사용할것 */
    companion object {
        val CORS_OPEN = CorsRule.builder()
            .allowedOrigins(listOf("*"))
            .allowedHeaders(listOf("*"))
            .allowedMethods(listOf(HttpMethods.GET, HttpMethods.HEAD))
            .build()!!
    }
}