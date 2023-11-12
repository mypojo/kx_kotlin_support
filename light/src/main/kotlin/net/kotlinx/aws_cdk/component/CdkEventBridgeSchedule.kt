package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core.DeploymentType
import net.kotlinx.core.gson.GsonSet
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.events.IRuleTarget
import software.amazon.awscdk.services.events.Rule
import software.amazon.awscdk.services.events.RuleProps


/**
 * 각종 스케쥴링 설정
 * 주의!! 이거 대신 스케쥴러 사용할것
 * */
class CdkEventBridgeSchedule(
    val project: CdkProject,
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
        val ruleName = "${project.projectName}-${jobName}-${this.deploymentType}"
        val comment = GsonSet.TABLE_UTC.toJson(options) //변환 전으로 해야함
        val rule = Rule(
            this.stack, ruleName, RuleProps.builder()
                .enabled(enabled)
                .ruleName(ruleName)
                .description(comment)
                .schedule(options.updateToUtc().toSchedule())
                .build()
        )
        rule.addTarget(this.ruleTarget)
        TagUtil.tag(rule, deploymentType)
        return rule
    }

    companion object {
        /** 업무시간 : 오전 8시 00분 ~ 오후 7시 59분  */
        const val HOUR_WORK: String = "23,00-10"

    }


}