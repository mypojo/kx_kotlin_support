package net.kotlinx.jpa.util

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * 리플렉션 패키지로 가야할지도
 * */
enum class KColumnTypeGroup {
    STRING, NUMBER, ENUM, LOCAL_DATE_TIME, LOCAL_DATE, BOOLEAN, UNKNOWN
    ;

    companion object {
        fun from(columnType: KClass<*>): KColumnTypeGroup = when {
            columnType == String::class -> STRING
            columnType.isSubclassOf(Number::class) -> NUMBER
            columnType.isSubclassOf(Enum::class) -> ENUM
            columnType == LocalDateTime::class -> LOCAL_DATE_TIME
            columnType == LocalDate::class -> LOCAL_DATE
            columnType == Boolean::class -> BOOLEAN
            else -> UNKNOWN
        }
    }
}