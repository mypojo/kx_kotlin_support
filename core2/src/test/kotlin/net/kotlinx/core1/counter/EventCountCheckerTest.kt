package net.kotlinx.core1.counter

import org.junit.jupiter.api.Test

internal class EventCountCheckerTest {

    @Test
    fun 기본테스트() {

        var eventCnt = 0
        val counter = EventCountChecker(3)

        repeat(10) { cnt ->
            counter.check {
                println("이벤트 $it")
                eventCnt++
            }
        }
        check(eventCnt == 3)

    }
}