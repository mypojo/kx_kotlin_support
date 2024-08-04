package net.kotlinx.validation.conditional

import com.linecorp.conditional.kotlin.and
import com.linecorp.conditional.kotlin.or
import io.kotest.matchers.shouldBe
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
                    "입력필드 벨리데이션 체크 성공"
                }

                val 관리자검증 = condition("관리자검증") {
                    200.milliseconds.delay()
                    if (1 == 2) {
                        it.failMsgs += "요청 xx / 검증값 bb -> cc를 만족해서 실패"
                    }
                    "DB 조회 후 각종 지표 검사 성공"
                }

                val VIP검증 = condition("VIP검증") {
                    if (1 == 1) {
                        it.failMsgs += "VIP가 아닙니다"
                    }
                    if (1 == 1) {
                        it.failMsgs += "서비스 대상 지역이 아닙니다"
                    }
                    "VP 정상 인증 완료"
                }

                Then("VPC 검증이 실패하더라도 최종 벨리데이션은 통과") {

                    val condition = (사용자검증 and 관리자검증) or VIP검증

                    //시간체크
                    val result = condition.validate()
                    log.info { "작업 $condition => 결과 ${result.ok}" }
                    result.logs.print()
                    result.ok shouldBe true
                    result.logs.first { it.condition == "VIP검증" }.message.size shouldBe 2
                }
            }
        }

    }
}