package net.kotlinx.validation.ann

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.Pattern
import kotlin.reflect.KClass

/** 코드 참고용 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
@Pattern(regexp = "^01[016789]-[0-9]{3,4}-[0-9]{4}$", message = "적합한 전화번호 형식이 아닙니다")
annotation class ValidTel(
    val message: String = "-",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
