package net.kotlinx.core2.calculator

import net.kotlinx.core1.string.toTextGrid
import net.kotlinx.core1.time.toKr01
import net.kotlinx.core2.test.TestLevel01
import net.kotlinx.core2.test.TestRoot
import java.time.LocalDateTime

class BatchEstimateTest : TestRoot() {

    @TestLevel01
    fun test() {

        val now = LocalDateTime.now()
        val startTime = now.minusMinutes(5)
        val totalCount: Long = 100
        val estimate1 = BatchEstimate(totalCount, 13)
        listOf(
            arrayOf(0, "0%", startTime.toKr01()),
            arrayOf(estimate1.completedCount, "${estimate1.progressRate}%", now.toKr01()),
            arrayOf(totalCount, "100%", estimate1.estimateEndTime(startTime)),
        ).also {
            listOf("진행", "진행율", "시간").toTextGrid(it).print()
        }


    }

}