package net.kotlinx.json.gson

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

/**
 * Google Guava의 Expose와 유사.
 * @field:NotExpose 이렇게 쓰면됨 PROPERTY가 안되는듯.. 일단 스킵
 * 좀 쓰다가 Moshi  같은거로 대체해보자
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