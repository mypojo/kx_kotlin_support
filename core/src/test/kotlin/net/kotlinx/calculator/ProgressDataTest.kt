package net.kotlinx.calculator

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.toKr01
import java.time.LocalDateTime

class ProgressDataTest : BeSpecLog() {

    init {

        initTest(KotestUtil.SLOW)

        Given("ProgressData") {
            Then("진행율 계산") {
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
            Then("프로그레스 진행확인") {
                val startTime = LocalDateTime.now()
                val totalCount: Long = 30
                (0..totalCount).forEach {
                    val data = ProgressData(totalCount, it, startTime)
                    log.info { " -> $data" }
                    Thread.sleep(80)
                }
            }
        }
    }
}