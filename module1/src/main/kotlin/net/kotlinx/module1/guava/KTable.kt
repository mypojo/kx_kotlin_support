package net.kotlinx.module1.guava

import net.kotlinx.core1.string.toTextGrid
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

data class KTable(
    val name: String,
    val columns: List<KColumn>,
) {
    fun print() {
        columns.map { column ->
            arrayOf(
                column.name,
                column.columnTypeGroup,
                column.columnType.toString().substringAfterLast(".")
            )
        }.also {
            listOf("name", "typeGroup", "type").toTextGrid(it).print()
        }
    }
}

data class KColumn(
    val name: String,
    val columnType: KClass<*>,
    val columnTypeGroup: KColumnTypeGroup = KColumnTypeGroup.from(columnType)
)

enum class KColumnTypeGroup {
    String, Number, Enum, LocalDateTime, LocalDate, Boolean, Unknown
    ;

    companion object {
        fun from(columnType: KClass<*>): KColumnTypeGroup = when {
            columnType == kotlin.String::class -> String
            columnType.isSubclassOf(kotlin.Number::class) -> Number
            columnType.isSubclassOf(kotlin.Enum::class) -> Enum
            columnType == java.time.LocalDateTime::class -> LocalDateTime
            columnType == java.time.LocalDate::class -> LocalDate
            columnType == kotlin.Boolean::class -> Boolean
            else -> Unknown
        }
    }
}