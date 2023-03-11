package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkInterface
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.DeploymentType
import net.kotlinx.aws_cdk.util.TagUtil
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.s3.BucketProps
import software.amazon.awscdk.services.s3.IBucket

class CdkS3(
    val project: CdkProject,
    val name: String,
    val deploymentType: DeploymentType,
) : CdkInterface {

    override val logicalName: String
        get() = "${project.projectName}-${name}-${deploymentType}"

    var ibucket: IBucket? = null

    fun create(stack: Stack, bucketProps: BucketProps): CdkS3 {
        ibucket = Bucket(stack, logicalName, bucketProps)
        TagUtil.tag(ibucket!!, deploymentType)
        return this
    }

    fun load(stack: Stack): CdkS3 {
        ibucket = Bucket.fromBucketName(stack, logicalName, logicalName)
        return this
    }

//    /** 디폴트 CORS (Cross-Origin Resource Sharing). 필요하면 더 추가해서 사용할것 */
//    val  CORS =
//    {
//        allowedOrigins: ['*'],
//        allowedHeaders: ['*'],
//        allowedMethods: [HttpMethods.GET, HttpMethods.HEAD, HttpMethods.PUT],
//    };
}