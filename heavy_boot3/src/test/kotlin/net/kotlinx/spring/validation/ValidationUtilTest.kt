package net.kotlinx.spring.validation

import jakarta.validation.constraints.NotNull
import net.kotlinx.domain.validation.ValidationUtil
import net.kotlinx.domain.validation.print
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import java.time.LocalDate

class ValidationUtilTest : BeSpecLog() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("validation") {

            log.warn { "표준 벨리데이션 사용 금지!! 너무 구림" }

            /** 어노테이션 예제 */
            data class ValidRequestDto(
                @field:NotNull(message = "value는 필수 입력값입니다.")
                val value: String? = null,

                @field:NotNull(message = "createdAt은 필수 입력값입니다.")
                val createdAt: LocalDate? = null,

                @field:NotNull(message = "number는 필수 입력값입니다.")
                val number: Long? = null
            )

            Then("테스트 샘플") {
                ValidationUtil.validateResult(ValidationString01().apply {
                    groupName01 = "그룹1"
                    groupName02 = "그룹2"
                    comNo1 = "12345678"
                    comNo2 = "1234567890"
                    contents1 = "짧은본문글자"
                    bidCost = 110
                    lastInviteDate = "20230633"
                    //tel = "110-1111-2222"
                }).print()
            }

        }
    }


}