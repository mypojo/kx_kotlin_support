package net.kotlinx.core2.calculator

import net.kotlinx.core1.string.toTextGrid
import net.kotlinx.core1.time.toKr01
import net.kotlinx.core2.test.TestLevel01
import net.kotlinx.core2.test.TestRoot
import java.time.LocalDateTime

class ProgressDataTest : TestRoot() {

    @TestLevel01
    fun test() {

        val now = LocalDateTime.now()
        val totalCount: Long = 100
        val data = ProgressData(totalCount, 13, now.minusMinutes(5))
        listOf(
            arrayOf(0, "0%", data.startTime!!.toKr01()),
            arrayOf(data.completed, "${data.progressRate}%", now.toKr01()),
            arrayOf(totalCount, "100%", data.estimateEndTime),
        ).also {
            listOf("진행", "진행율", "시간").toTextGrid(it).print()
        }


    }

}