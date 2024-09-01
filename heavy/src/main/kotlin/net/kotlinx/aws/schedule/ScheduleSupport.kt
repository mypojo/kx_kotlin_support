package net.kotlinx.aws.schedule

import aws.sdk.kotlin.services.scheduler.SchedulerClient
import aws.sdk.kotlin.services.scheduler.getSchedule
import aws.sdk.kotlin.services.scheduler.listSchedules
import aws.sdk.kotlin.services.scheduler.model.GetScheduleResponse
import net.kotlinx.collection.doUntilTokenNull
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.string.toTextGridPrint


/**
 * 전체 스케쥴 리턴
 * */
suspend fun SchedulerClient.listAllScheduleDetails(): List<GetScheduleResponse> {
    val summaryList = doUntilTokenNull { _, token ->
        val response = this.listSchedules {
            this.nextToken = token as String?
        }
        response.schedules to response.nextToken
    }.flatten()

    return summaryList.map {
        suspend {
            this.getSchedule {
                this.groupName = it.groupName
                this.name = it.name
            }
        }
    }.coroutineExecute(8)
}

fun List<GetScheduleResponse>.printSimple() {
    listOf("groupName", "name", "scheduleExpression", "state", "target").toTextGridPrint {
        this.map {
            arrayOf(it.groupName, it.name, it.scheduleExpression, it.state, it.target!!.arn.substringAfter("function:"))
        }
    }
}


