package net.kotlinx.validation.bg

import mu.KotlinLogging
import net.kotlinx.collection.addAndGet
import net.kotlinx.domain.developer.DeveloperData

object BgValidationDemo {

    private val _VALIDATIONS = mutableListOf<BgValidationConfig>()

    private const val GROUP = "demo"

    private val log = KotlinLogging.logger {}

    val RPT01 = _VALIDATIONS.addAndGet {
        BgValidationConfig {
            group = GROUP
            code = "rpt01"
            desc = listOf("합계 검증")
            codeDesc = "D-4 아테나 & kinesis"
            range = BgValidationType.DAY
            authors = listOf(DeveloperData(id = "kim"))
            validationCode = BgValidation.inline {
                log.debug { "thread 1 : ${Thread.currentThread().name}" }
                Thread.sleep(3000)
                log.debug { "thread 2 : ${Thread.currentThread().name}" }
                "정상종료1"
            }
        }
    }

    val RPT_SYSTEM = _VALIDATIONS.addAndGet {
        BgValidationConfig {
            group = GROUP
            code = "rpt_system"
            desc = listOf("전체 시스템 모니터링 & 이상감지")
            codeDesc = "API & RDS 비교"
            range = BgValidationType.DAY
            authors = listOf(DeveloperData(id = "lee"))
            validationCode = BgValidation.inline {
                log.debug { "thread 1 : ${Thread.currentThread().name}" }
                Thread.sleep(2000)
                log.debug { "thread 2 : ${Thread.currentThread().name}" }
                "정상종료2"
            }
        }
    }

    /** 정의된 벨리데이션 */
    val VALIDATIONS = _VALIDATIONS.toList()


}