package net.kotlinx.module1.guava

import com.google.common.reflect.ClassPath
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
    val classes: List<Class<*>> by lazy {
        ClassPath.from(ClassLoader.getSystemClassLoader()).allClasses
            .filter { clazz -> clazz.packageName.startsWith(packageName) }
            .map { clazz -> clazz.load() }
    }

    /** 특정 어노테이션이 포함된 모든 클래스 */
    fun findByAnnotation(clazz: KClass<*>): List<Class<*>> = classes.filter { c -> c.annotations.firstOrNull { clazz.isInstance(it) } != null }

}
