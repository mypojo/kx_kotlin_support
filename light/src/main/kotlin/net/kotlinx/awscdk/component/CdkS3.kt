package net.kotlinx.awscdk.component

import net.kotlinx.awscdk.CdkEnum
import net.kotlinx.awscdk.toCdk
import net.kotlinx.awscdk.util.TagUtil
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Duration
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.s3.*

/**
 * 디폴트로 자동 암호화임
 * */
class CdkS3 : CdkEnum {

    @Kdsl
    constructor(name: String, block: CdkS3.() -> Unit = {}) {
        apply(block).apply {
            this.name = name
        }
    }

    /** 버킷 명 */
    lateinit var name: String

    /** 도메인 등은 이름 그대로를 버킷명으로 사용함 */
    var domain: Boolean = false

    override val logicalName: String
        get() = if (domain) name else "${project.projectName}-${name}-${deploymentType.name.lowercase()}"

    lateinit var iBucket: IBucket

    var lifecycleRules: List<LifecycleRule> = mutableListOf()

    var removalPolicy = RemovalPolicy.RETAIN

    var versioned: Boolean = false

    /** 이거 설정하면 자동 s3:GetObject 권한 부여해줌 */
    var publicReadAccess: Boolean = false

    /** 2023 이후 설정 적용. (AWS에서는 권장하지 않음) */
    var publicAll: Boolean = false

    /** static 이미지등을 링크 걸어서 사용할거면 필수 */
    var cors: List<CorsRule>? = null

    /** 웹호스팅-리다이렉트 (딴데로 넘김) */
    var websiteRedirect: String? = null

    /** 웹호스팅-문서 (S3를 기반으로 호스팅) */
    var websiteIndexDocument: String? = null

    fun create(stack: Stack, block: BucketProps.Builder.() -> Unit = {}): CdkS3 {
        val bucketProps = BucketProps.builder()
            .bucketName(this.logicalName)
            .removalPolicy(removalPolicy)
            .lifecycleRules(lifecycleRules)
            .versioned(versioned)
            .publicReadAccess(publicReadAccess)
            .cors(cors)
            .websiteIndexDocument(websiteIndexDocument)
            .apply {
                websiteRedirect?.let {
                    //리다이렉트는 별도의 객체 권한 필요없음
                    websiteRedirect(RedirectTarget.builder().hostName(it).build())
                }
                if (publicAll) {
                    //과거 publicReadAccess 와 동일설정.
                    //publicReadAccess 대체함. Bucket policy가 자동으로 생김
                    //2023년에 AWS 정책 변경. 이거 없으면 다음의 오류가 발생함  API: s3:PutBucketPolicy Access Denied
                    blockPublicAccess(
                        BlockPublicAccess(
                            BlockPublicAccessOptions.builder()
                                .blockPublicAcls(false)
                                .ignorePublicAcls(false)
                                .blockPublicPolicy(false)
                                .restrictPublicBuckets(false)
                                .build()
                        )
                    )
                }
            }
            .apply(block)
            .build()

        val id = "${logicalName}${if (domain) "-bucket" else ""}" //혹시나 도메인 그대로 쓰면 겹칠까봐 변경해줌
        iBucket = Bucket(stack, id, bucketProps)
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
        lifecycleRules += LifecycleRule.builder().id("${project.projectName}-lifecycle-$name-${deploymentType.name.lowercase()}")
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
        lifecycleRules += LifecycleRule.builder().id("${project.projectName}-lifecycle-$name-${deploymentType.name.lowercase()}")
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