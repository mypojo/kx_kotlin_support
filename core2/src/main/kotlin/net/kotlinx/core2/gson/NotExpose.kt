package net.kotlinx.core2.gson

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Google Guava의 Expose와 유사.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FIELD)
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