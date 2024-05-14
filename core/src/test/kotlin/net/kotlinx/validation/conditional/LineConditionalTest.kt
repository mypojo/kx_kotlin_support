package net.kotlinx.validation.conditional

import com.linecorp.conditional.kotlin.and
import com.linecorp.conditional.kotlin.or
import io.kotest.matchers.shouldBe
import jakarta.validation.ValidationException
import net.kotlinx.concurrent.delay
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.print
import kotlin.time.Duration.Companion.milliseconds


/**
 * https://github.com/line/conditional
 * */
class LineConditionalTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        Given("사용자 유효성 체크 정의") {

            When("다음처럼 컨디션이 정의됨") {

                val 사용자검증 = condition("사용자검증") {
                    log.trace { "입력필드 벨리데이션 체크" }
                    true
                }

                val 관리자검증 = condition("관리자검증") {
                    log.trace { "DB 조회 후 각종 지표 검사.." }
                    200.milliseconds.delay()
                    it += "요청 xx / 검증값 bb -> cc를 만족해서 통과"
                    true
                }

                val VIP검증 = condition("VIP검증") {
                    it += "VIP 검증 경고.."
                    throw ValidationException("VIP가 아닙니다.")
                }

                Then("컨디션 정의 & 문서화") {

                    val ctx = conditionContext()
                    val condition = (사용자검증 and 관리자검증) or VIP검증

                    val result = condition.matches(ctx)
                    log.info { "작업 $condition => 결과 $result" }
                    val resultLogs = ctx.resultLogs()
                    resultLogs.print()
                    result shouldBe true
                    resultLogs.first { it.condition == "VIP검증" }.message.size shouldBe 2
                }
            }

        }

        Given("백그라운드 유요성 체크리스트") {

        }

    }
}