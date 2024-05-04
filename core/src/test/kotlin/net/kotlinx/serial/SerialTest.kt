package net.kotlinx.serial

import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.kotlinx.core.serial.SerialJsonSet
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


class SerialTest : BeSpecLog() {
    init {

        initTest(KotestUtil.FAST)

        Given("코틀린 기본 시리얼 테스트") {

            @Serializable
            data class SerialDemo(
                val name: String,
                val datas: List<String>,
                private val intervalSec: Double?,
            ) {

                val interval: Duration? = intervalSec?.let { it.seconds }

            }

            Then("정상 변환 1") {
                val demo1 = SerialDemo("asdsad", listOf("aa", "bb"), 2.2)
                val json = SerialJsonSet.JSON.encodeToString(demo1)
                val demo2 = SerialJsonSet.JSON.decodeFromString<SerialDemo>(json)
                demo1 shouldBe demo2
            }
            Then("정상 변환 2 - null인경우") {
                val demo1 = SerialDemo("asdsad", listOf("aa", "bb"), null)
                val json = SerialJsonSet.JSON.encodeToString(demo1)
                val demo2 = SerialJsonSet.JSON.decodeFromString<SerialDemo>(json)
                demo1 shouldBe demo2
            }
        }

    }
}