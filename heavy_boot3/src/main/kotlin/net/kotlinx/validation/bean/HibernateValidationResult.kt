package net.kotlinx.validation.bean

import jakarta.validation.ConstraintViolation
import net.kotlinx.core.Comment
import net.kotlinx.reflect.annotaionAll
import net.kotlinx.reflect.findClass
import net.kotlinx.reflect.name
import net.kotlinx.spring.el.SpringElUtil
import kotlin.reflect.full.memberProperties

/**
 * 벨리데이션 결과를 매핑하는 컨버터 도구
 */
class HibernateValidationResult(val violation: ConstraintViolation<*>) {

    //==================================================== 기본설정 ======================================================

    /** 벨리데이션이 설졍된 어노테이션 */
    val annotaion: Annotation = violation.constraintDescriptor.annotation!!

    /** 기본으로 클래스 명 */
    val code = annotaion.annotationClass.name()

    /** 필드 ID */
    val fieldId = violation.propertyPath.toString()

    /** 어노테이션이 달린 필드 */
    val field = violation.rootBean::class.memberProperties.firstOrNull { it.name == fieldId }

    /** 기본 필드명 */
    val fieldName = field?.annotaionAll()?.findClass<Comment>()?.firstOrNull()?.value ?: fieldId

    /** 거부된 값 */
    val invalidValue: Any? = violation.invalidValue

    /** 속성 */
    val attr: Map<String, Any?>? = violation.constraintDescriptor.attributes.entries.filter { it.key !in ANN_ATT_IGNORES }.associate { it.key!! to it.value }

    /** 기본 메세지. 이걸 위해서 객체로 매핑했다. */
    val message: String = SpringElUtil.elFormat(violation.message, this)

    /** 변환  */
    fun toValidationResult(): ValidationResult = ValidationResult(
        code = code,
        fieldId = fieldId,
        fieldName = fieldName,
        invalidValue = invalidValue,
        attr = attr,
        message = message,
    )

    companion object {
        /** SpringValidatorAdapter 에서 복붙했다.. 맘에 안들어..  리플렉션 시 기본 어노테이션 인자를 제거한다.  */
        private val ANN_ATT_IGNORES = setOf("message", "groups", "payload")
    }


}