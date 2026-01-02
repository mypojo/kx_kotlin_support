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

/** 단일사이트 클라우드프론트 샘플. */
class CdkCloudFront : CdkInterface {

    @Kdsl
    constructor(block: CdkCloudFront.() -> Unit = {}) {
        apply(block)
    }

    /**
     * api.xxx.com 등의 도메인
     * 여기로 cname 연결은 따로 해줘야 한다
     * */
    lateinit var domain: String

    /**
     * 디폴트 작동방식 설정
     * 오리진 세팅은 아래처럼!
     * ex) S3호스팅 -> CdkCloudFront.forWebsite(origon.iBucket)
     * ex) ALB -> LoadBalancerV2Origin(alb, LoadBalancerV2OriginProps.builder().protocolPolicy(OriginProtocolPolicy.HTTPS_ONLY).build()
     * */
    lateinit var defaultBehavior: BehaviorOptions

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
            .defaultBehavior(defaultBehavior)
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
         * 프론트엔드는 캐싱해야 함으로 기본설정 그대로 써도됨 샘플
         * @see defaultBehavior
         * */
        fun behaviorForFrontend(origin: IOrigin) = BehaviorOptions.builder().origin(origin).build()!!

        /**
         * API 서버는 캐싱 끄고, AllViewerAndCloudFrontHeaders 등을 설정 샘플
         * @see defaultBehavior
         *  */
        fun behaviorForBackend(origin: IOrigin) = BehaviorOptions.builder()
            .origin(origin)
            .cachePolicy(CachePolicy.CACHING_DISABLED) //캐시 꺼야함
            .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS) //https 만 사용
            .allowedMethods(AllowedMethods.ALLOW_ALL) //다 허용해야함
            .originRequestPolicy(OriginRequestPolicy.ALL_VIEWER_AND_CLOUDFRONT_2022) //원본 헤더 +@ 전송 (국가정보 등)
            .build()!!


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