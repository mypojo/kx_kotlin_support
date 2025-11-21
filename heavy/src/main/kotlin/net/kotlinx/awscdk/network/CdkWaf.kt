package net.kotlinx.awscdk.network

import mu.KotlinLogging
import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.core.Kdsl
import net.kotlinx.lazyLoad.default
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.wafv2.CfnWebACL
import software.amazon.awscdk.services.wafv2.CfnWebACLProps

/** CloudFront용 WAF(Web ACL) 생성을 담당하는 구성요소 */
class CdkWaf : CdkInterface {

    @Kdsl
    constructor(block: CdkWaf.() -> Unit = {}) {
        apply(block)
    }

    override val logicalName: String
        get() = "waf-${projectName}-${name}-${suff}"

    /** 이름 */
    lateinit var name: String

    /**
     * Web ACL 에 적용할 룰 목록
     * - 외부에서 구성해 주입한다. (예: CdkWafIp 를 통해 생성)
     */
    var rules: List<CfnWebACL.RuleProperty> = emptyList()

    var scope = "CLOUDFRONT"

    /** 디폴트로 전부 막음! 주의!! 이거설정하면 접근 안됨! */
    var defaultAction = CfnWebACL.DefaultActionProperty.builder().block(CfnWebACL.BlockActionProperty.builder().build()).build()!!

    /** 일단 끄자 */
    var visibilityConfig by default {
        CfnWebACL.VisibilityConfigProperty.builder()
            .sampledRequestsEnabled(false)
            .cloudWatchMetricsEnabled(false)
            .metricName(logicalName)
            .build()!!
    }

    /** 생성 결과 ARN */
    lateinit var webAcl: CfnWebACL

    fun create(stack: Stack) {
        webAcl = CfnWebACL(
            stack,
            logicalName,
            CfnWebACLProps.builder()
                .name(logicalName)
                .scope(scope)
                .defaultAction(defaultAction)
                .visibilityConfig(visibilityConfig)
                .rules(rules)
                .build()
        )
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
