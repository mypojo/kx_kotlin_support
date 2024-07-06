package net.kotlinx.openApi

import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.lazyLoad.lazyLoadStringSsm

class KoreaeximClientTest : BeSpecLight() {

    init {
        initTest(KotestUtil.FAST)

        Given("KoreaeximClient") {

            val secret by lazyLoadStringSsm("/api/koreaexim/key")
            val client = KoreaeximClient(secret)

            Then("달러-원 환율") {
                val dollarWon = client.dollarWon()
                log.info { "원달러 환율 : $dollarWon" }
            }
        }
    }

}
