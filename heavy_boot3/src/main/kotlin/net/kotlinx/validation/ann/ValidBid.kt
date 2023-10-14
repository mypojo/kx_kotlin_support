package net.kotlinx.validation.ann

import com.google.common.base.Strings
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import net.kotlinx.core.string.DomainValidationUtil.isBusinessId
import org.hibernate.validator.constraints.Length
import kotlin.reflect.KClass

/** 사업자등록번호  */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidBidValidator::class])
@Length(min = 10, max = 10)
annotation class ValidBid(
    val message: String = "적합한 사업자 등록번호가 아닙니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

internal class ValidBidValidator : ConstraintValidator<ValidBid, String> {
    override fun initialize(annotation: ValidBid) {}
    override fun isValid(text: String?, context: ConstraintValidatorContext): Boolean {
        if (text == null) return true
        return if (Strings.isNullOrEmpty(text)) true else isBusinessId(text)
    }
}