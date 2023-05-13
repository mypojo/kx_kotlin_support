package net.kotlinx.core1.counter

import net.kotlinx.core1.number.toLocalDateTime
import net.kotlinx.core1.time.toKr01
import net.kotlinx.core1.time.toTimeString
import net.kotlinx.core2.test.TestLevel02
import net.kotlinx.core2.test.TestRoot

class EventTimeCheckerTest : TestRoot() {


    @TestLevel02
    fun test() {

        val eventTimeChecker = EventTimeChecker()

        (0..100).forEach {
            val result = eventTimeChecker.check()
            if (result.ok) {
                log.info { "성공 : ${result.now.toLocalDateTime().toKr01()}" }
            } else {
                log.debug { "try ${result.tryCnt} / 남은시간 ${result.next.toTimeString()}" }
            }

            Thread.sleep(500)
        }

    }

}