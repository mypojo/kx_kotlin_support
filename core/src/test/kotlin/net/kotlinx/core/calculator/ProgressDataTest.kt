package net.kotlinx.core.calculator

import io.kotest.core.spec.style.BehaviorSpec
import mu.KotlinLogging
import net.kotlinx.core.string.toTextGridPrint
import net.kotlinx.core.test.KotestUtil
import net.kotlinx.core.test.init
import net.kotlinx.core.time.toKr01
import java.time.LocalDateTime

class ProgressDataTest : BehaviorSpec({

    val log = KotlinLogging.logger {}
    init(KotestUtil.SLOW)

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
            val now = LocalDateTime.now()
            val totalCount: Long = 24
            (0..totalCount).forEach {
                val data = ProgressData(totalCount, it, now)
                log.info { " -> $data" }
                Thread.sleep(120)
            }
        }
    }
})