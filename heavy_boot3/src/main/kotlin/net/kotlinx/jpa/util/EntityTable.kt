package net.kotlinx.jpa.util

import net.kotlinx.string.toTextGrid

/** 테이블 */
data class EntityTable(
    val name: String,
    val columns: List<EntityColumn>,
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