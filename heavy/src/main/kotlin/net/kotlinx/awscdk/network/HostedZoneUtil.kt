package net.kotlinx.awscdk.network

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.route53.HostedZone
import software.amazon.awscdk.services.route53.HostedZoneProviderProps
import software.amazon.awscdk.services.route53.IHostedZone

object HostedZoneUtil {

    private val cache: MutableMap<String, IHostedZone> = mutableMapOf()

    /** 간단 로드 */
    fun load(stack: Stack, domain: String): IHostedZone {
        val zone = try {
            HostedZone.fromLookup(
                stack, "hostedZone-${domain}", HostedZoneProviderProps.builder()
                    .domainName(domain)
                    .build()
            )
        } catch (e: Exception) {
            cache[domain]!!
        }
        cache[domain] = zone
        return zone
    }

}