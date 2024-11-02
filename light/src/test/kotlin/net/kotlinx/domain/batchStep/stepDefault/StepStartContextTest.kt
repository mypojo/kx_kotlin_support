package net.kotlinx.domain.batchStep.stepDefault

import io.kotest.matchers.shouldBe
import net.kotlinx.json.gson.GsonData
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.time.toKr01
import net.kotlinx.time.truncatedMills
import java.time.LocalDateTime

class StepStartContextTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.FAST)

        Given("StepStartContext") {
            Then("JSON 변환 체크") {
                val startTime = LocalDateTime.now().truncatedMills()
                log.info { "시간 -> ${startTime.toKr01()}" }
                val context = StepStartContext(
                    startTime,
                    100, 1,
                    listOf("a", "b")
                )
                log.info { "원본객체 변환 -> $context" }
                val gsonData = GsonData.fromObj(context)
                val json = gsonData.toString()
                log.info { "객체 -> SON 변환 -> $json" }
                val parsed = gsonData.fromJson<StepStartContext>()
                log.info { "JSON -> 객체 변환 -> $parsed" }
                parsed shouldBe context
            }

        }
    }


}
