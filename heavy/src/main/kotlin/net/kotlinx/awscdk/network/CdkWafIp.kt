package net.kotlinx.awscdk.network

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.wafv2.CfnIPSet
import software.amazon.awscdk.services.wafv2.CfnIPSetProps
import software.amazon.awscdk.services.wafv2.CfnWebACL

/**
 * WAF(Web ACL) 에서 IP 화이트리스트 기반 허용 룰을 생성하는 구성요소
 * - IPv4 / IPv6 를 각각 IPSet 으로 생성한 뒤, 해당 IPSet 참조 룰을 반환한다.
 * - 실제 WebACL 생성(CdkWaf)은 분리되어 있으며, 여기서 생성한 rules 를 주입하면 된다.
 */
class CdkWafIp :CdkInterface{

    @Kdsl
    constructor(block: CdkWafIp.() -> Unit = {}) {
        apply(block)
    }

    override val logicalName: String
        get() = "waf_rule-${projectName}-${name}-${suff}"

    /** 이름 */
    lateinit var name: String

    /** 허용할 IP 화이트리스트 (CIDR 표기) */
    var ips: List<String> = emptyList()

    /** 실행할 액션 */
    var action: CfnWebACL.AllowActionProperty = CfnWebACL.AllowActionProperty.builder().build()

    /** 결과 */
    lateinit var rules: List<CfnWebACL.RuleProperty>

    /**
     * IP 화이트리스트 기반 룰을 생성해서 반환한다.
     * @param stack 리소스를 생성할 스택
     */
    fun create(stack: Stack) {

        // IPv4 / IPv6 분리
        val (ipv4, ipv6) = ips.partition { it.contains(":").not() }

        val ipSetArns = buildList {
            if (ipv4.isNotEmpty()) {
                val ipSetV4 = CfnIPSet(
                    stack,
                    "${logicalName}_ipset_v4",
                    CfnIPSetProps.builder()
                        .name("${logicalName}-ipv4")
                        .description("IPv4 list")
                        .scope("CLOUDFRONT")
                        .addresses(ipv4)
                        .ipAddressVersion("IPV4")
                        .build()
                )
                add(ipSetV4.attrArn)
            }
            if (ipv6.isNotEmpty()) {
                val ipSetV6 = CfnIPSet(
                    stack,
                    "${logicalName}_ipset_v6",
                    CfnIPSetProps.builder()
                        .name("${logicalName}-ipv6")
                        .description("IPv6 list")
                        .scope("CLOUDFRONT")
                        .addresses(ipv6)
                        .ipAddressVersion("IPV6")
                        .build()
                )
                add(ipSetV6.attrArn)
            }
        }

        rules = ipSetArns.mapIndexed { idx, arn ->
            CfnWebACL.RuleProperty.builder()
                .name("${logicalName}-${idx}")
                .priority(idx)
                .action(CfnWebACL.RuleActionProperty.builder().allow(action).build())
                .statement(CfnWebACL.StatementProperty.builder().ipSetReferenceStatement(CfnWebACL.IPSetReferenceStatementProperty.builder().arn(arn).build()).build())
                .visibilityConfig(
                    CfnWebACL.VisibilityConfigProperty.builder()
                        .sampledRequestsEnabled(true)
                        .cloudWatchMetricsEnabled(true)
                        .metricName("${logicalName}-allow-ipset-${idx}")
                        .build()
                )
                .build()
        }
    }

}
