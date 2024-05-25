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

    fun create(stack: Stack) {
        val origonBucket = CdkS3(hostDomain).apply {
            domain = true
            removalPolicy = RemovalPolicy.DESTROY
            publicReadAccess = true
            publicAll = true
            websiteIndexDocument = this@CdkStaticHost.websiteIndexDocument
            create(stack)
        }

        CdkCloudFront {
            domain = hostDomain
            iBucket = origonBucket.iBucket
            iCertificate = Certificate.fromCertificateArn(stack, "hosting-${hostDomain}", certArn)
            create(stack)

            val zone = HostedZoneUtil.load(stack, rootDomain)
            Route53Util.arecord(stack, zone, hostDomain, toRecordTarget())
        }


    }
}

