package net.kotlinx.kotest.modules.job

import com.google.common.eventbus.Subscribe
import mu.KotlinLogging
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

}
