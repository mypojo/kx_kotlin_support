package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkDeploymentType
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.toCdk
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core.DeploymentType
import software.amazon.awscdk.Duration
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.s3.*

/**
 * 디폴트로 자동 암호화임
 * */
class CdkS3(
    val project: CdkProject,
    val name: String,
) : CdkDeploymentType {

    override var deploymentType: DeploymentType = DeploymentType.dev

    override val logicalName: String
        get() = "${project.projectName}-${name}-${deploymentType}"

    lateinit var iBucket: IBucket

    var lifecycleRules: List<LifecycleRule> = mutableListOf()

    var removalPolicy = RemovalPolicy.RETAIN

    var versioned: Boolean = false
    var publicReadAccess: Boolean = false
    var cors = listOf(CORS_OPEN)

    fun create(stack: Stack): CdkS3 {
        val bucketProps = BucketProps.builder()
            .bucketName(this.logicalName)
            .removalPolicy(removalPolicy)
            .lifecycleRules(lifecycleRules)
            .versioned(versioned)
            .apply {
                if (publicReadAccess) {
                    publicReadAccess(true)
                    cors(cors)
                }
            }
            .build()
        iBucket = Bucket(stack, logicalName, bucketProps)
        TagUtil.tag(iBucket, deploymentType)
        return this
    }

    fun load(stack: Stack): CdkS3 {
        iBucket = Bucket.fromBucketName(stack, logicalName, logicalName)
        return this
    }

    //==================================================== S3 라이프사이클 ======================================================/**
    // * 자주 사용하는 S3 라이프사이클 정책 샘플.
    // * 정책 설정하면 모든 대상에 대해서 소급 적용됨
    // * IA 로 이동시키는건 별도 설정할것

    /**
     * 버전X용 : x일 후에 삭제
     * ex) athena/ -> 금방 지워도 됨
     * ex) cloudtrail/ -> x년 보관  Duration.days(365 * 5)
     * ex) "athena", "athena/", Duration.days(3)
     * */
    fun addLifeCycleDelete(name: String, prefix: String, delete: kotlin.time.Duration) {
        lifecycleRules += LifecycleRule.builder().id("${project.projectName}-lifecycle-$name-$deploymentType")
            .prefix(prefix)
            .abortIncompleteMultipartUploadAfter(Duration.days(7))// 기본설정
            .expiration(delete.toCdk())
            .build()
    }

    /**
     * 버전O용 : x일 후에 삭제마킹 -> x일 후에 삭제
     * ex) download/ -> 90일 후에 삭제 마킹 -> 삭제마킹 이후 30일 이후 실제 삭제
     * */
    fun addLifeCycleMarkAndDelete(name: String, prefix: String, deleteMark: kotlin.time.Duration, delete: kotlin.time.Duration) {
        lifecycleRules += LifecycleRule.builder().id("${project.projectName}-lifecycle-$name-$deploymentType")
            .prefix(prefix)
            .abortIncompleteMultipartUploadAfter(Duration.days(7))// 기본설정
            .expiration(deleteMark.toCdk()) //xx 후에 삭제 마킹
            .noncurrentVersionExpiration(delete.toCdk())  // 삭제마킹후 xx후에 진짜 삭제
            .build()
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