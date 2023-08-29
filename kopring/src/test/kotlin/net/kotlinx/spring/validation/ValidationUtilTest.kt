package net.kotlinx.spring.validation

import jakarta.validation.constraints.NotNull
import net.kotlinx.core.test.TestRoot
import java.time.LocalDate

class ValidationUtilTest : TestRoot() {

    data class ValidRequestDto(
        @field:NotNull(message = "value는 필수 입력값입니다.")
        val value: String? = null,

        @field:NotNull(message = "createdAt은 필수 입력값입니다.")
        val createdAt: LocalDate? = null,

        @field:NotNull(message = "number는 필수 입력값입니다.")
        val number: Long? = null
    )




}