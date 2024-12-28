package net.kotlinx.aws.athena

import net.kotlinx.string.toSnakeFromCamel
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

object AthenaUtil {

    /** 한번에 50개 까지만 호출 가능  */
    const val API_LIMIT_SIZE = 50

    /**
     * 키와 타입을 리턴해준다.
     * data class 로 아테나 스키마 만들때 사용
     * @see  net.kotlinx.aws.athena.table.AthenaTable
     * */
    fun toSchema(clazz: KClass<*>): Map<String, String> {
        return clazz.members.filterIsInstance<KProperty<*>>().associate {
            val kClazz: KClass<*> = it.returnType.classifier as? KClass<*> ?: throw IllegalStateException("변환 실패")
            it.name.toSnakeFromCamel() to when (kClazz) {
                Long::class -> "bigint"
                Int::class -> "int"
                Boolean::class -> "boolean"
                LocalDateTime::class -> "timestamp"
                else -> "string"
            }
        }
    }
}