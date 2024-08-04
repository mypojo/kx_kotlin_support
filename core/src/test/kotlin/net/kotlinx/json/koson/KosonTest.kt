package net.kotlinx.json.koson

import com.lectra.koson.arr
import com.lectra.koson.obj
import com.lectra.koson.rawJson
import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

internal class KosonTest : BeSpecLog() {

    companion object {
        val DEMO_KOSON = obj {
            "type" to "normal"
            "members" to arr[
                obj { "name" to "A"; "age" to 10; },
                obj { "name" to "B"; "age" to 20; },
            ]
        }
    }

    init {
        initTest(KotestUtil.FAST)

        Given("koson") {

            Then("koson에서 json / text 타입 인식") {
                val jsonText = DEMO_KOSON.toString()
                val json2 = obj {
                    "option" to rawJson(jsonText)  //json 으로 인식
                    "optionText" to jsonText
                }.toGsonData()
                json2["option"].isObject shouldBe true
                json2["optionText"].isPrimitive shouldBe true
            }

            Then("array 확인하기") {
                val aar = arr[1, 2, 3]
                val data = obj {
                    "arr" to arr[1, 2, 3]
                    "aar" to aar
                }
                data.toGsonData()["aar"].size shouldBe 3
            }

            Then("map 변환하기") {
                val map = mapOf(
                    "a" to 3,
                    "b" to 4,
                )
                map.toKoson().toGsonData()["a"].str == "3"
            }
        }
    }

}
