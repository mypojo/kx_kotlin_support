package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.DeploymentType
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core2.gson.GsonSet
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.events.*

/** 한국시간 입력기 */
data class CronKrOptions(
    /** 한국 날짜 등록 */
    val krDay: Int? = null,
    /** 한국시간 등록 */
    val krHour: Int? = null,

    var year: String? = null,
    var month: String? = null,
    var weekDay: String? = null,
    var hour: String? = null,
    var day: String? = null,
    var minute: String? = null,
) {
    //{krDay: 2, krHour: 3, minute: '00'}
    //{krHour: 9, minute: '30'}
    //{minute: '15/30'}
    //{hour:"23,00-10",minute: '15/30'}

    fun toSchedule(): Schedule {
        return Schedule.cron(
            CronOptions.builder()
                .year(year)
                .month(month)
                .weekDay(weekDay)
                .hour(hour)
                .day(day)
                .minute(minute)
                .build()
        )
    }

    /** 한국 시간을 UTC로 변경해준다. */
    fun toUtc(offsetHour: Int = OFFSET_HOUR): CronKrOptions {
        if (krHour == null) return this

        val hourKr = krHour - offsetHour
        val isYesterday = hourKr < 0;
        hour = "${hourKr + (if (isYesterday) 24 else 0)}" //시간은 24시간 더하고
        day = krDay?.let { "${it + (if (isYesterday) -1 else 0)}" } ?: null //날짜는 하루 당겨줘야함
        return this
    }

    companion object {
        /** 시차 */
        const val OFFSET_HOUR: Int = 9
    }
}


/**
 * 각종 스케쥴링 설정
 * */
class CdkEventBridgeSchedule(
    val project: CdkProject,
    val deploymentType: DeploymentType,
    val stack: Stack,
    /** 해당 시케줄에 트리거 할 대상 (람다 등..) */
    val ruleTarget: IRuleTarget,
) {

    /** 스케쥴을 등록함 */
    fun addSchedule(jobName: String, enabled: Boolean, config: CronKrOptions): Rule {
        val ruleName = "${project.projectName}-${jobName}-${this.deploymentType}"

        val comment = GsonSet.TABLE_UTC.toJson(config) //변환 전으로 해야함
        val rule = Rule(
            this.stack, ruleName, RuleProps.builder()
                .enabled(enabled)
                .ruleName(ruleName)
                .description(comment)
                .schedule(config.toUtc().toSchedule())
                .build()
        )
        rule.addTarget(this.ruleTarget);
        TagUtil.tag(rule, deploymentType);
        return rule
    }

    companion object {
        /** 업무시간 : 오전 8시 00분 ~ 오후 7시 59분  */
        const val HOUR_WORK: String = "23,00-10"

    }


}