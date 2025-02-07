package net.kotlinx.delegate

import io.kotest.matchers.shouldBe
import net.kotlinx.json.gson.GsonData
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

internal class MapAttributeDelegateTest : BeSpecLog() {

    data class Poo2(
        val name: String
    ) : MapAttribute {
        override var attributes: MutableMap<String, Any> = mutableMapOf()
    }

    var Poo2.age: Int by MapAttributeDelegate<Int>()


    init {
        initTest(KotestUtil.FAST)

        Given("MapAttributeDelegate") {

            val poo = Poo2("김씨")

            Then("리플렉션 -> 추가된 멤버필드는 없음. 대신 attributes 가 노출됨") {
                poo.age = 17
                GsonData.fromObj(poo).toString() shouldBe """{"name":"김씨","attributes":{"age":17}}"""
            }
        }
    }

}

