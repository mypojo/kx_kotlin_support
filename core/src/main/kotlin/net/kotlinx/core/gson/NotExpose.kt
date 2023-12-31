package net.kotlinx.core.gson

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

/**
 * Google Guava의 Expose와 유사.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class NotExpose(val value: String = "")

/** NotExpose의 기본구현.   */
class NotExposeStrategy : ExclusionStrategy {
    override fun shouldSkipField(att: FieldAttributes): Boolean {
        val notExpose: Annotation? = att.getAnnotation(NotExpose::class.java)
        return notExpose != null
    }

    override fun shouldSkipClass(arg0: Class<*>?): Boolean {
        return false
    }
}