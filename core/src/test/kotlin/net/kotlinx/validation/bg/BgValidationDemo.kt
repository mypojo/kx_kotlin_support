package net.kotlinx.validation.bg

import mu.KotlinLogging
import net.kotlinx.concurrent.delay
import net.kotlinx.domain.developer.DeveloperData
import kotlin.time.Duration.Companion.milliseconds

object BgValidationDemo {

    private const val GROUP = "demo"

    private val log = KotlinLogging.logger {}

    val VALIDATION_LIST = BgValidationList {

        regist {
            group = GROUP
            code = "demo01"
            desc = listOf("샘플 실행 테스트 - 성공")
            range = BgValidationType.DAY
            authors = listOf(DeveloperData(id = "kim"))
            validator = {
                200.milliseconds.delay()
                "정상종료1"
            }
        }

        regist {
            group = GROUP
            code = "demo02"
            desc = listOf("샘플 실행 테스트 - 실패")
            range = BgValidationType.DAY
            authors = listOf(DeveloperData(id = "kim"))
            validator = {
                150.milliseconds.delay()
                it += "실패했습니다. 사유 aaa"
                it += "실패했습니다. 사유 bbb"
                "정상종료2"
            }
        }

        configDemo01()
    }


}