package net.kotlinx.validation.bean.anno

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidIpAddressValidator::class])
annotation class ValidIpAddress(
    val message: String = "{net.kotlinx.validation.bean.anno.ValidIpAddress.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)

class ValidIpAddressValidator : ConstraintValidator<ValidIpAddress, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrEmpty()) return true // null 값은 유효하다고 가정합니다. 필요하다면 이 부분을 변경할 수 있습니다.

        val parts = value.split(".")
        if (parts.size != 4) {
            return false
        }
        return parts.all { part ->
            part.toIntOrNull()?.let { it in 0..255 } ?: false
        }
    }
}