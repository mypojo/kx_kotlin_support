package net.kotlinx.core.time

import net.kotlinx.core.string.toLocalDate
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class TimeUtilTest : TestRoot() {

    @Test
    fun test() {

        val start = "20231201".toLocalDate()
        val end = "20231205".toLocalDate()

        check(start <= end) { "시작 날짜보다 종료 날짜가 더 커야합니다" }

        val list = mutableListOf<String>()
        var current = start
        while (true){
            list.add(current.toYmd())
            if(current >= end) break
            current = current.plusDays(1)
        }

        println(list)

    }


}