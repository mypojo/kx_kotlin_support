package net.kotlinx.core.calculator

import net.kotlinx.core.string.toTextGrid
import net.kotlinx.core.test.TestLevel01
import net.kotlinx.core.test.TestLevel02
import net.kotlinx.core.test.TestRoot
import net.kotlinx.core.time.toKr01
import java.time.LocalDateTime

class ProgressDataTest : TestRoot() {

    @TestLevel01
    fun test() {
        val now = LocalDateTime.now()
        val totalCount: Long = 100
        val data = ProgressData(totalCount, 47, now.minusMinutes(5))
        listOf(
            arrayOf(0, "0%", data.startTime!!.toKr01()),
            arrayOf(data.completed, "${data.progressRate}%", now.toKr01()),
            arrayOf(totalCount, "100%", data.estimateEndTime),
        ).also {
            listOf("진행", "진행율", "시간").toTextGrid(it).print()
        }
    }

    @TestLevel02
    fun run() {
        val now = LocalDateTime.now()
        val totalCount: Long = 24
        (0 .. totalCount).forEach {
            val data = ProgressData(totalCount, it, now)
            log.info { " -> $data" }
            Thread.sleep(700)
        }
    }

}