package net.kotlinx.awscdk.component

import net.kotlinx.awscdk.util.Route53Util
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.certificatemanager.Certificate
import software.amazon.awscdk.services.route53.IHostedZone


/**
 * 간단한 스태틱 호스팅 프로젝트 구성용
 * 버킷과 클라우드프론트를 한번에 만들어준다.
 *
 * 1. 북미서버에 인증서가 준비되어있어야 한다
 * 2. zone에 도메인이 등록되어 있어야 한다.
 * */
class CdkStaticHost {

    @Kdsl
    constructor(block: CdkStaticHost.() -> Unit = {}) {
        apply(block)
    }

    /**
     * 루트 도메인
     * ex) HostedZoneUtil.load(stack, "kotlinx.net")
     *  */
    lateinit var hostedZone: IHostedZone

    /**
     * 호스팅에 사용할 도메인
     * ex) www.kotlinx.net
     *  */
    lateinit var hostDomain: String

    /** 루트 접속시 사용할 주소 */
    var websiteIndexDocument: String = "index.html"

    /**
     * 북미 인증서 ARN
     * 보통 서버 생성은 로컬리즌이지만 인증서는 북미에 있기때문에  SSM 작동아 안된다.
     * 미리 로드해서 스택을 실행하거나 하드코딩 할것
     * */
    lateinit var certArn: String

    /**
     * S3 오리진의 이름.
     * CloudFront 사용시 꼭 버킷이름이 도메인과 일치할 필요는 없음. 다만 일치하면 이쁘니깐 권장.
     * hostDomain S3버킷명이 이미 선점되었을경우 이걸 사용해서 대체할것
     *  */
    var hostS3Name: String? = null

    fun create(stack: Stack) {
        val s3Name = hostS3Name ?: hostDomain
        val origonBucket = CdkS3(s3Name).apply {
            domain = true
            removalPolicy = RemovalPolicy.DESTROY
            publicReadAccess = true
            publicAll = true
            websiteIndexDocument = this@CdkStaticHost.websiteIndexDocument
            create(stack)
        }

        val cloudFront = CdkCloudFront {
            domain = hostDomain
            iBucket = origonBucket.iBucket
            iCertificate = Certificate.fromCertificateArn(stack, "hosting-${hostDomain}", certArn)
            create(stack)
        }
        Route53Util.arecord(stack, hostedZone, hostDomain, cloudFront.toRecordTarget())

    }
}

