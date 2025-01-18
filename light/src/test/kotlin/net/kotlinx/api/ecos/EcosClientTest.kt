package net.kotlinx.api.ecos

import io.kotest.matchers.bigdecimal.shouldBeInRange
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

internal class EcosClientTest : BeSpecHeavy() {

    // 데이터를 저장할 데이터 클래스 정의
    data class ExchangeRateData(
        val TIME: String,
        val DATA_VALUE: String
    )

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



