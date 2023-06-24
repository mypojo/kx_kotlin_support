package net.kotlinx.module.guava

import net.kotlinx.core.string.toTextGrid
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
    KString, KNumber, KEnum, KLocalDateTime, KLocalDate, KBoolean, KUnknown
    ;

    companion object {
        fun from(columnType: KClass<*>): KColumnTypeGroup = when {
            columnType == String::class -> KString
            columnType.isSubclassOf(Number::class) -> KNumber
            columnType.isSubclassOf(Enum::class) -> KEnum
            columnType == java.time.LocalDateTime::class -> KLocalDateTime
            columnType == java.time.LocalDate::class -> KLocalDate
            columnType == Boolean::class -> KBoolean
            else -> KUnknown
        }
    }
}