package net.kotlinx.validation.repeated

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

internal class BgValidationConfigKtTest : BeSpecLog() {

    init {
        initTest(KotestUtil.SLOW)

        Given("validationConfig") {

            val validationList = BgValidationDemo.VALIDATION_LIST

            Then("주어진 벨리데이션들 테스트 -> 스래드 병렬처리") {
                validationList.allValidations.validateAllByThread().print2()
            }

            Then("주어진 벨리데이션들 테스트 -> 코루틴 병렬처리") {
                validationList.allValidations.validateAllByCoroutine().print2()
            }

            Then("개벌 필터링") {
                validationList.allValidations.filter { it.group == BgValidationLogic.GROUP }.size shouldBe 2
            }
        }
    }

}