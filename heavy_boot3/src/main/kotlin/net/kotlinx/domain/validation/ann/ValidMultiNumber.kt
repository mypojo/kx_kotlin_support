package net.kotlinx.domain.validation.ann

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * X의 배수만 입력되어야 한다. ex) value=11 => 22,44...
 * 입찰가에 주로 사용
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidMultiNumberValidator::class])
annotation class ValidMultiNumber(
    val value: Int = 10,
    val message: String = "{value}의 배수로만 입력되어야 합니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Payload>> = []
)

internal class ValidMultiNumberValidator : ConstraintValidator<ValidMultiNumber, Number?> {
    private var value = 0
    override fun isValid(num: Number?, arg1: ConstraintValidatorContext): Boolean {
        if (num == null) return true
        val rest = (num.toLong() % value).toInt()
        return rest == 0
    }

    override fun initialize(annotation: ValidMultiNumber) {
        value = annotation.value
    }
}