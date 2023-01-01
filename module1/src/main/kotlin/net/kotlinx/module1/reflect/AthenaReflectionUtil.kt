package net.kotlinx.module1.reflect

import net.kotlinx.core1.string.toSnakeFromCamel
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * * 간단한 리플렉션 도구
 * */
object AthenaReflectionUtil {

    /**
     * 키와 타입을 리턴해준다.
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

