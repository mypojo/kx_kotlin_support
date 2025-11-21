package net.kotlinx.awscdk.network

import mu.KotlinLogging
import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.certificatemanager.ICertificate
import software.amazon.awscdk.services.cloudfront.*
import software.amazon.awscdk.services.cloudfront.origins.S3BucketOrigin
import software.amazon.awscdk.services.cloudfront.origins.S3StaticWebsiteOrigin
import software.amazon.awscdk.services.s3.IBucket

/** 단일사이트 클라우드프론트 샘플 */
class CdkCloudFront : CdkInterface {

    @Kdsl
    constructor(block: CdkCloudFront.() -> Unit = {}) {
        apply(block)
    }

    lateinit var domain: String
    lateinit var origin: IOrigin
    lateinit var iCertificate: ICertificate

    /** 외부에서 생성된 WAF WebACL ARN. 설정되면 CloudFront에 연결됨 */
    var webAclArn: String? = null

    override val logicalName: String
        get() = "${domain}_cloudfront"

    /**
     * 적절하기 조합하기
     * 리엑트의 경우 404는 본문 페이지로 이동후 리엑트 라우팅을 트리거 시킬것!
     * IP 차단등이 들어가있다면 403도 오버라이딩 해야함 ex)
     *  */
    var errorResps = listOf(errorReact())


    /** 결과 */
    lateinit var distribution: Distribution

    fun create(stack: Stack, block: DistributionProps.Builder.() -> Unit = {}) {
        val builder = DistributionProps.builder()
            .defaultBehavior(
                BehaviorOptions.builder()
                    .origin(origin)
                    .build()
            )
            .comment(logicalName)
            .domainNames(listOf(domain))  //해당 도메인에 DNS 설정이 있으면 안된다. CDN 생성 후 DNS를 생성할것.
            .certificate(iCertificate)
            .errorResponses(errorResps)

        webAclArn?.let { builder.webAclId(it) }

        distribution = Distribution(
            stack,
            logicalName,
            builder
                .apply(block)
                .build()
        )
    }

    companion object {

        private val log = KotlinLogging.logger {}

        /**
         * 이미지 링크 등의 CDN용
         * 이렇게 하면 S3에 스태틱 호스팅 설정은 되지 않는다
         *  */
        fun forCdn(iBucket: IBucket): IOrigin = S3BucketOrigin.withOriginAccessControl(iBucket)

        /**
         * 서버리스 웹사이트용
         * S3에 스태틱 호스팅 설정됨
         *  */
        fun forWebsite(iBucket: IBucket): IOrigin = S3StaticWebsiteOrigin(iBucket)

        //==================================================== 에러처리 시리즈 ======================================================

        fun errorReact(path: String = "/index.html") = ErrorResponse.builder()
            .httpStatus(404)
            .responseHttpStatus(200)
            .responsePagePath(path)
            .build()!!

    }

}