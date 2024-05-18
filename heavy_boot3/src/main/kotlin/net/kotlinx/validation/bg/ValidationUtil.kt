package net.kotlinx.validation.bg


import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator


/** 벨리데이터 팩토리 있던데 뭐 지정하는게 딱히 없음 */
@Deprecated("kotlin 제품 사용해주세요")
object ValidationUtil {

    /** 기본 벨리데이터 */
    private val VALIDATOR: Validator by lazy { Validation.buildDefaultValidatorFactory().validator }

    fun <T> validate(obj: T): Set<ConstraintViolation<T>> = VALIDATOR.validate(obj)

    fun validateResult(obj: Any): List<ValidationResult> = VALIDATOR.validate(obj).map { it.toResult() }


}
