package net.kotlinx.aws1.batchStep

import net.kotlinx.core1.counter.EventTimeChecker


class BatchStepRuntimeConfig {
    lateinit var batchStepRuntime: BatchStepRuntime

    var desc:String = ""

    /**
     * 크롤링시 WAF를 회피하려면 실패 오류를 내면 안된다 (실패도 카운팅에 잡힌다) -> 딜레이를 줘서 실패를 안나게 하는게 더 빠름
     * 동일한 IP에서의 연속 호출을 막아준다
     *  */
    var eventTimeChecker: EventTimeChecker = EventTimeChecker()
}