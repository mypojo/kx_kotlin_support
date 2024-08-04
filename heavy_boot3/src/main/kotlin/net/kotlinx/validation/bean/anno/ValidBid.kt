package net.kotlinx.validation.bean.anno

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import net.kotlinx.string.DomainValidationUtil.isBusinessId
import org.hibernate.validator.constraints.Length
import kotlin.reflect.KClass

/** 사업자등록번호  */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidBidValidator::class])
@Length(min = 10, max = 10)
annotation class ValidBid(
    val message: String = "{net.kotlinx.validation.bean.anno.ValidBid.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

internal class ValidBidValidator : ConstraintValidator<ValidBid, String> {
    override fun initialize(annotation: ValidBid) {}
    override fun isValid(text: String?, context: ConstraintValidatorContext): Boolean {
        if (text.isNullOrEmpty()) return true
        return isBusinessId(text)
    }
}