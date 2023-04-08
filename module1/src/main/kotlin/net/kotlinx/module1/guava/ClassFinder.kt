package net.kotlinx.module1.guava

import KColumn
import KTable
import com.google.common.reflect.ClassPath
import net.kotlinx.core1.lang.annotationsOrEmpty
import net.kotlinx.core1.lang.findClass
import net.kotlinx.core1.lang.props
import net.kotlinx.core1.lang.toKClass
import kotlin.reflect.KClass


/**
 * 클래스 동적 로드 유틸
 * https://www.baeldung.com/java-find-all-classes-in-package
 * 자바 특성상 최초 로드시에는 file 로 접근을 해야함
 * */
class ClassFinder(
    private val packageName: String
) {

    /** 해당 패키지의 전체 클래스 */
    val classes: List<KClass<*>> by lazy {
        ClassPath.from(ClassLoader.getSystemClassLoader()).allClasses
            .filter { clazz -> clazz.packageName.startsWith(packageName) }
            .map { clazz -> clazz.load().kotlin }
    }

    //==================================================== 작업 샘플 ======================================================

    /** JPA 출력 */
    fun findAll(table: KClass<out Annotation>, column: KClass<out Annotation>): List<KTable> {
        return classes.filter { it.annotationsOrEmpty.findClass(table).isNotEmpty() }.map { table ->
            KTable(
                table.simpleName!!,
                table.props().filter { it.annotations.findClass(column).isNotEmpty() }.map { column ->
                    KColumn(
                        column.name,
                        column.returnType.toKClass()!!
                    )
                }
            )
        }
    }

}
