package net.kotlinx.json.koson

import com.lectra.koson.arr
import com.lectra.koson.obj
import com.lectra.koson.rawJson
import io.kotest.matchers.shouldBe
import net.kotlinx.json.gson.toGsonData
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
        }
    }
}