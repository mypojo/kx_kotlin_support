package net.kotlinx.domain.validation

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

private const val GROUP = "테스트"

internal class ValidationConfigKtTest : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("validationConfig") {
            Then("임시") {
                val rptValidations = listOf(
                    validationConfig {
                        group = GROUP
                        code = "rpt01"
                        desc = "합계 검증"
                        codeDesc = "D-4 아테나 & kinesis"
                        range = ValidationRange.DAY
                        authors = listOf("kim")
                        validationCode = { _ ->
                            log.info { "thread 1 : ${Thread.currentThread().name}" }
                            Thread.sleep(4000)
                            log.info { "thread 2 : ${Thread.currentThread().name}" }
                            "정상종료1"
                        }
                    },
                    validationConfig {
                        group = GROUP
                        code = "rpt-system"
                        descs {
                            +"전체 시스템 모니터링 & 이상감지"
                        }
                        codeDesc = "API & RDS 비교"
                        range = ValidationRange.DAY
                        authors = listOf("lee")
                        validationCode = { _ ->
                            log.info { "thread 1 : ${Thread.currentThread().name}" }
                            Thread.sleep(7000)
                            log.info { "thread 2 : ${Thread.currentThread().name}" }
                            "정상종료2"
                        }
                    },
                )

                log.info { "테스트 시작.." }
                rptValidations.check(8).printAndThrow()
                log.info { "테스트 종료" }
            }
        }
    }

}