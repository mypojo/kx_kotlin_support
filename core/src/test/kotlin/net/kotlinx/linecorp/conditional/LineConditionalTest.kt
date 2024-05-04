package net.kotlinx.linecorp.conditional

import com.linecorp.conditional.kotlin.and
import com.linecorp.conditional.kotlin.coroutineCondition
import com.linecorp.conditional.kotlin.or
import net.kotlinx.core.string.ResultText
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

/**
 * https://github.com/line/conditional
 * */
class LineConditionalTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        Given("유효성 체크 정의") {

            val ctx = conditionContext()

            val 사용자검증 = coroutineCondition { _ -> true }.alias { "유효성통과" }
            val 관리자검증 = coroutineCondition { true }.alias { "관리자검증" }

            val VIP검증 = coroutineCondition {
                it.msgs += ResultText(false, "VIP가 아닙니다.")
                false
            }.alias { "VIP검증" }

            Then("컨디션 정의 & 문서화") {

                val condition = (사용자검증 and 관리자검증) or (VIP검증)

                val result = condition.matches(ctx)
                log.info { "작업결과 : $result" }
                ctx.logs().forEach {
                    println(it)
                }
                log.warn { ctx.msgs }
            }
        }
    }
}