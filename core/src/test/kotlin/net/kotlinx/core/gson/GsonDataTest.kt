package net.kotlinx.core.gson

import io.kotest.matchers.shouldBe
import net.kotlinx.core.number.halfUp
import net.kotlinx.koson.KosonTest.Companion.DEMO_KOSON
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.TestPoo01
import net.kotlinx.kotest.initTest
import net.kotlinx.string.print
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private data class DataClass01(
    val name: String,
) {
    lateinit var aa: String

}

internal class GsonDataTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("GsonData") {
            Then("데이터클래스 변환") {
                val class01 = DataClass01("aa")
                class01.aa = "데모데이터"

                val json = GsonSet.GSON.toJson(class01)
                log.debug { "json : $json -> 객체 변환" }

                val q2 = GsonData.parse(json).fromJson<DataClass01>()
                q2.aa shouldBe "데모데이터"
            }

            Then("출력형식 검사") {
                val array = GsonData.array {
                    add(GsonData.obj {
                        put("name", "일반")
                        put("a", 12)
                        put("b", "한글abc")
                    })
                    val num = 121238.12149873
                    add(GsonData.obj {
                        put("name", "숫자")
                        put("a", num.toBigDecimal().halfUp(1))
                        put("b", num.toBigDecimal().halfUp(3))
                    })
                    add(GsonData.obj {
                        put("name", "유효숫자 주의")
                        put("a", num.toBigDecimal().halfUp(-1))
                        put("b", num.toBigDecimal().halfUp(-1).toPlainString())
                    })
                }
                array.print()
            }
        }

        Given("json 변환해보기") {

            Then("json 파싱후 오버라이드") {
                val updated = GsonData.parse(DEMO_KOSON).apply {
                    put("type", "수정됨")
                    put("xxx", "yyy")
                }
                log.info { "json update -> $updated" }
                updated["type"].str shouldBe "수정됨"
                updated["type2"].str shouldBe null
            }

            Then("json / text 타입 인식") {
                val gsonData = GsonData.parse(DEMO_KOSON)
                gsonData["members"].filter { it["name"].str == "B" }.onEach { it.put("age", 25) }
                gsonData["members"].sumOf { it["age"].long ?: 0L } shouldBe 35L
            }


            val poo1 = TestPoo01().apply {
                name = "영감님"
                age = 75
                cnt = 99987
                time = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS) //밀리초 생략
                parent = TestPoo01().apply {
                    name = "할매"
                    age = 132
                }
            }

            Then("객체 변환/역변환 -> 같아야함") {
                val gsonData = GsonData.fromObj(poo1)
                val poo2 = gsonData.fromJson<TestPoo01>()
                poo1 shouldBe poo2
            }

            Then("map 변환/역변환 -> 같아야함") {
                val map = GsonData.fromObj(poo1).fromJson<Map<String, Any>>()
                val poo2 = GsonData.fromObj(map).fromJson<TestPoo01>()
                poo1 shouldBe poo2
            }

        }
    }
}