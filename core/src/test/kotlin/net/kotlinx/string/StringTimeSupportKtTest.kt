package net.kotlinx.string

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.time.TimeFormat
import net.kotlinx.time.TimeUtil
import net.kotlinx.time.toKr01

internal class StringTimeSupportKtTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        Given("기본적인 설명") {
            When("UTC 시간이 주어진 경우") {
                val zonedDateTime = TimeFormat.ISO.toZonedDateTime("2023-12-22T06:43:49.611+00:00")

                Then("UTC 시간은 입력한 그대로 나와야함") {
                    zonedDateTime.withZoneSameInstant(TimeUtil.UTC).toLocalDateTime().toKr01() shouldBe "2023년12월22일(금) 06시43분49초"
                }
                Then("SEOUL 시간은 +9 시간대가 나와야함") {
                    zonedDateTime.withZoneSameInstant(TimeUtil.SEOUL).toLocalDateTime().toKr01() shouldBe "2023년12월22일(금) 15시43분49초"
                }
            }

            When("SEOUL 시간이 주어진 경우") {
                val zonedDateTime = TimeFormat.ISO.toZonedDateTime("2023-12-22T15:43:49.611+09:00[Asia/Seoul]") //뒤에 존 표시해도 되고 안해도됨

                Then("UTC 시간은 -9 시간대가 나와야함") {
                    zonedDateTime.withZoneSameInstant(TimeUtil.UTC).toLocalDateTime().toKr01() shouldBe "2023년12월22일(금) 06시43분49초"
                }
                Then("SEOUL 시간은 그대로 나와야함") {
                    zonedDateTime.withZoneSameInstant(TimeUtil.SEOUL).toLocalDateTime().toKr01() shouldBe "2023년12월22일(금) 15시43분49초"
                }
            }
        }

        Given("string -> LocalDateTime 변환") {
            When("일반포맷") {
                Then("일반적인 변환") {
                    "2022-12-31".toLocalDateTime().toKr01() shouldBe "2022년12월31일(토) 00시00분00초"
                    "20221231-11:30:12".toLocalDateTime().toKr01() shouldBe "2022년12월31일(토) 11시30분12초"
                }
            }

            When("ISO & 명시적 +시간 없음") {
                Then("한국시간으로 그대로 변환") {
                    "2023-01-04T14:09:12.952065".toLocalDateTime().toKr01() shouldBe "2023년01월04일(수) 14시09분12초"
                }
            }

            When("ISO & UTC 시간") {
                Then("한국시간으로 변환") {
                    "2023-12-22T06:43:49.611+00:00".toLocalDateTime().toKr01() shouldBe "2023년12월22일(금) 15시43분49초"
                }
            }

        }
    }
}