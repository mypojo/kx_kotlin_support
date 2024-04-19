package net.kotlinx.core.calculator

import net.kotlinx.core.string.toTextGridPrint
import net.kotlinx.core.time.toKr01
import net.kotlinx.test.TestLevel01
import net.kotlinx.test.TestLevel02
import net.kotlinx.test.TestRoot
import java.time.LocalDateTime

class ProgressDataTest : TestRoot() {

    @TestLevel01
    fun test() {
        val now = LocalDateTime.now()
        val totalCount: Long = 100
        val data = ProgressData(totalCount, 47, now.minusMinutes(5))
        listOf("진행", "진행율", "시간").toTextGridPrint {
            listOf(
                arrayOf(0, "0%", data.startTime!!.toKr01()),
                arrayOf(data.completed, "${data.progressRate}%", now.toKr01()),
                arrayOf(totalCount, "100%", data.estimateEndTime),
            )
        }
    }

    @TestLevel02
    fun `프로그레스 데이터 직접생성 - 20초`() {
        val now = LocalDateTime.now()
        val totalCount: Long = 24
        (0..totalCount).forEach {
            val data = ProgressData(totalCount, it, now)
            log.info { " -> $data" }
            Thread.sleep(400)
        }
    }

}