package net.kotlinx.domain.validation.ann

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * 기본적으로 자바(UTF-8)기준으로 한다.  한글3바이트. 영문1바이트
 * 오라클 UTF-8 인코딩일 경우 그대로 사용하면 될듯 하다.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidByteValidator::class])
annotation class ValidByte(
    val max: Int,
    val message: String = "최대 바이트수({max}byte) 를 초과했습니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

internal class ValidByteValidator : ConstraintValidator<ValidByte, String?> {
    var maxByteSize = 0
    override fun isValid(text: String?, arg1: ConstraintValidatorContext): Boolean {
        if (text == null) return true
        return text.toByteArray().size <= maxByteSize
    }

    override fun initialize(annotation: ValidByte) {
        maxByteSize = annotation.max
    }
}