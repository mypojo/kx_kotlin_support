package net.kotlinx.validation

import com.linecorp.conditional.kotlin.and
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import jakarta.validation.constraints.NotNull
import net.kotlinx.concurrent.delay
import net.kotlinx.core.Comment
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.print
import net.kotlinx.validation.bean.HibernateValidation
import net.kotlinx.validation.bean.ValidationResultException
import net.kotlinx.validation.bean.anno.ValidMultiNumber
import net.kotlinx.validation.bean.throwIfFail
import net.kotlinx.validation.conditional.condition
import net.kotlinx.validation.conditional.validate
import org.hibernate.validator.constraints.Range
import kotlin.time.Duration.Companion.seconds

/**
 * 벨리데이션 통합 테스트
 * */
class ValidatorSupportKtTest : BeSpecLight() {

    private data class UserReq(

        @Comment("일 예산")
        @field:NotNull
        @field:Range(min = 50000, max = 100000000, message = "#{fieldName} : 최소 50,000원 ~ 최대 10억원까지 설정할 수 있습니다")
        @field:ValidMultiNumber(value = 10, message = "#{fieldName} :  #{attr[value]}원 단위로 설정할 수 있습니다")
        val dailyBudget: Long? = null,

        @Comment("이름")
        val name: String? = null,

        @Comment("구매 수")
        val buyCnt: Int? = null,

        )


    init {
        initTest(KotestUtil.FAST)

        Given("사용자 입력 검증 프로세스 (전체 3단계)") {

            suspend fun validationLogic(req: UserReq) {
                log.trace { "step01 사용자 입력값에 대한 필드 수준의 bean 벨리데이션" }
                HibernateValidation.validate(req).throwIfFail()

                log.trace { "step02 사용자 입력값에 대한 필드 수준의 커스텀 벨리데이션" }
                valid("nameCheck", req.name == "멍멍") { "강아지만 등록 가능합니다" }

                log.trace { "step03 API or DB 값 등으로 복잡한 체크 수행" }
                req.buyCnt?.let { buyCnt ->

                    val 사용자확인 = condition("사용자확인") {
                        "정상 사용자"
                    }

                    val 품질지수확인 = condition("품질지수확인") {
                        2.seconds.delay()
                        "정상 상태"
                    }

                    val 재고확인 = condition("재고확인") {
                        log.trace { " -> DB에서 재고를 가져옵니다..." }
                        val remainItemCnt = run {
                            2.seconds.delay()
                            10
                        }
                        if (buyCnt > remainItemCnt) {
                            it.failMsgs += "구매실패! 재고가 부족합니다 -> 요청/재고 = ${buyCnt}/${remainItemCnt}"
                        }
                        "구매완료 -> 요청/재고 = ${buyCnt}/${remainItemCnt}"
                    }

                    val validState = 사용자확인 and 품질지수확인 and 재고확인
                    val result = validState.validate()
                    if (log.isTraceEnabled) {
                        result.logs.print()  //전체 로그는 이걸로 확인
                    }
                    result.throwIfFail()
                }


            }
            Then("널체크") {
                val exception = shouldThrow<ValidationResultException> { validationLogic(UserReq(dailyBudget = null, "멍멍")) }
                exception.message shouldBe "일 예산 : 필수입력항목입니다"
            }
            Then("10단위 입력해야함") {
                val exception = shouldThrow<ValidationResultException> { validationLogic(UserReq(dailyBudget = 50002, "멍멍")) }
                exception.message shouldBe "일 예산 :  10원 단위로 설정할 수 있습니다"
            }
            Then("레인지 아웃") {
                shouldThrow<ValidationResultException> { validationLogic(UserReq(dailyBudget = 500, "멍멍")) }
            }
            Then("강아지로 입력해야함") {
                val exception = shouldThrow<ValidationResultException> { validationLogic(UserReq(dailyBudget = 500000, "야옹")) }
                exception.message shouldBe "강아지만 등록 가능합니다"
            }

            Then("재고없음") {
                val buyCnt = 100
                val exception = shouldThrow<ValidationResultException> { validationLogic(UserReq(dailyBudget = 500000, "멍멍", buyCnt)) }
                exception.message shouldBe "구매실패! 재고가 부족합니다 -> 요청/재고 = ${buyCnt}/10"
            }

            When("정상통과") {
                val buyCnt = 10
                validationLogic(UserReq(dailyBudget = 500000, "멍멍", buyCnt))
            }


        }
    }

}
