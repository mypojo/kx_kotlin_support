package net.kotlinx.aws.schedule

import aws.sdk.kotlin.services.scheduler.model.ActionAfterCompletion
import aws.sdk.kotlin.services.scheduler.model.ScheduleState
import net.kotlinx.json.gson.GsonData
import java.time.LocalDateTime

/**
 * 람다 스케쥴 정보
 * 여기서 추가로 시작 & 종료 날짜 지정가능
 *
 * ex) 2025년 1월 ~ 8월 사이 매주 목금,  오전 3,7시에 작동
 * */
data class SchedulerLambda(
    val groupName: String,
    /** 최대 64자까지 가능함 */
    val name: String,
    val cron: CronExpression,
    val lambdaName: String,
    val lambdaRole: String,
    /** 가능하면 무조건 넣을것! */
    val dlq: String,

    /** 디스크립션 */
    val description: String? = null,

    /** 시작시간 */
    val startTime: LocalDateTime? = null,

    /** 종료시간 */
    val endTime: LocalDateTime? = null,

    val state: ScheduleState = ScheduleState.Enabled,
    /**
     * 더이상 트리거가 불가능할시 어떻게 할지?
     * ex) 종료 날짜가 지났거나 1회성 날짜 지정시
     *  */
    val after: ActionAfterCompletion = ActionAfterCompletion.None,
    /**
     * 람다 페이로드.
     * 이거 지정시  이벤트브릿지 스타일 json은 완전히 무시되고 교체된 페이로드가 람다로 그대로 전달됨
     * */
    val payload: GsonData? = null,
)