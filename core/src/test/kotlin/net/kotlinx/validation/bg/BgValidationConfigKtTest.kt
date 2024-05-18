package net.kotlinx.validation.bg

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.time.TimeStart

internal class BgValidationConfigKtTest : BeSpecLog() {

    init {
        initTest(KotestUtil.SLOW)

        Given("validationConfig") {
            Then("주어진 벨리데이션들 테스트 -> 병렬처리") {
                val start = TimeStart()
                val validations = BgValidationDemo.VALIDATIONS
                log.info { "테스트 시작.." }
                validations.validateAll().andThrowIfInvalid()
                log.info { "테스트 종료 -> $start" }
            }
        }
    }

}