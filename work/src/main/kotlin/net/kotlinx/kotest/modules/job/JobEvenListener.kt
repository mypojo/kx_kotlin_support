package net.kotlinx.kotest.modules.job

import com.google.common.eventbus.Subscribe
import mu.KotlinLogging
import net.kotlinx.domain.job.EventBridgeJobStatus
import net.kotlinx.domain.job.JobFailEvent
import net.kotlinx.domain.job.JobSuccessEvent

/**
 * 잡 종료시 이벤트 처리기
 */
class JobEvenListener {

    private val log = KotlinLogging.logger {}

    /** 에러 or 경고 알람 */
    @Subscribe
    fun onFinish(event: JobFailEvent) {
        val job = event.job
        log.info { "job fail : ${job.toKeyString()}" }
    }

    /** 성공중에서 강제 호출인경우 알람 */
    @Subscribe
    fun onFinish(event: JobSuccessEvent) {
        val job = event.job
        log.info { "job success : ${job.toKeyString()}" }
    }

    /** 잡 상태변경 */
    @Subscribe
    fun onStatueChange(event: EventBridgeJobStatus) {
        val job = event.job
        log.warn { "잡 이벤트 변경!! [${job.toKeyString()}] => ${job.jobStatus}" }
    }

}
