package net.kotlinx.awscdk.network

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.certificatemanager.Certificate
import software.amazon.awscdk.services.certificatemanager.CertificateProps
import software.amazon.awscdk.services.certificatemanager.CertificateValidation

/**
 * https://ap-northeast-2.console.aws.amazon.com/acm/home?region=ap-northeast-2#/certificates/list
 * */
object AcmUtil {

    /**
     * 도메인 인증서 등록
     * 도메인 소유 여부를 판단하기 위해 DNS로 인증한다 -> 등록 완료에 시간이 걸리니 잠시 대기
     * 경고!! 클라우드포른트의 경우 북미 1서버에 등록 해야함.
     * @param domain ex) "kotlinx.net"
     * @param certDomain ex) "*.kotlinx.net"
     *  */
    fun regCertificate(stack: Stack, domain: String, certDomain: String): Certificate {
        val zone = HostedZoneUtil.load(stack, domain)
        val id = certDomain.replace("*", "all") //id는 특문 안됨
        return Certificate(
            stack, id, CertificateProps.builder()
                .domainName(certDomain)
                //.certificateName(certificateName)
                .validation(CertificateValidation.fromDns(zone))
                .build()
        )
    }

}