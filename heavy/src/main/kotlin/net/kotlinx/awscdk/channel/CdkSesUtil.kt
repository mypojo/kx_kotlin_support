package net.kotlinx.awscdk.channel

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.route53.IHostedZone
import software.amazon.awscdk.services.route53.PublicHostedZone
import software.amazon.awscdk.services.route53.PublicHostedZoneAttributes
import software.amazon.awscdk.services.ses.EmailIdentity
import software.amazon.awscdk.services.ses.Identity

object CdkSesUtil {

    /**
     * 코드 참고용
     * 이메일 도메인 인증 생성 (DKIM 레코드 등이 Route 53에 자동 추가되서 인증됨)
     * */
    fun create(stack: Stack, lookedUpZone: IHostedZone): EmailIdentity? {
        //변환을 한번 해줘야함
        val publicHostedZone = PublicHostedZone.fromPublicHostedZoneAttributes(
            stack, "PublicHostedZone",
            PublicHostedZoneAttributes.builder()
                .zoneName(lookedUpZone.zoneName)
                .hostedZoneId(lookedUpZone.hostedZoneId)
                .build()
        )
        return EmailIdentity.Builder.create(stack, "EmailIdentity")
            .identity(Identity.publicHostedZone(publicHostedZone))
            .build()
    }

}
