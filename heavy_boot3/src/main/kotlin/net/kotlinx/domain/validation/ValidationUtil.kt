package net.kotlinx.domain.validation


import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator


/** 벨리데이터 팩토리 있던데 뭐 지정하는게 딱히 없음 */
object ValidationUtil {

    /** 기본 벨리데이터 */
    private val VALIDATOR: Validator by lazy { Validation.buildDefaultValidatorFactory().validator }

    fun <T> validate(obj: T): Set<ConstraintViolation<T>> = VALIDATOR.validate(obj)

    fun validateResult(obj: Any): List<ValidationResult> = VALIDATOR.validate(obj).map { it.toResult() }


}
