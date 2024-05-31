package net.kotlinx.validation.bg

import mu.KotlinLogging
import net.kotlinx.domain.developer.DeveloperData


private val log = KotlinLogging.logger {}

object BgValidationLogic {
    const val GROUP = "logic"
}

fun BgValidationList.configDemo01() {
    regist {
        group = BgValidationLogic.GROUP
        code = "rpt01"
        desc = listOf("합계 검증", "D-4 아테나 & kinesis")
        range = BgValidationType.DAY
        authors = listOf(DeveloperData(id = "kim"))
        validator = {
            log.debug { "thread 1 : ${Thread.currentThread().name}" }
            Thread.sleep(100)
            log.debug { "thread 2 : ${Thread.currentThread().name}" }
            "정상종료1"
        }
    }

    regist {
        group = BgValidationLogic.GROUP
        code = "rpt_system"
        desc = listOf("전체 시스템 모니터링 & 이상감지", "API & RDS 비교")
        range = BgValidationType.DAY
        authors = listOf(DeveloperData(id = "lee"))
        validator = {
            log.debug { "thread 1 : ${Thread.currentThread().name}" }
            Thread.sleep(200)
            log.debug { "thread 2 : ${Thread.currentThread().name}" }
            "정상종료2"
        }
    }
}