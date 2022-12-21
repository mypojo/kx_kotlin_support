package net.kotlinx.module1.reflect

import net.kotlinx.core1.string.TextGrid
import net.kotlinx.core1.string.toLocalDateTime
import net.kotlinx.core1.string.toTextGrid
import net.kotlinx.module1.reflect.DataNullMark.*
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

/**
 * * 간단한 리플렉션 도구
 * 패키지 크기가 커서 의존관계 하위로 이동시킴
 * */
object ReflectionUtil {

    /**
     * 객체간 변환을 도와준다.
     * @param to 기본 생성자가 있어야함
     * */
    fun <T : Any> convertTo(from: Any, to: KClass<T>): T {
        val fromMap = from::class.members.filterIsInstance<KProperty<*>>().associateBy { it.name }
        val newInstance = to.constructors.firstOrNull { it.parameters.isEmpty() }?.call() ?: throw IllegalArgumentException("기본 생성자가 있어야 합니다 : $to")
        to.members.filterIsInstance<KMutableProperty<*>>().forEach { toField ->
            val value = fromMap[toField.name]?.getter?.call(from)
            value?.let { toField.setter.call(newInstance, it) }
        }
        return newInstance
    }

    /**
     * 리플렉션으로 data class를 생성해주는 도구.  constructor 기반으로 작동한다.
     * 용도 : athena 쿼리 결과 or csv -> data class 변환
     * 이 방법은 권장하지 않음. 코틀린의 엄격한 타입 체크와 기본 값을 다 무시한다.
     * 한정된 용도로만 사용하자
     * @see Serializable 확인
     * */
    fun <T : Any> lineToData(from: Array<String>, to: KClass<T>): T {
        val constructor = to.constructors.firstOrNull { it.parameters.size == from.size }
        checkNotNull(constructor) { "클래스 [${to.simpleName}] 에 입력인자 길이 ${from.size} 와 일치하는 생성자가 존재하지 않습니다. " }

        val constructorParams = constructor.parameters.mapIndexed { index, kParameter ->
            val name = kParameter.name
            val type = kParameter.type
            val value = from[index]
            return@mapIndexed convertTo(type, value, name)
        }
        try {
            return constructor.call(*constructorParams.toTypedArray())
        } catch (e: Exception) {
            constructor.parameters.mapIndexed { index, it ->
                val value = constructorParams[index]
                arrayOf(it.name, it.type, value)
            }.also {
                listOf("이름", "타입", "값").toTextGrid(it).print()
            }
            throw e
        }
    }

    /** 간단 로그 출력용 */
    fun dataToLine(from: Any): List<String> {
        val fromMap = from::class.members.filterIsInstance<KProperty<*>>().associateBy { it.name }
        //가장 긴 파라메터로 사용
        return from::class.constructors.maxWith(compareBy { it.parameters.size }).parameters.map { p ->
            val field = fromMap[p.name]
            val value = field?.getter?.call(from)
            value?.toString() ?: ""
        }
    }

    /** 간단 로그 출력용 */
    fun dataToHeader(clazz: KClass<*>): List<String> = clazz.constructors.maxWith(compareBy { it.parameters.size }).parameters.map { it.name ?: "-" }

    /**
     * @return 입력된 문자열을 지정된 타입의 값으로 변경해준다.
     * */
    private fun convertTo(type: KType, value: String, name: String?): Comparable<Nothing>? {
        val kClazz = type.classifier as KClass<*>
        val nullMarker = run {
            if (value.isNotEmpty()) return@run NONE
            if (type.isMarkedNullable) NULL else EMPTY
        }
        return run {
            if (kClazz.isSubclassOf(Int::class)) {
                when (nullMarker) {
                    NULL -> null
                    EMPTY -> 0
                    NONE -> value.toInt()
                }
            } else if (kClazz.isSubclassOf(Long::class)) {
                when (nullMarker) {
                    NULL -> null
                    EMPTY -> 0L
                    NONE -> value.toLong()
                }
            } else if (kClazz.isSubclassOf(LocalDateTime::class)) {
                when (nullMarker) {
                    NULL -> null
                    EMPTY -> throw IllegalStateException("$name must not be empty")
                    NONE -> value.toLocalDateTime()
                }
            } else if (kClazz.java.isEnum) {
                when (nullMarker) {
                    NULL -> null
                    EMPTY -> throw IllegalStateException("$name must not be empty")
                    NONE -> kClazz.java.enumConstants.filterIsInstance<Enum<*>>().first { it.name == value }
                }
            } else {
                value
            }

        }
    }
}

/** 리플렉션으로 출력. 하나 이상의 객체가 있어야 한다. */
fun List<Any>.toTextGrid(): TextGrid {
    val first = this.first()
    val datas = this.map { ReflectionUtil.dataToLine(it).toTypedArray() }
    return ReflectionUtil.dataToHeader(first::class).toTextGrid(datas)
}

enum class DataNullMark {
    /** 정상 */
    NONE,

    /** 비어있으며 널 허용 안함 */
    EMPTY,

    /** 비어있으며 널 허용 */
    NULL,
    ;
}