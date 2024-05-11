package net.kotlinx.counter

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.number.toLocalDateTime
import net.kotlinx.time.toKr01
import net.kotlinx.time.toTimeString
import kotlin.time.Duration.Companion.seconds

class EventTimeCheckerTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("EventTimeChecker") {
            Then("x초에 한번만 실행 -> x회 반복 체크 -> x회 호출") {
                val eventTimeChecker = EventTimeChecker(2.seconds)

                var exeCnt = 0
                repeat(20) { cnt ->
                    val result = eventTimeChecker.check()
                    if (result.ok) {
                        exeCnt++
                        log.info { "$cnt -> 성공 $exeCnt : ${result.now.toLocalDateTime().toKr01()}" }
                    } else {
                        log.trace { "$cnt -> 남은시간 ${result.next.toTimeString()}" }
                    }
                    Thread.sleep(300)
                }
                exeCnt shouldBe 3
            }
        }
    }


}