package net.kotlinx.aws.schedule

import aws.sdk.kotlin.services.scheduler.*
import aws.sdk.kotlin.services.scheduler.model.*
import aws.sdk.kotlin.services.scheduler.model.Target
import aws.sdk.kotlin.services.scheduler.paginators.listSchedulesPaginated
import aws.smithy.kotlin.runtime.time.toSdkInstant
import kotlinx.coroutines.flow.toList
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.awsConfig
import net.kotlinx.aws.regist
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.TimeUtil

val AwsClient.schedule: SchedulerClient
    get() = getOrCreateClient { SchedulerClient { awsConfig.build(this) }.regist(awsConfig) }

/** 전체 스케쥴 리턴 */
suspend fun SchedulerClient.listAllScheduleDetails(groupName: String? = null): List<GetScheduleResponse> {
    val summaryList = this.listSchedulesPaginated { this.groupName = groupName }.toList().map { it.schedules }.flatten()
    return summaryList.map { suspend { getSchedule(it.groupName!!, it.name!!) } }.coroutineExecute(8)
}

/** 단일 스케쥴 리턴 */
@Throws(ResourceNotFoundException::class)
suspend fun SchedulerClient.getSchedule(groupName: String, name: String): GetScheduleResponse = this.getSchedule {
    this.groupName = groupName
    this.name = name
}

/** 간단 출력 */
fun List<GetScheduleResponse>.printSimple() {
    listOf("groupName", "name", "scheduleExpression", "state", "target").toTextGridPrint {
        this.map {
            arrayOf(it.groupName, it.name, it.scheduleExpression, it.state, it.target!!.arn.substringAfter("function:"))
        }
    }
}

/** 스케쥴 삭제 */
suspend fun SchedulerClient.deleteSchedule(groupName: String, name: String) = this.deleteSchedule {
    this.groupName
    this.name
}

/** 스케쥴 수정 or 생성 */
suspend fun SchedulerClient.updateOrCreateSchedule(schedule: SchedulerForLambda) {
    try {
        this.updateSchedule(schedule)
    } catch (_: ResourceNotFoundException) {
        this.createSchedule(schedule)
    }
}

/** 그룹 생성 */
suspend fun SchedulerClient.createScheduleGroup(groupName: String, tagMap: Map<String, String>) {
    this.createScheduleGroup {
        name = groupName
        // 태그를 추가할 수도 있습니다.
        tags = tagMap.map { e ->
            Tag {
                key = e.key
                value = e.value
            }
        }
    }
}

/** 간단 스캐쥴 생성 */
suspend fun SchedulerClient.createSchedule(schedule: SchedulerForLambda) {
    val config = this.awsConfig
    this.createSchedule {
        groupName = schedule.groupName
        name = schedule.name
        state = schedule.state
        description = schedule.description
        scheduleExpression = "cron(${schedule.cron})"
        scheduleExpressionTimezone = "Asia/Seoul"
        actionAfterCompletion = schedule.after
        flexibleTimeWindow = FlexibleTimeWindow {
            mode = FlexibleTimeWindowMode.Off
        }
        startDate = schedule?.startTime?.atZone(TimeUtil.SEOUL)?.toInstant()?.toSdkInstant()
        endDate = schedule?.endTime?.atZone(TimeUtil.SEOUL)?.toInstant()?.toSdkInstant()
        target = Target {
            arn = "arn:aws:lambda:${config.region}:${config.awsId}:function:${schedule.lambdaName}"
            roleArn = "arn:aws:iam::${config.awsId}:role/${schedule.lambdaRole}"
            deadLetterConfig = DeadLetterConfig {
                arn = "arn:aws:sqs:${config.region}:${config.awsId}:${schedule.dlq}"
            }
            schedule.payload?.let { input = it.toString() }
        }
    }
}

/** 간단 스캐쥴 수정 */
suspend fun SchedulerClient.updateSchedule(schedule: SchedulerForLambda) {
    val config = this.awsConfig
    this.updateSchedule {
        groupName = schedule.groupName
        name = schedule.name
        state = schedule.state
        description = schedule.description
        scheduleExpression = "cron(${schedule.cron})"
        scheduleExpressionTimezone = "Asia/Seoul"
        actionAfterCompletion = schedule.after
        flexibleTimeWindow = FlexibleTimeWindow {
            mode = FlexibleTimeWindowMode.Off
        }
        startDate = schedule?.startTime?.atZone(TimeUtil.SEOUL)?.toInstant()?.toSdkInstant()
        endDate = schedule?.endTime?.atZone(TimeUtil.SEOUL)?.toInstant()?.toSdkInstant()
        target = Target {
            arn = "arn:aws:lambda:${config.region}:${config.awsId}:function:${schedule.lambdaName}"
            roleArn = "arn:aws:iam::${config.awsId}:role/${schedule.lambdaRole}"
            deadLetterConfig = DeadLetterConfig {
                arn = "arn:aws:sqs:${config.region}:${config.awsId}:${schedule.dlq}"
            }
            schedule.payload?.let { input = it.toString() }
        }
    }
}


