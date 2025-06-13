package net.kotlinx.awscdk.channel

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.scheduler.CfnSchedule
import software.amazon.awscdk.services.scheduler.CfnScheduleGroup
import software.amazon.awscdk.services.scheduler.CfnScheduleGroupProps
import software.amazon.awscdk.services.scheduler.CfnScheduleProps
import software.amazon.awscdk.services.sqs.IQueue

/**
 * 각종 스케쥴링 설정
 * */
class CdkSchedulerGroup : CdkInterface {

    @Kdsl
    constructor(block: CdkSchedulerGroup.() -> Unit = {}) {
        apply(block)
    }

    lateinit var stack: Stack

    /** 그룹명 */
    lateinit var groupName: String

    /** 디폴트로 서울 */
    var timezone = "Asia/Seoul"

    /** 너무 길지않게 조정했음 */
    override val logicalName: String
        get() = "${projectName}-${groupName}-${suff}"

    /** 결과 */
    lateinit var scheduleGroup: CfnScheduleGroup

    /** 대상. ex) 람다.. */
    lateinit var targetArn: String

    /** 역할 */
    lateinit var role: IRole

    /** dlq */
    lateinit var dlq: IQueue

    /**
     * 기본으로 리트라이 안함!
     * 참고로 스케줄링, 람다 등에서 리트라이 설정 가능 ->  각각 3번씩 리트라이하는 경우 최대 9번 리트라이됨
     *  */
    var retryCnt: Int = 0

    fun create(): CdkSchedulerGroup {

        scheduleGroup = CfnScheduleGroup(stack, "scheduler_group-${logicalName}", CfnScheduleGroupProps.builder().name(logicalName).build())
        TagUtil.tagDefault(scheduleGroup)
        return this
    }

    class ScheduleData {

        @Kdsl
        constructor(block: ScheduleData.() -> Unit = {}) {
            apply(block)
        }

        /** 스케쥴명 */
        lateinit var name: String

        /** 스케쥴 설명 (한글 지원됨)  */
        var description: String = ""

        /**
         * 한국 시간으로 입력하면됨
         * 분/시/일/월/요일/년
         * ex) 55 07 ? * 2-6 *  => 평일(월~금) 7시 55분
         *
         * 주의!! 2가 월요일이다!!!
         * 주의!! 요일을 지정할경우 날짜에는 알 수 없다는 의미인 ? 를 지정해야 한다
         * */
        lateinit var cronExpression: String

        /** ON 여부 */
        var enabled: Boolean = true

        /**
         * 입력 JSON
         * 이게 없으면 디폴트(이벤트브릿지하고 비슷한 스키마)로 변경됨
         * */
        var inputJson: Any? = null

        /**
         * 이거 설정시 시작시점 + 이구간 이내에서 실행됨
         * ex) 15분의 유연한 시간 창을 구성하면 예약된 시간 이후 15분 이내에 대상이 호출
         * 15분~30분 정도가 적당한듯
         *  */
        var flexibleTimeWindowMin: Number? = null

    }

    /** 실제 스케쥴 등록 */
    fun schedule(block: ScheduleData.() -> Unit) {
        val data = ScheduleData(block)
        CfnSchedule(
            stack, "CfnSchedule-${data.name}-${suff}", CfnScheduleProps.builder()
                .name("${data.name}-${suff}") //그룹화 되었음으로 프로젝트 접두어를 더이상 쓸 필요가 없다 -> 콘솔로는 만들어지는데 CDK로는 안됨. 그냥 접미어 붙여줌
                .description(data.description)
                .flexibleTimeWindow(CfnSchedule.FlexibleTimeWindowProperty.builder().mode("OFF").build()) //왜있는지 모를 기능.
                .groupName(scheduleGroup.name)
                .target(
                    CfnSchedule.TargetProperty.builder()
                        .arn(targetArn)
                        .roleArn(role.roleArn)
                        .deadLetterConfig(CfnSchedule.DeadLetterConfigProperty.builder().arn(dlq.queueArn).build())
                        .retryPolicy(
                            //디폴트로  1일 185 times 이 설정되기 때문에 0으로 변경한다.
                            CfnSchedule.RetryPolicyProperty.builder()
                                .maximumRetryAttempts(retryCnt)
                                .build()
                        )
                        .apply {
                            data.inputJson?.let { input(it.toString()) }
                        }
                        .build()
                )
                .scheduleExpression("cron(${data.cronExpression})")
                .scheduleExpressionTimezone(timezone)
                .state(if (data.enabled) "ENABLED" else "DISABLED")
                .apply {
                    data.flexibleTimeWindowMin?.let {
                        flexibleTimeWindow(CfnSchedule.FlexibleTimeWindowProperty.builder().mode("FLEXIBLE").maximumWindowInMinutes(it).build())
                    }
                }
                .build()
        )
    }


}