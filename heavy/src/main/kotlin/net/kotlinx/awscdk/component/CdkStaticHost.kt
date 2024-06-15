package net.kotlinx.awscdk.component

import net.kotlinx.awscdk.util.HostedZoneUtil
import net.kotlinx.awscdk.util.Route53Util
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.certificatemanager.Certificate


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
     * ex) kotlinx.net
     *  */
    lateinit var rootDomain: String

    /**
     * 호스팅에 사용할 도메인
     * ex) www.kotlinx.net
     *  */
    lateinit var hostDomain: String

    /** 루트 접속시 사용할 주소 */
    var websiteIndexDocument: String = "index.html"

    /**
     * 북미 인증서 ARN
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

        val zone = HostedZoneUtil.load(stack, rootDomain)
        Route53Util.arecord(stack, zone, hostDomain, cloudFront.toRecordTarget())

    }
}

