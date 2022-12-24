package net.kotlinx.core1.counter

import org.junit.jupiter.api.Test

internal class ActionCounterTest {

    @Test
    fun 기본테스트() {

        var eventCnt = 0
        val counter = ActionCounter(3) {
            println("이벤트 $it")
            eventCnt++
        }
        repeat(10) { counter.incrementAndGet() }
        println("eventCnt = $eventCnt")
        check(eventCnt == 3) { "이벤트 수는 3 이어야함" }

        println("Kotlin Version  : ${KotlinVersion.CURRENT} ")

        println("Java Version     : ${ System.getProperty("java.version") } ")
        println("Java VM Version  : ${ System.getProperty("java.vm.version") } ")
        println("Java VM Vendor   : ${ System.getProperty("java.vm.vendor") }  ")
        println("Java VM Name     : ${ System.getProperty("java.vm.name")  } ")
    }
}