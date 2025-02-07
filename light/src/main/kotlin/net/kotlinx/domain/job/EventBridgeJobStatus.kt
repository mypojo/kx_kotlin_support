package net.kotlinx.domain.job

import net.kotlinx.aws.lambda.dispatch.asynch.EventBridge
import net.kotlinx.aws.lambda.dispatch.asynch.EventBridgeJson

/**
 * 잡 이벤트브릿지 - 상태변경
 *  */
data class EventBridgeJobStatus(private val event: EventBridgeJson) : EventBridge by event {

    val job: Job by lazy { detail.fromJson<Job>() }

    companion object {
        const val DETAIL_TYPE = "Job Status Change"
    }
}