package net.kotlinx.collection

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class CollectionSupportKtTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        val demo = listOf("a", null, "", "b","c")

        Given("기본테스트") {
            Then("컬렉션 사칙연산") {
                val xx = listOf("a","",null)
                println(demo - xx)
            }
        }

        Given("mapNotEmpty") {
            Then("널이거나 빈값 제외") {
                check(demo.mapNotEmpty { it }.size == 2)
            }
        }
    }
}