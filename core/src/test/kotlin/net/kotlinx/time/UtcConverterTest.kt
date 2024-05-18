package net.kotlinx.time

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.number.toLocalDateTime

class UtcConverterTest : BeSpecLight() {

    init {
        initTest(KotestUtil.FAST)

        val timeNum = 1715928125498
        val datetime = timeNum.toLocalDateTime()
        val timeFormatKr = "2024년05월17일(금) 15시42분05초"

        Given("밀리초 시간 = 존이 포함된 절대값임") {

            Then("일반변환시 15시로 표기") {
                datetime.toKr01() shouldBe timeFormatKr
            }

            When("동일한 숫자를 시간으로 표현하는경우") {
                Then("UTC와 서울의 시간이 틀림") {
                    timeNum.toLocalDateTime(TimeUtil.UTC).toKr01() shouldBe "2024년05월17일(금) 06시42분05초"
                    timeNum.toLocalDateTime(TimeUtil.SEOUL).toKr01() shouldBe timeFormatKr
                }
            }
        }

        Given("ISO_INSTANT_SEOUL 변환 (kinesis for athena)") {

            val converter = UtcConverter {
                this.zoneId = TimeUtil.SEOUL
                timeFormat = TimeFormat.ISO_INSTANT_SEOUL
            }

            When("SEOUL <-> UTC로 변환됨") {
                val utc = converter.toText(datetime)
                Then("SEOUL -> UTC : -9시간 되어서 표기된다") {
                    utc shouldBe "2024-05-17T06:42:05.498Z"
                }
                Then("UTC -> SEOUL : 정상 시간 변환") {
                    converter.fromText(utc).toKr01() shouldBe timeFormatKr
                }
            }

        }

        Given("ISO_OFFSET 변환 (eventBridge)") {

            val converter = UtcConverter {
                this.zoneId = TimeUtil.SEOUL
                timeFormat = TimeFormat.ISO_OFFSET
            }

            When("SEOUL <-> UTC로 변환됨") {
                val utc = converter.toText(datetime)
                Then("SEOUL -> UTC : 현재시간 뒤에 +9시간 되어서 표기된다") {
                    converter.toText(datetime) shouldBe "2024-05-17T15:42:05.498+09:00"
                }
                Then("UTC -> SEOUL : 정상 시간 변환") {
                    converter.fromText(utc).toKr01() shouldBe timeFormatKr
                }
            }
        }

        Given("ISO_OFFSET 변환을 UTC로 주는 경우") {
            val converter = UtcConverter {
                this.zoneId = TimeUtil.UTC
                timeFormat = TimeFormat.ISO_OFFSET
            }

            When("UTC <-> UTC로 변환됨") {
                val utc = converter.toText(datetime)
                Then("UTC -> UTC : 오프셋이 없음으로 ISO_INSTANT 하고 동일하게 표기된다") {
                    converter.toText(datetime) shouldBe "2024-05-17T15:42:05.498Z"
                }
                Then("UTC -> UTC : 정상 시간 변환") {
                    converter.fromText(utc).toKr01() shouldBe timeFormatKr
                }
            }
        }
    }

}
