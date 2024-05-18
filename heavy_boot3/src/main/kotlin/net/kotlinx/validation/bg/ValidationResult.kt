package net.kotlinx.validation.bg

import jakarta.validation.ConstraintViolation
import jakarta.validation.ValidationException
import net.kotlinx.core.Comment
import net.kotlinx.reflect.annotaionAll
import net.kotlinx.reflect.findClass
import net.kotlinx.string.toTextGrid
import kotlin.reflect.full.memberProperties

/** 간단변환 */
fun ConstraintViolation<*>.toResult() = ValidationResult(this)

/** 간단 변환 */
fun Collection<ValidationResult>.toException() = ValidationResultException(this)

/** 간단 출력 */
fun Collection<ValidationResult>.print() {
    this.map {
        arrayOf(
            it.propertyName,
            it.fieldName,
            it.code,
            it.attr,
            it.text,
        )
    }.also {
        listOf("프로퍼티", "코멘트", "어노테이션명", "어노테이션 속성", "최종결과메세지").toTextGrid(it).print()
    }
}

/**
 * 결과메세지를 만들기 위한 템플릿용 빈
 * jakarta ConstraintViolation 와 호환된다.
 */
@Deprecated("kotlin 제품 사용해주세요")
class ValidationResult(
    val violation: ConstraintViolation<*>
) {

    val annotaion: Annotation = violation.constraintDescriptor.annotation!!

    /**
     * 예외 코드 (어노테이션 이름)
     * ex) maxFileCnt
     */
    val code: String = annotaion.annotationClass.simpleName!!

    /** 프로퍼티 영문명 */
    val propertyName: String = violation.propertyPath.toString()

    /** 필드. 가능하다면 한글명 */
    val fieldName: String by lazy {
        val classFieldName = propertyName
        when (val field = violation.rootBean::class.memberProperties.firstOrNull { it.name == classFieldName }) {
            null -> classFieldName
            else -> {
                field.annotaionAll().findClass<Comment>().firstOrNull()?.value ?: classFieldName
            }
        }
    }

    /** 거부된 값 */
    val rejectedValue: Any? = violation.invalidValue

    /**
     * 어노테이션의 속성에서 공통속성을 제거 후 리턴한다.
     * ex) maxSize, limitCnt 등..
     *  */
    val attr: Map<String, Any?>
        get() = violation.constraintDescriptor.attributes.entries.filter { it.key !in ANN_ATT_IGNORES }.associate { it.key!! to it.value }

    /**
     * 기존 메세지 순서 -> {min} 등의 문법 지원함
     *  #1. 로케일에 따라 기본값이 제공
     *  #2. 별도 프로퍼티 추가로 전체 커스터마이징 가능 (설정파일)
     *  #3. anno 에서 개별로 재정의 가능
     * 최종 메세지는 기본 메세지 +@ 를 해준다.
     *  */
    val text = when {
        violation.invalidValue?.toString().isNullOrEmpty() -> "[${fieldName}] ${violation.message}"
        else -> "[${fieldName}] '${violation.invalidValue}' -> ${violation.message}"
    }

    companion object {
        /** SpringValidatorAdapter 에서 복붙했다.. 맘에 안들어..  리플렉션 시 기본 어노테이션 인자를 제거한다.  */
        private val ANN_ATT_IGNORES = setOf("message", "groups", "payload")
    }
}

/**
 * 상세 벨리데이션 예외를 추가한 케이스
 * * *   */
class ValidationResultException(val violationResults: Collection<ValidationResult>, val sep: String = "\n") : ValidationException(violationResults.joinToString(sep) { it.text })