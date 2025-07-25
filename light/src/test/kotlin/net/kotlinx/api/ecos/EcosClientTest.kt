package net.kotlinx.api.ecos

import io.kotest.matchers.bigdecimal.shouldBeInRange
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

internal class EcosClientTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("EcosClient") {

            Then("달러-원 환율 가져오기") {
                val client = koin<EcosClient>()
                val value = client.dollarWon()
                log.info { "원달러 환율 : ${value}" }
                value shouldBeInRange 1300.toBigDecimal()..1500.toBigDecimal()
            }

        }
    }
}



