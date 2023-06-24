package net.kotlinx.validation.ann

import javax.validation.Payload
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

/** IP 벨리데이션  */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Pattern(regexp = "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$", message = "적합한 IP가 아닙니다")
annotation class ValidIp(
    val message: String = "적합한 IP가 아닙니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Payload>> = []
)