package net.kotlinx.json.gson

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.time.toKr01
import net.kotlinx.time.truncatedMills
import java.time.LocalDateTime

class GsonSetTest : BeSpecLight() {

    init {
        initTest(KotestUtil.FAST)

        Given("GsonSet") {

            When("날짜변환 테스트 - ZONE") {

                data class Poo1(val date: LocalDateTime)

                val poo1 = Poo1(LocalDateTime.now())

                val json = GsonSet.TABLE_UTC_WITH_ZONE.toJson(poo1)
                val poo2 = GsonSet.TABLE_UTC_WITH_ZONE.fromJson(json, Poo1::class.java)!!

                log.info { "json : $json" }
                log.info { "원본시간 : ${poo1.date.toKr01()}" }
                log.info { "변환시간 : ${poo2.date.toKr01()}" }

                poo1 shouldBe poo2
            }

            When("변환테스트") {

                data class Poo(
                    val date: LocalDateTime?,
                    @field:NotExpose
                    val alias: String,
                )

                Then("NotExpose 때문에 블일치") {
                    val poo1 = Poo(LocalDateTime.now().truncatedMills(), "XG")
                    val json = GsonSet.GSON.toJson(poo1)
                    log.debug { " -> 변환 json : $json" }
                    val poo2 = GsonSet.GSON.fromJson(json, Poo::class.java)
                    poo1 shouldNotBe poo2
                }

            }
        }
    }

}
