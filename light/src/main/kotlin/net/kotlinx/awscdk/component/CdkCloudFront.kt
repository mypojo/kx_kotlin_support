package net.kotlinx.awscdk.component

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.certificatemanager.ICertificate
import software.amazon.awscdk.services.cloudfront.BehaviorOptions
import software.amazon.awscdk.services.cloudfront.Distribution
import software.amazon.awscdk.services.cloudfront.DistributionProps
import software.amazon.awscdk.services.cloudfront.origins.S3Origin
import software.amazon.awscdk.services.route53.RecordTarget
import software.amazon.awscdk.services.route53.targets.CloudFrontTarget
import software.amazon.awscdk.services.s3.IBucket

/** 단일사이트 클라우드프론트 샘플 */
class CdkCloudFront : CdkInterface {

    @Kdsl
    constructor(block: CdkCloudFront.() -> Unit = {}) {
        apply(block)
    }

    lateinit var domain: String
    lateinit var iBucket: IBucket
    lateinit var iCertificate: ICertificate


    override val logicalName: String
        get() = "${domain}_cloudfront"

    /** 결과 */
    lateinit var distribution: Distribution

    fun create(stack: Stack, block: DistributionProps.Builder.() -> Unit = {}) {
        distribution = Distribution(
            stack, logicalName, DistributionProps.builder()
                .defaultBehavior(
                    BehaviorOptions.builder()
                        .origin(S3Origin(iBucket))
                        .build()
                )
                .comment(logicalName)
                .domainNames(listOf(domain))  //해당 도메인에 DNS 설정이 있으면 안된다. CDN 생성 후 DNS를 생성할것.
                .certificate(iCertificate)
                .apply(block)
                .build()
        )
    }

    /**
     * 도메인 연결용 레코드 정보 리턴
     * 도메인 연결은 외부에서 할 수 도 있기 때문에 분리\
     * ex) Route53Util.arecord(stack, it, domain, target)
     *  */
    fun toRecordTarget(): RecordTarget = RecordTarget.fromAlias(CloudFrontTarget(distribution))


}