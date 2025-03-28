@file:Suppress("DEPRECATION")

package net.kotlinx.awscdk.channel

import net.kotlinx.aws.AwsConfig
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.json.gson.GsonSet
import net.kotlinx.system.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.events.IRuleTarget
import software.amazon.awscdk.services.events.Rule
import software.amazon.awscdk.services.events.RuleProps


/**
 * 각종 스케쥴링 설정
 * 주의!! 이거 대신 스케쥴러 사용할것
 * */
@Deprecated("신제품 출시로 더이상 사용하지 않음")
class CdkEventBridgeSchedule(
    val project: AwsConfig,
    val deploymentType: DeploymentType,
    val stack: Stack,
    /** 해당 시케줄에 트리거 할 대상 (람다 등..) */
    val ruleTarget: IRuleTarget,
    block: CdkEventBridgeSchedule.() -> Unit = {}
) {

    init {
        block(this)
    }

    /** 스케쥴을 등록함. 가능하면 인라인 가능하도록 구성 */
    fun addSchedule(jobName: String, enabled: Boolean, block: CronKrOptions.() -> Unit = {}): Rule {
        val options = CronKrOptions().apply(block)
        val ruleName = "${project.profileName}-${jobName}-${this.deploymentType}"
        val comment = GsonSet.GSON.toJson(options) //변환 전으로 해야함
        val rule = Rule(
            this.stack, ruleName, RuleProps.builder()
                .enabled(enabled)
                .ruleName(ruleName)
                .description(comment)
                .schedule(options.updateToUtc().toSchedule())
                .build()
        )
        rule.addTarget(this.ruleTarget)
        TagUtil.tagDefault(rule)
        return rule
    }

    companion object {
        /** 업무시간 : 오전 8시 00분 ~ 오후 7시 59분  */
        const val HOUR_WORK: String = "23,00-10"

    }


}