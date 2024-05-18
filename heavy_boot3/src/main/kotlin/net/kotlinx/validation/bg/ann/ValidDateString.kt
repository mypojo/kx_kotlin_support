package net.kotlinx.validation.bg.ann

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import net.kotlinx.time.TimeFormat
import kotlin.reflect.KClass

/** 문자열 형식이지만 실제로는 숫자만 들어가는 형태를 컨버팅한다.
 * 실제로는 DateTime을 사용해야 하지만, 낙후된 환경에서 문자열로 DateTime을 대체할때 사용하자
 * 주의!! 6월 31일로 입력시 벨리데이션은 통과되고 6월 30일로 리턴됨 (6월 31일은 없음)
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidDateStringValidator::class])
annotation class ValidDateString(
    val pattern: TimeFormat = TimeFormat.YMD,
    val message: String = "적합한 날짜 형식이 아닙니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/** 직접 파싱하는 방법을 사용함으로, 무거운 로직에는 사용하면 안된다.  */
class ValidDateStringValidator : ConstraintValidator<ValidDateString, String?> {

    private lateinit var pattern: TimeFormat

    override fun isValid(text: String?, context: ConstraintValidatorContext): Boolean {
        if (text.isNullOrEmpty()) return true
        return try {
            pattern[text]
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun initialize(constraintAnnotation: ValidDateString) {
        pattern = constraintAnnotation.pattern
    }
}