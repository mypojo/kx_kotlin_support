package net.kotlinx.jpa.util

import mu.KotlinLogging
import net.kotlinx.core.string.abbr
import net.kotlinx.core.string.toSnakeFromCamel
import net.kotlinx.core.string.toTextGrid
import net.kotlinx.module.guava.ClassFinder
import net.kotlinx.module.reflect.annotationsOrEmpty
import net.kotlinx.module.reflect.findClass
import net.kotlinx.module.reflect.props
import net.kotlinx.module.reflect.toKClass
import javax.persistence.Column
import javax.persistence.Table
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


/**
 * 클래스 파인더의 JPA 조회버전
 * */
class EntityFinder(
    val packageName: String,
) {

    /** 테이블 어노테이션 */
    val table: KClass<out Annotation> = Table::class

    /** 컬럼 어노테이션 */
    val column: KClass<out Annotation> = Column::class

    private val log = KotlinLogging.logger {}

    /** JPA 출력 */
    fun print() {

        val classFinder = ClassFinder(packageName)

        val tables = classFinder.classes.filter { it.annotationsOrEmpty.findClass(table).isNotEmpty() }.map { table ->
            EntityTable(
                table.simpleName!!,
                table.props().filter { it.annotations.findClass(column).isNotEmpty() }.map { column ->
                    EntityColumn(
                        column.name,
                        column.returnType.toKClass()
                    )
                }
            )
        }
        //전체항목 출력
        if (log.isTraceEnabled) {
            log.info { "전체 ${tables.size}" }
            tables.forEach { t ->
                log.info { "테이블명 : ${t.name}" }
                t.print()
            }
        }

        log.info { "컬럼명 카운트" }
        tables.flatMap { it.columns }.map { it.name.toSnakeFromCamel() }.groupBy { it }.entries.sortedBy { it.value.size * -1 }.map {
            arrayOf(it.key, it.value.size)
        }.also {
            listOf("컬럼", "카운트").toTextGrid(it).print()
        }

        log.info { "단어 카운트" }
        tables.flatMap { it.columns }.map { it.name.toSnakeFromCamel() }.flatMap { it.split("_") }.groupBy { it }.entries.sortedBy { it.value.size * -1 }.map {
            arrayOf(it.key, it.value.size)
        }.also {
            listOf("단어", "카운트").toTextGrid(it).print()
        }

        log.info { "동일 단어셋으로 구성된 컬럼명이 있는지 체크.." }
        tables.flatMap { it.columns }.map { it.name.toSnakeFromCamel() }.groupBy { it.split("_").toSet() }.entries.forEach { e ->
            val distinct = e.value.distinct()
            if (distinct.size == 1) return@forEach
            log.warn { "컬럼명 단어 조합 불일치 발견 -> ${e.key} / $distinct" }
        }

        //접미어 맵
        val suffMap = tables.flatMap { it.columns }.groupBy { it.name.toSnakeFromCamel().substringAfterLast("_") }
        val eqTypeIgnores = setOf("id")
        suffMap.entries.forEach { c ->
            if (c.key in eqTypeIgnores) return@forEach
            val types = c.value.map { it.columnType }.distinct()
            if (types.any { it.java.isEnum }) return@forEach //enum 류는 무시
            if (types.count() > 1) {
                log.warn("동일한 접미어에 다른 타입 사용됨 [${c.key}] => ${c.value.map { "${it.name}(${it.columnType.simpleName})" }.distinct().joinToString(",")}")
            }
        }

        val suffPrintIgnores = setOf("id", "yn", "div", "date", "time", "status", "nm", "no")
        suffMap.entries.filter { it.key !in suffPrintIgnores && it.value.size > 1 }.sortedBy { it.value.size * -1 }.map { c ->
            arrayOf(c.key, c.value.first().columnType.simpleName, c.value.size, c.value.map { it.name }.distinct().joinToString(",").abbr(100))
        }.also {
            listOf("접미어", "타입", "사용수", "샘플").toTextGrid(it).print()
        }


    }

}

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

/** 컬럼 */
data class EntityColumn(
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