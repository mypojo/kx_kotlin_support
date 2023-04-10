package net.kotlinx.module1.guava

import mu.KotlinLogging
import net.kotlinx.core1.string.toSnakeFromCamel
import net.kotlinx.core1.string.toTextGrid
import net.kotlinx.module1.reflect.annotationsOrEmpty
import net.kotlinx.module1.reflect.findClass
import net.kotlinx.module1.reflect.props
import net.kotlinx.module1.reflect.toKClass
import kotlin.reflect.KClass


/**
 * JPA 클래스
 * XXXX (이동시키기)
 * */
object ClassFinderJpa {

    private val log = KotlinLogging.logger {}

    /** JPA 출력 */
    fun print(packageName: String, table: KClass<out Annotation>, column: KClass<out Annotation>) {

        val classFinder = ClassFinder(packageName)

        val tables = classFinder.classes.filter { it.annotationsOrEmpty.findClass(table).isNotEmpty() }.map { table ->
            KTable(
                table.simpleName!!,
                table.props().filter { it.annotations.findClass(column).isNotEmpty() }.map { column ->
                    KColumn(
                        column.name,
                        column.returnType.toKClass()
                    )
                }
            )
        }
        //전체항목 출력
        log.info { "전체 ${tables.size}" }
        tables.forEach { t ->
            log.info { "테이블명 : ${t.name}" }
            t.print()
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

        log.info { "동일 단어셋으로 구성된 컬럼명이 있는지?" }
        tables.flatMap { it.columns }.map { it.name.toSnakeFromCamel() }.groupBy { it.split("_").toSet() }.entries.forEach { e ->
            val distinct = e.value.distinct()
            if (distinct.size == 1) return@forEach
            log.warn { "컬럼명 단어 조합 불일치 발견 -> ${e.key} / $distinct" }
        }


    }

}

