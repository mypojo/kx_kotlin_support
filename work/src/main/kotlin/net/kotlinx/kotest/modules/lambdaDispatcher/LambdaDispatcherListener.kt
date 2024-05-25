package net.kotlinx.kotest.modules.lambdaDispatcher

import com.google.common.eventbus.Subscribe
import mu.KotlinLogging
import net.kotlinx.aws.lambda.dispatch.LambdaDispatcherDeadEvent
import net.kotlinx.aws.lambda.dispatch.asynch.SchedulerEvent

class LambdaDispatcherListener {

    private val log = KotlinLogging.logger {}

    /** 테스트 확인용 */
    val allEvents = mutableListOf<Any>()

    @Subscribe
    fun lambdaDispatcherDeadEvent(event: LambdaDispatcherDeadEvent) {
        log.warn { "LambdaDispatcherDeadEvent 수신 : $event" }
        allEvents += event
    }

    //==================================================== 개별 메세지들 ======================================================

    @Subscribe
    fun onEvent(event: SchedulerEvent) {
        log.info { " -> LambdaDispatcher 수신 : $event" }
        allEvents += event
    }

}