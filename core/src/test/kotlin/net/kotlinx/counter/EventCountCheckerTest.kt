package net.kotlinx.counter

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

internal class EventCountCheckerTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("EventCountChecker") {
            Then("3회당 한번 이벤트 -> 10번 호출 -> 3번 이벤트 발생") {
                var eventCnt = 0
                val counter = EventCountChecker(3)

                repeat(10) { cnt ->
                    counter.check {
                        log.info { " => $cnt -> 이벤트 $it" }
                        eventCnt++
                    }
                }
                check(eventCnt == 3)
            }
        }
    }

}