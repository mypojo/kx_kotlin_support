package net.kotlinx.awscdk.util

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.route53.HostedZone
import software.amazon.awscdk.services.route53.HostedZoneProviderProps
import software.amazon.awscdk.services.route53.IHostedZone

object HostedZoneUtil {

    /** 간단 로드 */
    fun load(stack: Stack, domain: String): IHostedZone {
        return HostedZone.fromLookup(
            stack, domain, HostedZoneProviderProps.builder()
                .domainName(domain)
                .build()
        )
    }

}