package net.kotlinx.reflect

import net.kotlinx.string.toLocalDateTime
import net.kotlinx.string.toTextGrid
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf


/**
 * * 간단한 리플렉션 도구
 * 패키지 크기가 커서 의존관계 하위로 이동시킴
 * @see Bean
 * */
@Deprecated("Bean 사용")
object ReflectionLineUtil {

    /**
     * 객체간 변환을 도와준다.
     * @param to 기본 생성자가 있어야함
     * */
    @Deprecated("Bean 사용")
    fun <T : Any> convertTo(from: Any, to: KClass<T>): T {
        val fromMap: Map<String, KProperty<*>> = from::class.members.filterIsInstance<KProperty<*>>().associateBy { it.name }
        val newInstance: T = to.constructors.firstOrNull { it.parameters.isEmpty() }?.call() ?: throw IllegalArgumentException("기본 생성자가 있어야 합니다 : $to")
        to.members.filterIsInstance<KMutableProperty<*>>().forEach { toField ->
            val value: Any? = fromMap[toField.name]?.getter?.call(from)
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
    @Deprecated("Bean 사용")
    fun <T : Any> lineToData(from: Array<String?>, to: KClass<T>): T {
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
    @Deprecated("Bean 사용")
    fun dataToLine(from: Any): List<Any> {
        val fromMap = from::class.members.filterIsInstance<KProperty<*>>().associateBy { it.name }
        //가장 긴 파라메터로 사용
        return from::class.constructors.maxWith(compareBy { it.parameters.size }).parameters.map { p ->
            fromMap[p.name]?.getter?.call(
                from
            ) ?: ""
        }
    }

    /** 간단 로그 출력용 */
    @Deprecated("Bean 사용2", ReplaceWith("clazz.constructors.maxWith(compareBy { it.parameters.size }).parameters.map { it.name ?: \"-\" }"))
    fun dataToHeader(clazz: KClass<*>): List<String> = clazz.constructors.maxWith(compareBy { it.parameters.size }).parameters.map { it.name ?: "-" }

    /**
     * @return 입력된 문자열을 지정된 타입의 값으로 변경해준다.
     * */
    @Deprecated("Bean 사용")
    private fun convertTo(type: KType, value: String?, nameForPrint: String?): Any? {
        val kClazz: KClass<*> = type.classifier as? KClass<*> ?: throw IllegalStateException("안되는거! $type") //타입을 클래스로 변환 가능 (실패할 수 있음)
        val nullMarker: DataNullMark = when {
            !value.isNullOrEmpty() -> DataNullMark.NONE
            type.isMarkedNullable -> DataNullMark.NULL
            else -> DataNullMark.EMPTY
        }
        return when {
            kClazz.isSubclassOf(Int::class) -> {
                when (nullMarker) {
                    DataNullMark.NULL -> null
                    DataNullMark.EMPTY -> 0
                    DataNullMark.NONE -> value!!.toInt()
                }
            }

            kClazz.isSubclassOf(Long::class) -> {
                when (nullMarker) {
                    DataNullMark.NULL -> null
                    DataNullMark.EMPTY -> 0L
                    DataNullMark.NONE -> value!!.toLong()
                }
            }

            kClazz.isSubclassOf(LocalDateTime::class) -> {
                when (nullMarker) {
                    DataNullMark.NULL -> null
                    DataNullMark.EMPTY -> throw IllegalStateException("$nameForPrint must not be empty")
                    DataNullMark.NONE -> value!!.toLocalDateTime()
                }
            }

            kClazz.java.isEnum -> {
                when (nullMarker) {
                    DataNullMark.NULL -> null
                    DataNullMark.EMPTY -> throw IllegalStateException("$nameForPrint must not be empty")
                    DataNullMark.NONE -> kClazz.java.enumConstants.filterIsInstance<Enum<*>>()
                        .first { it.name == value }
                }
            }
            //데이터 클래스인경우
            kClazz.isData -> {
                when (nullMarker) {
                    DataNullMark.NULL -> null
                    DataNullMark.EMPTY -> throw IllegalStateException("$nameForPrint must not be empty")
                    DataNullMark.NONE -> convertTo(value!!, kClazz)
                }
            }

            else -> value
        }
    }
}

///** 리플렉션으로 출력. 하나 이상의 객체가 있어야 한다. */
//@Deprecated("Bean의 toTextGrid 사용")
//fun List<Any>.toTextGrid(): TextGrid {
//    val first = this.first()
//    val datas = this.map { ReflectionLineUtil.dataToLine(it).toTypedArray() }
//    return first::class.constructors.maxWith(compareBy { it.parameters.size }).parameters.map { it.name ?: "-" }.toTextGrid(datas)
//}

enum class DataNullMark {
    /** 정상 */
    NONE,

    /** 비어있으며 널 허용 안함 = 디폴트값 넣어줘야함 */
    EMPTY,

    /** 비어있으며 널 허용 */
    NULL,
    ;
}
